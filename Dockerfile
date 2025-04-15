FROM openjdk:21-jdk-slim

# Create app directory
WORKDIR /app

# Copy JAR from Gradle build output
COPY build/libs/*.jar app.jar

# Expose port if your app listens on 8080
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
