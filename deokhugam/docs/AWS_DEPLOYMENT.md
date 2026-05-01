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
  CW --> S3LOG["S3 log bucket"]
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

## Docker 이미지

ECS EC2 인스턴스가 `linux/amd64` 환경이므로 이미지는 반드시 amd64로 빌드합니다.

```bash
cd deokhugam
docker build --platform linux/amd64 -t <ecr-repository>:latest .
```

Dockerfile은 non-root `appuser`로 애플리케이션을 실행합니다. 운영 로그는 컨테이너 파일이 아니라 표준 출력으로 남기고, ECS `awslogs` 드라이버가 CloudWatch Logs로 수집합니다.

컨테이너 로컬 파일은 ECS task 재시작 시 유실될 수 있으므로 S3 적재의 원본으로 사용하지 않습니다.

## S3 로그 적재

### 권장 흐름

운영 기본 로그 적재 흐름은 아래와 같습니다.

```text
Spring Boot stdout
-> ECS awslogs driver
-> CloudWatch Logs /ecs/deokhugam-task
-> CloudWatch Logs Export 또는 Firehose
-> S3 log bucket
```

애플리케이션 내부 `DailyLogUploadScheduler`는 기본값에서 비활성화합니다. 이 방식은 `/app/logs` 같은 컨테이너 로컬 파일에 의존하지 않아 ECS 재시작, task 교체, Logback rollover 타이밍 문제를 피할 수 있습니다.

| 항목 | 값 |
| --- | --- |
| CloudWatch log group | `/ecs/deokhugam-task` |
| CloudWatch stream prefix | `ecs` |
| S3 export prefix | `cloudwatch/` |
| MDC 필드 | `requestId`, `clientIp` |

### CloudWatch Logs Export 수동 검증

1. AWS Console에서 `CloudWatch` → `Logs` → `Log groups`로 이동합니다.
2. `/ecs/deokhugam-task`를 선택합니다.
3. `Actions` → `Export data to Amazon S3`를 선택합니다.
4. Export 범위를 전날 `00:00:00`부터 `23:59:59`까지로 지정합니다.
5. 대상 버킷은 `<log-bucket>`, prefix는 `cloudwatch/yyyy/MM/dd/` 형태로 지정합니다.

Export 결과 경로 예시:

```text
s3://<log-bucket>/cloudwatch/yyyy/MM/dd/
```

완전 자동화가 필요하면 EventBridge Scheduler + Lambda로 `CreateExportTask`를 매일 호출하거나, 실시간성이 필요하면 CloudWatch Logs subscription filter + Kinesis Data Firehose + S3 구성을 사용합니다.

### S3 Lifecycle 권장값

| Prefix | Transition | Expiration |
| --- | --- | --- |
| `app/` | 30일 후 Standard-IA 또는 Glacier 계층 | 90일 또는 프로젝트 제출 이후 삭제 |

## 운영 배치 스케줄

모든 운영 배치는 `Asia/Seoul` 시간대를 명시합니다.

| 작업 | 기본 실행 시각 |
| --- | --- |
| CloudWatch Logs S3 Export | 매일 01:00 권장 |
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

CloudWatch Logs Export는 애플리케이션 task role이 아니라 Export를 실행하는 IAM principal 또는 Lambda role에 S3 쓰기 권한을 부여합니다.

CloudWatch Logs Export용 최소 권한 예시:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "CloudWatchLogsExport",
      "Effect": "Allow",
      "Action": "logs:CreateExportTask",
      "Resource": "arn:aws:logs:<aws-region>:<aws-account-id>:log-group:<cloudwatch-log-group>"
    },
    {
      "Sid": "S3LogExportWrite",
      "Effect": "Allow",
      "Action": [
        "s3:GetBucketAcl",
        "s3:PutObject"
      ],
      "Resource": [
        "arn:aws:s3:::<log-bucket>",
        "arn:aws:s3:::<log-bucket>/cloudwatch/*"
      ]
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

### S3 로그 적재

CloudWatch Logs Export 실행 후 아래 경로를 확인합니다.

```text
s3://<log-bucket>/cloudwatch/yyyy/MM/dd/
```

객체가 없다면 아래 순서로 확인합니다.

1. `/ecs/deokhugam-task` 로그 그룹에 대상 시간대 로그 이벤트가 있는지
2. Export task 상태가 `COMPLETED`인지
3. Export 대상 S3 bucket region이 CloudWatch Logs와 같은 region인지
4. Export 실행 주체에 `logs:CreateExportTask`, `s3:GetBucketAcl`, `s3:PutObject` 권한이 있는지

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
4. task definition의 image URI와 Secrets Manager 참조 확인

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
