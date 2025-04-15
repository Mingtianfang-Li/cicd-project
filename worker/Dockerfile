# Use a base image with Java 21
FROM eclipse-temurin:21-jdk

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file into the image
COPY build/libs/worker.jar .

# Expose the port (optional, for debugging or local access)
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "worker.jar"]
