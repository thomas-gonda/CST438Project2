# syntax=docker/dockerfile:1

# Build the Spring Boot jar with the project's Gradle wrapper.
FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

COPY src/ src/
RUN ./gradlew bootJar --no-daemon --quiet

# Keep only the runtime and the executable jar in the final image.
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring \
    --home-dir /app --no-create-home spring
USER spring:spring

COPY --from=build --chown=spring:spring /app/build/libs/app.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/api/v1/games?size=1" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
