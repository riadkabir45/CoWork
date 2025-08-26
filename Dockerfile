
# Use a basic Eclipse Temurin Linux base image with OpenJDK 24 only (no Maven needed).
FROM eclipse-temurin:24.0.2_12-jre-ubi9-minimal

# Set the working directory inside the container.
WORKDIR /app

# Copy only the built JAR from the build context root (as produced by the workflow)
COPY target/app.jar app.jar

# Expose the port your Spring Boot application listens on (default 8080).
EXPOSE 8080

# Command to run your Spring Boot application from the JAR.
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
