FROM gradle:8.4-jdk17 AS builder
WORKDIR /workspace

COPY . .

RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy AS runtime
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
