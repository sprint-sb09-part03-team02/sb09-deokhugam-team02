# CI/CD Workflow 설정 가이드

## 워크플로우 구조

| 파일 | 트리거 | 역할 |
|------|--------|------|
| `ci.yml` | dev/main PR | 빌드 + 테스트 + 커버리지 리포트 |
| `cd.yml` | main 브랜치 push, 수동 실행 | 테스트 → Docker 빌드 → ECR push → ECS 배포 |

## 브랜치 전략

```
feature/** ──PR──▶ dev ──PR──▶ main
                                  │
                                  ▼
                           production 배포 (CD 자동 실행)
```

## GitHub Actions 값 설정

GitHub 저장소 → Settings → Secrets and variables → Actions 에서 등록

### Secrets (민감 정보) — Actions secrets 탭

| 이름 | 설명 |
|------|------|
| `AWS_ACCESS_KEY_ID` | CD용 IAM 유저 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | CD용 IAM 유저 시크릿 키 |

### Variables (비민감 설정값) — Actions variables 탭

| 이름 | 값 | 설명 |
|------|-----|------|
| `AWS_REGION` | `ap-northeast-2` | AWS 리전 |
| `ECR_REPOSITORY` | `deokhugam` | ECR 리포지토리 이름 |
| `ECS_TASK_DEFINITION` | `task-definition.json` | 리포 루트 기준 파일 경로 |
| `ECS_SERVICE` | `deokhugam-service` | ECS 서비스 이름 |
| `ECS_CLUSTER` | `deokhugam-cluster` | ECS 클러스터 이름 |

> `ECS_TASK_DEFINITION`은 Family 이름이 아니라 **파일 경로 문자열**입니다.

---

## IAM 권한 설정

이 프로젝트에는 **IAM 주체가 2개** 존재합니다.  
역할이 다르기 때문에 정책을 반드시 분리해야 합니다.

### 주체 1: CD용 IAM 유저 (GitHub Actions 배포 주체)

`AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY`로 인증되는 주체입니다.  
**ECR push와 ECS 배포 호출 권한만 필요합니다.**

#### ❌ 이 유저에서 제거해야 할 정책

| 정책명 | 제거 이유 |
|--------|-----------|
| `AmazonECSTaskExecutionRo...` | 태스크 실행 역할용 정책이며, IAM 유저가 아닌 `ecsTaskExecutionRole`에 부여해야 함 |
| `SecretsManagerAccess` | Secrets Manager 조회는 컨테이너 런타임에 `ecsTaskExecutionRole`이 수행. CD 유저는 불필요 |
| `AllowReadDiscodeitEnvFromS3` | `task-definition.json`에서 `environmentFiles`를 사용하지 않으므로 불필요 |

#### ✅ 이 유저에 부여할 정책 (인라인 정책으로 추가)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ECRLogin",
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Sid": "ECRPush",
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload"
      ],
      "Resource": "arn:aws:ecr:ap-northeast-2:{aws Id}:repository/deokhugam"
    },
    {
      "Sid": "ECSDeployRead",
      "Effect": "Allow",
      "Action": [
        "ecs:DescribeTaskDefinition",
        "ecs:DescribeServices"
      ],
      "Resource": "*"
    },
    {
      "Sid": "ECSDeployWrite",
      "Effect": "Allow",
      "Action": [
        "ecs:RegisterTaskDefinition",
        "ecs:UpdateService"
      ],
      "Resource": "*"
    },
    {
      "Sid": "PassExecutionRole",
      "Effect": "Allow",
      "Action": "iam:PassRole",
      "Resource": "arn:aws:iam::{aws Id}:role/ecsTaskExecutionRole"
    }
  ]
}
```

---

### 주체 2: ecsTaskExecutionRole (컨테이너 런타임 주체)

`task-definition.json`의 `executionRoleArn`에 지정된 역할입니다.  
**컨테이너 시작 시 Secrets Manager에서 시크릿을 조회하는 역할입니다.**

#### ✅ 이 Role에 부여할 정책 (인라인 정책으로 추가)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "SecretsManagerRead",
      "Effect": "Allow",
      "Action": "secretsmanager:GetSecretValue",
      "Resource": "arn:aws:secretsmanager:ap-northeast-2:{aws Id}:secret:deokhugam/prod*"
    },
    {
      "Sid": "CloudWatchLogs",
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:ap-northeast-2:{aws Id}:log-group:/ecs/deokhugam-task:*"
    }
  ]
}
```

> `AmazonECSTaskExecutionRolePolicy` (AWS 관리형) + 위 인라인 정책을 함께 사용하면 됩니다.

---

## 런타임 시크릿 관리

민감값은 GitHub에 저장하지 않고, AWS Secrets Manager로 관리합니다.

| 시크릿 키 | 저장 위치 | 참조 방식 |
|-----------|-----------|-----------|
| `JWT_SECRET` | Secrets Manager `deokhugam/prod` | `task-definition.json` `secrets.valueFrom` |
| `DB_PASSWORD` | Secrets Manager `deokhugam/prod` | `task-definition.json` `secrets.valueFrom` |
| `NAVER_CLIENT_SECRET` | Secrets Manager `deokhugam/prod` | `task-definition.json` `secrets.valueFrom` |

