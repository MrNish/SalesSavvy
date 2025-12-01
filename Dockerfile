# ==========================
# Stage 1: Build the JAR
# ==========================
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper and pom files
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

# Fix executable permission for Maven wrapper
RUN chmod +x mvnw

# Build the JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ==========================
# Stage 2: Run the application
# ==========================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/SalesSavvyApp-0.0.1-SNAPSHOT.jar app.jar

# Expose the port used by Spring Boot
EXPOSE 8080

# Run the application with optimized Java flags
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]