# ── Stage 1: Build ──────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching for dependency downloads
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build, skipping tests (tests run against H2; integration tests
# should be run separately against a running database)
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
