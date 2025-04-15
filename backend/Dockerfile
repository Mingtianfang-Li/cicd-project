# Use Java 21 runtime image
FROM openjdk:21-jdk-slim

# Create working directory
WORKDIR /app

# Copy your built jar
COPY build/libs/backend.jar .

# Expose the port your backend runs on (adjust if not 8080)
EXPOSE 8080

# Run your backend jar
CMD ["java", "-jar", "backend.jar"]
