# syntax=docker/dockerfile:1.7
# ---- build ------------------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/folio-api-*.jar extract --layers --launcher --destination target/extracted

# ---- runtime ----------------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S folio && adduser -S folio -G folio
WORKDIR /app
COPY --from=build --chown=folio:folio /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=folio:folio /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=folio:folio /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=folio:folio /workspace/target/extracted/application/ ./
USER folio
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" | grep -q '"UP"' || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
