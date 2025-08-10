# Multi-stage build
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Load dependencies (cached if pom.xml has not changed)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Production image
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy jar file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# JVM settings for container
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
