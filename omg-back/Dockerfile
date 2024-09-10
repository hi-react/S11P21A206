# 1단계: 빌드 단계 (Gradle 빌드를 수행)
FROM gradle:7.5.1-jdk17 AS build
WORKDIR /app

# Gradle 캐시를 활용하여 종속성 다운로드 속도 향상
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/
RUN gradle build --no-daemon || return 0

# 소스 코드 복사 및 빌드 수행
COPY . .
RUN gradle bootJar --no-daemon

# 2단계: 실행 단계 (경량화된 JDK로 실행)
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 애플리케이션을 실행
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
