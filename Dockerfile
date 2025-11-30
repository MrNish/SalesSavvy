# Use an OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Add the JAR file
ARG JAR_FILE=target/*.jar
COPY target/SalesSavvyApp-0.0.1-SNAPSHOT app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
