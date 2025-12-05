# =============================================================================
# IMRBS Backend Dockerfile
# Multi-stage build for optimized production image
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom files for dependency caching
COPY pom.xml .
COPY imrbs-core/pom.xml imrbs-core/
COPY imrbs-infrastructure/pom.xml imrbs-infrastructure/
COPY imrbs-web/pom.xml imrbs-web/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY imrbs-core/src imrbs-core/src
COPY imrbs-infrastructure/src imrbs-infrastructure/src
COPY imrbs-web/src imrbs-web/src
COPY checkstyle.xml .

# Build application
RUN mvn clean package -DskipTests -B

# Extract layered jar for optimized Docker caching
WORKDIR /app/imrbs-web/target
RUN java -Djarmode=layertools -jar *.jar extract

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: Create non-root user
RUN addgroup -g 1000 imrbs && \
    adduser -u 1000 -G imrbs -s /bin/sh -D imrbs

# Install necessary tools and cleanup
RUN apk add --no-cache curl tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=Asia/Taipei

WORKDIR /app

# Copy layered application (for Docker caching optimization)
COPY --from=builder /app/imrbs-web/target/dependencies/ ./
COPY --from=builder /app/imrbs-web/target/spring-boot-loader/ ./
COPY --from=builder /app/imrbs-web/target/snapshot-dependencies/ ./
COPY --from=builder /app/imrbs-web/target/application/ ./

# Create log directory
RUN mkdir -p /var/log/imrbs && \
    chown -R imrbs:imrbs /var/log/imrbs /app

# Switch to non-root user
USER imrbs

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/var/log/imrbs/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
