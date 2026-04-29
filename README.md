# 덕후감 서버 (Deokhugam Server)

[![CI](https://github.com/sprint-sb09-part03-team02/sb09-deokhugam-team02/actions/workflows/ci.yml/badge.svg)](https://github.com/sprint-sb09-part03-team02/sb09-deokhugam-team02/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/sprint-sb09-part03-team02/sb09-deokhugam-team02/graph/badge.svg)](https://codecov.io/gh/sprint-sb09-part03-team02/sb09-deokhugam-team02)

덕후감은 도서, 리뷰, 댓글, 알림, 인기 랭킹을 관리하는 Spring Boot 기반 백엔드 서버입니다.
로컬 개발은 H2로 바로 실행할 수 있고, 운영 배포는 GitHub Actions를 통해 ECR 이미지를 빌드한 뒤 AWS ECS EC2 서비스로 배포합니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.x, Spring Security, Spring Batch, Spring Actuator |
| Database | PostgreSQL, H2(local/test), JPA, QueryDSL 5.1.0, Flyway |
| Mapping / Validation | MapStruct 1.6.2, Bean Validation, Lombok |
| Infra | AWS ECS EC2, ECR, RDS PostgreSQL, S3, CloudWatch Logs, Secrets Manager |
| Test / Quality | JUnit5, Mockito, Spring Batch Test, JaCoCo, Codecov |
| CI/CD | GitHub Actions |

## 실행 방법

### 로컬 H2 실행

별도 DB 설정 없이 실행할 수 있는 기본 개발 방식입니다.

```bash
cd deokhugam
./gradlew bootRun
```

기본 프로필은 `local-h2`입니다.

### 로컬 PostgreSQL 실행

```bash
cd deokhugam
./gradlew bootRun --args='--spring.profiles.active=local'
```

민감값은 `application-local.yml`에 직접 커밋하지 않습니다. 필요한 경우 `application-local.yml.example`을 참고해 로컬 전용 파일로 관리합니다.

## 테스트 및 커버리지

```bash
cd deokhugam
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

- 전체 테스트 커버리지 기준: **80% 이상**
- JaCoCo HTML 리포트: `deokhugam/build/reports/jacoco/test/html/index.html`
- CI에서 PR 생성 시 테스트, 커버리지 검증, Codecov 업로드를 수행합니다.

## 인증 방식

현재 서버는 JWT 토큰 기반 인증이 아니라 API 요구사항에 맞춘 요청 헤더 기반 인증을 사용합니다.

| 헤더 | 설명 |
| --- | --- |
| `Deokhugam-Request-User-ID` | 로그인 이후 요청에서 사용자 UUID를 전달하는 헤더 |
| `X-Request-Id` | 서버가 요청별 추적을 위해 응답에 추가하는 request id |

로그인, 회원가입, 홈/공개 조회 경로를 제외한 보호 API는 `Deokhugam-Request-User-ID` 헤더를 기준으로 사용자 컨텍스트를 구성합니다.

## 주요 운영 기능

### MDC 요청 로그

모든 요청에 대해 `requestId`, `clientIp`를 MDC에 저장하고 로그 패턴에 포함합니다. 응답 헤더에는 `X-Request-Id`가 내려갑니다.

### S3 날짜별 로그 적재

애플리케이션 파일 로그는 `/app/logs`에 생성되고, 스케줄러가 매일 `00:10 Asia/Seoul`에 전날 로그를 S3에 업로드합니다.

- 로컬 로그 파일: `/app/logs/deokhugam.yyyy-MM-dd.log`
- S3 적재 경로: `app/yyyy/MM/dd/deokhugam.yyyy-MM-dd.log`
- 상세 설정: [AWS 배포 및 운영 가이드](deokhugam/docs/AWS_DEPLOYMENT.md)

### Spring Batch 및 Actuator 메트릭

인기 도서, 인기 리뷰, 파워 유저 랭킹과 알림 정리를 Spring Batch로 관리합니다. Batch 실행 결과는 Actuator 메트릭으로 확인할 수 있습니다.

주요 메트릭:

- `deokhugam.batch.job.completed`
- `deokhugam.batch.job.duration`
- `deokhugam.batch.job.last.duration.seconds`
- `deokhugam.batch.job.last.success`

## CI/CD

| Workflow | Trigger | 역할 |
| --- | --- | --- |
| `ci.yml` | `dev`, `main` 대상 PR | 테스트, 커버리지 검증, 리포트 업로드 |
| `cd.yml` | `main` push, 수동 실행 | 테스트, Docker 이미지 빌드, ECR push, ECS 배포 |

배포 흐름:

```text
PR -> CI 통과 -> dev -> main merge -> CD -> ECR push -> ECS service update
```

상세 GitHub Actions 설정은 [.github/workflows/README.md](.github/workflows/README.md)를 확인합니다.

## AWS 구성 요약

| 리소스 | 용도 |
| --- | --- |
| VPC / Subnet / Security Group | ECS, RDS 네트워크 격리 및 접근 제어 |
| ECR | Docker 이미지 저장소 |
| ECS EC2 | Spring Boot 애플리케이션 실행 |
| RDS PostgreSQL | 운영 데이터베이스 및 Spring Batch 메타테이블 저장 |
| S3 이미지 버킷 | 이미지/썸네일 저장 |
| S3 로그 버킷 | 날짜별 애플리케이션 로그 적재 |
| Secrets Manager | DB, Naver API, OCR Space API 민감값 관리 |
| CloudWatch Logs | ECS 컨테이너 표준 출력 로그 확인 |

자세한 구축/검증 절차는 [AWS 배포 및 운영 가이드](deokhugam/docs/AWS_DEPLOYMENT.md)를 확인합니다.

## API 검증

- 헬스 체크: `GET /actuator/health`
- 메트릭 목록: `GET /actuator/metrics`
- Swagger UI: `/swagger-ui/index.html`
- Postman 시나리오: [POSTMAN_TEST_SCENARIOS.md](deokhugam/docs/POSTMAN_TEST_SCENARIOS.md)

## 트러블슈팅 문서

주요 배포 이슈와 해결 방식은 [AWS 배포 및 운영 가이드](deokhugam/docs/AWS_DEPLOYMENT.md)의 트러블슈팅 섹션에 정리되어 있습니다.
