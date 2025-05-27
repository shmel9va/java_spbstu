FROM gradle:8.4-jdk21 AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle gradlew gradle/ ./
RUN ./gradlew dependencies --no-daemon

COPY . .

RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jdk-jammy AS runtime
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
