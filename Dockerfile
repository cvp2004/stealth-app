# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy only pom first to leverage Docker layer caching for dependencies
COPY pom.xml ./

# Pre-fetch dependencies (offline) to speed up subsequent builds
RUN mvn -B -q -DskipTests dependency:go-offline

# Now copy the source and build
COPY src ./src

# Build the application (skip tests for faster image builds)
RUN mvn -B -DskipTests clean package


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy the fat jar from the builder image
# Adjust name if your artifactId/version changes
COPY --from=builder /workspace/target/evently-0.0.1-SNAPSHOT.jar /app/app.jar

# Default environment variables (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=default \
    SPRING_APP_NAME=evently \
    DATABASE_URL=jdbc:postgresql://localhost:5432/evently_db \
    REDIS_URL=redis://localhost:6379 \
    FLYWAY_ENABLED=true \
    TZ=Asia/Kolkata

# Install timezone support
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

# Expose the application port
EXPOSE 8080

# Use non-root user
USER spring

# JVM tuned for containers; Spring Boot jar execution
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-Duser.timezone=Asia/Kolkata","-jar","/app/app.jar"]
