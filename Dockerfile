ARG BUILD_IMAGE=gradle:8.11.0-jdk17
ARG RUNTIME_IMAGE=eclipse-temurin:17-jre

FROM ${BUILD_IMAGE} as builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM ${RUNTIME_IMAGE} as runner

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]