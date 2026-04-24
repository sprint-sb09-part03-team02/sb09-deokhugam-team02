# 덕후감 서버 (Deokhugam Server)

[![CI](https://github.com/sprint-sb09-part03-team02/sb09-deokhugam-team02/actions/workflows/ci.yml/badge.svg)](https://github.com/sprint-sb09-part03-team02/sb09-deokhugam-team02/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/sprint-sb09-part03-team02/sb09-deokhugam-team02/graph/badge.svg?token=ZHKEENIICM)](https://codecov.io/gh/sprint-sb09-part03-team02/sb09-deokhugam-team02)
## 🛠 기술 스택

- **Java 17** / **Spring Boot 3.5.**
- **PostgreSQL** / **JPA** / **QueryDSL 5.1.0**
- **MapStruct 1.6.2** / **JWT (JJWT 0.12.6)**
- **AWS S3 (SDK v2)**

## 🚀 실행 방법

### 기본: 로컬 H2 환경 (별도 설정 불필요) ⭐ 권장

```bash
cd deokhugam
./gradlew bootRun
```

### 로컬 PostgreSQL 환경

1. `application-local.yml.example`을 참고하여 `application-local.yml` 생성
2. 본인의 PostgreSQL 정보 입력
3. 실행:
```bash
cd deokhugam
./gradlew bootRun --args='--spring.profiles.active=local'
```

### AWS 배포 환경

```bash
cd deokhugam
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**📌 주의:** 환경변수 설정 필수 (자세히: [`deokhugam/docs/AWS_DEPLOYMENT_ENV_VARS.md`](deokhugam/docs/AWS_DEPLOYMENT_ENV_VARS.md))

## 🧪 테스트 및 커버리지

```bash
cd deokhugam
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

- 커버리지 리포트: `deokhugam/build/reports/jacoco/test/html/index.html`
- 최소 커버리지 기준: **80%**

## 📚 환경 설정 및 배포

- **[환경 구성 검토 보고서](deokhugam/docs/ENVIRONMENT_CONFIGURATION_REVIEW.md)** — 프로필 분리, 보안 설정
- **[AWS 배포 환경변수 명세](deokhugam/docs/AWS_DEPLOYMENT_ENV_VARS.md)** — ECS Task Definition, IAM 정책
- **[로컬/AWS DB 전략](deokhugam/docs/LOCAL_AWS_DB_STRATEGY.md)** — H2 vs PostgreSQL, 마이그레이션 전략
- **[환경 설정 최종 요약](ENVIRONMENT_SETUP_SUMMARY.md)** — 빠른 시작 가이드
