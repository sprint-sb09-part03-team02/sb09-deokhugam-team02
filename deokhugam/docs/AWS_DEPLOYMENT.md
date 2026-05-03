# AWS 배포 및 운영 가이드

이 문서는 덕후감 서버를 AWS ECS EC2 방식으로 배포하고, RDS, S3, Secrets Manager, CloudWatch Logs, GitHub Actions까지 운영 관점에서 확인하는 절차를 정리합니다.

## 전체 구조

```mermaid
flowchart LR
  GH["GitHub Actions"] --> ECR["Amazon ECR"]
  ECR --> ECS["ECS EC2 Service"]
  ECS --> RDS["RDS PostgreSQL"]
  ECS --> S3IMG["S3 image bucket"]
  ECS --> CW["CloudWatch Logs"]
  CW --> LAMBDA["Lambda export task"]
  EVENT["EventBridge Scheduler"] --> LAMBDA
  LAMBDA --> S3LOG["S3 log bucket"]
  SM["Secrets Manager"] --> ECS
```

## 핵심 리소스

| 리소스 | 값 |
| --- | --- |
| Region | `<aws-region>` |
| ECR repository | `<ecr-repository>` |
| ECS cluster | `<ecs-cluster>` |
| ECS service | `<ecs-service>` |
| ECS task family | `<ecs-task-family>` |
| Container name | `deokhugam-server` |
| Container port | `8080` |
| Runtime | ECS EC2, `linux/amd64`, bridge network |
| RDS | PostgreSQL |
| Image bucket | `<image-bucket>` |
| Log bucket | `<log-bucket>` |
| CloudWatch log group | `<cloudwatch-log-group>` |
| Log export Lambda | `deokhugam-cloudwatch-log-export` |
| Log export schedule | `deokhugam-daily-cloudwatch-log-export` |

## 네트워크 구성 체크

### VPC / Subnet

- ECS EC2 인스턴스와 RDS는 같은 VPC에 둡니다.
- ECS EC2 인스턴스는 ECR 이미지 pull과 외부 API 호출이 가능해야 합니다.
- RDS는 Public access를 끄고, ECS 보안그룹에서만 접근하도록 구성합니다.

### Security Group

| Security Group | Inbound | Source |
| --- | --- | --- |
| ECS SG | `8080` | 테스트/접속 대상 IP 또는 ALB SG |
| ECS SG | `22` | 운영자 IP, 필요 시에만 임시 허용 |
| RDS SG | `5432` | ECS SG |
| RDS SG | `5432` | 운영자 IP, DB 클라이언트 직접 점검 시에만 임시 허용 |

RDS 연결 장애가 발생하면 먼저 RDS SG의 `5432` inbound source가 ECS SG인지 확인합니다.

## Secrets Manager

컨테이너 런타임 민감값은 GitHub Secrets가 아니라 AWS Secrets Manager에서 관리합니다.

`task-definition.json`에서 참조하는 키:

| Key | 용도 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | RDS JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | RDS 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | RDS 비밀번호 |
| `NAVER_CLIENT_ID` | Naver 검색 API client id |
| `NAVER_CLIENT_SECRET` | Naver 검색 API secret |
| `OCR_SPACE_API_KEY` | OCR Space API key |

현재 인증 구조는 `Deokhugam-Request-User-ID` 헤더 기반입니다. `JWT_SECRET`, `JWT_EXPIRATION`은 task definition secrets에 넣지 않습니다.

## ECS Task Definition

현재 task definition은 루트의 `task-definition.json`을 기준으로 관리합니다.

주요 설정:

| 항목 | 값 |
| --- | --- |
| `requiresCompatibilities` | `EC2` |
| `networkMode` | `bridge` |
| `cpu` | `512` |
| `memory` | `768` |
| `runtimePlatform.cpuArchitecture` | `X86_64` |
| `executionRoleArn` | `ecsTaskExecutionRole` |
| `taskRoleArn` | `<ecs-task-role>` |
| Health check | `curl -f http://localhost:8080/actuator/health || exit 1` |

컨테이너 환경변수:

| Key | 값 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` |
| `AWS_REGION` | `<aws-region>` |
| `AWS_S3_BUCKET` | `<image-bucket>` |
| `S3_LOG_UPLOAD_ENABLED` | `false`, 앱 내부 파일 로그 업로드 fallback 비활성화 |

## Docker 이미지

ECS EC2 인스턴스가 `linux/amd64` 환경이므로 이미지는 반드시 amd64로 빌드합니다.

```bash
cd deokhugam
docker buildx build --platform linux/amd64 -t <ecr-repository>:latest --load .
```

Dockerfile은 non-root `appuser`로 애플리케이션을 실행합니다. 운영 로그는 컨테이너 파일이 아니라 표준 출력으로 남기고, ECS `awslogs` 드라이버가 CloudWatch Logs로 수집합니다.

## S3 로그 적재

### 운영 로그 흐름

운영 로그는 컨테이너 로컬 파일에 의존하지 않습니다. ECS task 교체나 재시작 시 `/app/logs` 파일은 사라질 수 있으므로, CloudWatch Logs를 원본으로 보고 S3는 보관용 export 대상으로 사용합니다.

```text
ECS stdout
-> CloudWatch Logs /ecs/deokhugam-task
-> Lambda deokhugam-cloudwatch-log-export
-> CloudWatch Logs Export Task
-> S3 deokhugam-logs-297904/cloudwatch/yyyy/MM/dd/
```

| 항목 | 값 |
| --- | --- |
| CloudWatch log group | `/ecs/deokhugam-task` |
| S3 bucket | `deokhugam-logs-297904` |
| S3 prefix | `cloudwatch/` |
| 날짜별 경로 | `cloudwatch/yyyy/MM/dd/` |
| MDC 필드 | `requestId`, `clientIp` |

### Lambda Export

Lambda `deokhugam-cloudwatch-log-export`는 전날 KST 기준 하루 범위를 계산해 CloudWatch Logs Export Task를 생성합니다.

환경변수:

| Key | 값 |
| --- | --- |
| `LOG_GROUP_NAME` | `/ecs/deokhugam-task` |
| `DESTINATION_BUCKET` | `deokhugam-logs-297904` |
| `DESTINATION_PREFIX` | `cloudwatch` |

수동 검증:

```bash
aws logs describe-export-tasks --region ap-northeast-2
```

성공 기준은 export task `status.code`가 `COMPLETED`이고, S3에 `cloudwatch/yyyy/MM/dd/` 하위 객체가 생성되는 것입니다.

### EventBridge Scheduler

| 항목 | 값 |
| --- | --- |
| Schedule name | `deokhugam-daily-cloudwatch-log-export` |
| Schedule expression | `cron(10 1 * * ? *)` |
| Timezone | `Asia/Seoul` |
| Target | Lambda `deokhugam-cloudwatch-log-export` |
| Payload | `{}` |
| Retry | 최대 이벤트 수명 1시간, 재시도 2회 |
| DLQ | 없음 |

매일 `01:10 KST`에 실행되며, 전날 `00:00:00~23:59:59 KST` 범위의 CloudWatch Logs를 S3로 export합니다.

### S3 Lifecycle 권장값

| Prefix | Transition | Expiration |
| --- | --- | --- |
| `cloudwatch/` | 30일 후 Standard-IA 또는 Glacier 계층 | 90일 또는 프로젝트 제출 이후 삭제 |

## 운영 배치 스케줄

모든 운영 배치는 `Asia/Seoul` 시간대를 명시합니다.

| 작업 | 기본 실행 시각 |
| --- | --- |
| CloudWatch Logs S3 export | 매일 01:10 |
| 일간/전체 랭킹 갱신 | 매일 03:00 |
| 알림 정리 | 매일 03:30 |
| 주간 랭킹 갱신 | 매주 월요일 04:00 |
| 삭제 유저 물리 삭제 | 매일 04:30 |
| 월간 랭킹 갱신 | 매월 1일 05:00 |

팀 프로젝트 비용 최소화가 목적이면 로그 버킷은 장기 보관하지 않고 만료 정책을 설정합니다.

## IAM 권한

### GitHub Actions 배포용 IAM User

필요 권한:

- ECR 로그인 및 push
- ECS task definition 등록
- ECS service update
- `<ecs-task-execution-role>`, `<ecs-task-role>`에 대한 `iam:PassRole`

### `ecsTaskExecutionRole`

컨테이너 시작에 필요한 권한:

- ECR image pull
- CloudWatch Logs write
- Secrets Manager `GetSecretValue`

### `<ecs-task-role>`

애플리케이션 런타임에서 필요한 권한:

- `<image-bucket>` 이미지 업로드/조회/삭제

CloudWatch Logs를 S3로 export하는 권한은 ECS task role이 아니라 S3 bucket policy와 Lambda execution role에서 관리합니다.

### CloudWatch Logs Export용 S3 Bucket Policy

CloudWatch Logs가 S3 export를 수행하려면 로그 버킷에 CloudWatch Logs 서비스 principal 권한이 필요합니다.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCloudWatchLogsGetBucketAcl",
      "Effect": "Allow",
      "Principal": {
        "Service": "logs.ap-northeast-2.amazonaws.com"
      },
      "Action": "s3:GetBucketAcl",
      "Resource": "arn:aws:s3:::deokhugam-logs-297904",
      "Condition": {
        "StringEquals": {
          "aws:SourceAccount": "297904677990"
        },
        "ArnLike": {
          "aws:SourceArn": "arn:aws:logs:ap-northeast-2:297904677990:log-group:*"
        }
      }
    },
    {
      "Sid": "AllowCloudWatchLogsPutObject",
      "Effect": "Allow",
      "Principal": {
        "Service": "logs.ap-northeast-2.amazonaws.com"
      },
      "Action": "s3:PutObject",
      "Resource": "arn:aws:s3:::deokhugam-logs-297904/cloudwatch/*",
      "Condition": {
        "StringEquals": {
          "aws:SourceAccount": "297904677990"
        },
        "ArnLike": {
          "aws:SourceArn": "arn:aws:logs:ap-northeast-2:297904677990:log-group:*"
        }
      }
    }
  ]
}
```

## CI/CD 흐름

`main` 브랜치에 push되거나 수동 실행하면 CD가 수행됩니다.

1. Gradle 테스트 및 커버리지 검증
2. Docker `linux/amd64` 이미지 빌드
3. ECR에 `${github.sha}`, `latest` 태그 push
4. `task-definition.json`의 `${AWS_ACCOUNT_ID}` 치환
5. ECS task definition 새 revision 등록
6. ECS service 업데이트
7. service stability 대기

## 배포 후 검증

### ECS 상태

```bash
aws ecs describe-services \
  --cluster <ecs-cluster> \
  --services <ecs-service> \
  --region <aws-region>
```

확인할 값:

- `desiredCount = 1`
- `runningCount = 1`
- `pendingCount = 0`
- 최신 task definition revision 사용

### Health Check

```bash
curl http://{public-ip}:8080/actuator/health
```

정상 응답:

```json
{"status":"UP"}
```

### Batch Metrics

```bash
curl http://{public-ip}:8080/actuator/metrics
curl http://{public-ip}:8080/actuator/metrics/deokhugam.batch.job.completed
curl http://{public-ip}:8080/actuator/metrics/deokhugam.batch.job.last.success
```

Batch가 한 번 이상 실행된 뒤 메트릭 값이 표시됩니다.

### Flyway 마이그레이션 확인

도서 제목 정렬 보정은 Flyway 마이그레이션으로 운영 DB에 반영됩니다.

| Version | 역할 |
| --- | --- |
| `V4__add_book_title_sort_key.sql` | `books.title_sort_key` 컬럼 최초 추가 |
| `V5__replace_review_unique_constraint_with_partial_index.sql` | 리뷰 soft delete 유니크 제약 정리 |
| `V6__rebuild_book_title_sort_key.sql` | 기존 도서 제목 정렬키를 한글 가나다순 호환 키로 재계산 |

배포 후 제목순 정렬이 계속 어긋나면 아래 항목을 확인합니다.

1. 애플리케이션 시작 로그에서 Flyway migration 실패가 없는지 확인합니다.
2. RDS의 `flyway_schema_history`에 `V6`이 `success=true`로 기록됐는지 확인합니다.
3. `books.title_sort_key`가 비어 있지 않고 최신 규칙으로 재계산됐는지 확인합니다.
4. API `GET /api/books?orderBy=title&direction=ASC&limit=20` 응답이 제목 오름차순인지 확인합니다.

### S3 로그 적재

스케줄 기준 다음날 `01:10 KST` 이후 아래 경로를 확인합니다.

```text
s3://<log-bucket>/cloudwatch/yyyy/MM/dd/
```

객체가 없다면 아래 순서로 확인합니다.

1. EventBridge Scheduler `deokhugam-daily-cloudwatch-log-export`가 `Enabled`인지
2. Lambda `deokhugam-cloudwatch-log-export` 실행 로그에 오류가 없는지
3. `aws logs describe-export-tasks --region ap-northeast-2`에서 task 상태가 `COMPLETED`인지
4. S3 bucket policy에 `logs.ap-northeast-2.amazonaws.com`의 `s3:GetBucketAcl`, `s3:PutObject` 권한이 있는지
5. CloudWatch log group `/ecs/deokhugam-task`에 전날 로그가 실제로 존재하는지

## 트러블슈팅

### `no matching manifest for linux/amd64`

원인: ARM 이미지가 ECR에 올라갔거나 베이스 이미지가 amd64 manifest를 제공하지 않는 경우입니다.

해결:

```bash
docker buildx build --platform linux/amd64 -t <ecr-repository>:<tag> --load .
```

### `CannotPullContainerError`

확인 순서:

1. ECR 이미지 태그가 실제로 존재하는지
2. ECS EC2 인스턴스가 ECR에 접근 가능한지
3. `ecsTaskExecutionRole`에 ECR pull 권한이 있는지
4. task definition image URI가 올바른지

### `Retrieved secret ... did not contain json key`

원인: task definition의 `secrets.valueFrom`에 지정한 JSON key가 Secrets Manager에 없습니다.

해결:

- 실제 필요한 key만 task definition에 남깁니다.
- 현재 구조에서는 `JWT_SECRET`, `JWT_EXPIRATION`을 제거합니다.

### 컨테이너 `exitCode=1`

확인 순서:

1. CloudWatch Logs 확인
2. DB 연결 정보/보안그룹 확인
3. Flyway schema validation 확인
4. Secrets Manager key 누락 여부 확인
5. task definition의 이미지 태그와 CPU/Memory 설정 확인

현재 운영 로그는 컨테이너 파일이 아니라 표준 출력 기반 CloudWatch Logs로 수집합니다. 따라서 `/app/logs` 파일 존재 여부는 운영 로그 적재 성공 조건이 아닙니다.

### RDS 접속 실패

확인 순서:

1. RDS public access 여부
2. RDS SG inbound `5432`
3. source가 ECS SG 또는 현재 접속자 IP인지
4. DB name, username, password
5. `SPRING_DATASOURCE_URL` 형식

JDBC URL 예시:

```text
jdbc:postgresql://{rds-endpoint}:5432/deokhugam
```
