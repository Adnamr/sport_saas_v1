# ═══════════════════════════════════════════════════════════════════
# Sport SaaS Backend - Multi-stage Dockerfile
# ═══════════════════════════════════════════════════════════════════

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom files first for better layer caching
COPY pom.xml .
COPY common/pom.xml common/
COPY config/pom.xml config/
COPY tenant/pom.xml tenant/
COPY auth/pom.xml auth/
COPY catalog/pom.xml catalog/
COPY inventory/pom.xml inventory/
COPY order/pom.xml order/
COPY billing/pom.xml billing/
COPY notification/pom.xml notification/
COPY admin/pom.xml admin/
COPY app/pom.xml app/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY common/src common/src
COPY config/src config/src
COPY tenant/src tenant/src
COPY auth/src auth/src
COPY catalog/src catalog/src
COPY inventory/src inventory/src
COPY order/src order/src
COPY billing/src billing/src
COPY notification/src notification/src
COPY admin/src admin/src
COPY app/src app/src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy the built artifact
COPY --from=builder /app/app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
