# Stage 1: Build
FROM gradle:8-jdk17-alpine AS builder

WORKDIR /app

# 의존성 캐시를 위해 gradle 설정 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 미리 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 후 빌드 (테스트 제외)
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# 보안: non-root 사용자 생성
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 빌드된 JAR 복사 (와일드카드로 버전 무관하게 처리)
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown appuser:appgroup app.jar

USER appuser

# 애플리케이션 포트
EXPOSE 8080

# 비민감 기본값
ENV DB_PORT=5432
ENV AWS_REGION=ap-northeast-2
ENV JWT_EXPIRATION=86400000
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
    "-jar", \
    "app.jar"]