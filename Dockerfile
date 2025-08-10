
# Use a basic Alpine Linux base image with OpenJDK 17 only (no Maven needed).
FROM alpine:latest

# Set the working directory inside the container.
WORKDIR /app

# Install OpenJDK 17.
RUN apk update && \
    apk add --no-cache openjdk17 && \
    rm -rf /var/cache/apk/*

# Copy only the built JAR from the build context root (as produced by the workflow)
COPY app.jar app.jar

# Expose the port your Spring Boot application listens on (default 8080).
EXPOSE 8080

# Command to run your Spring Boot application from the JAR.
CMD ["java", "-jar", "app.jar"]

# --- Alternative: Running from pre-compiled classes (if you don't want Maven in the image) ---
# If you have already compiled your application classes and gathered all dependencies
# into a specific structure (e.g., target/classes and target/lib), you could
# copy those directly and run using 'java -cp'. This is more complex to manage.
#
# FROM alpine:latest
# WORKDIR /app
# RUN apk update && \
#     apk add --no-cache openjdk17 && \
#     rm -rf /var/cache/apk/*
#
# # Assuming your compiled classes are in 'target/classes' and dependencies in 'target/lib'
# # You would need to ensure these directories exist and are populated on your host before building.
# COPY target/classes /app/classes
# COPY target/lib /app/lib
#
# # Construct the classpath to include all dependency JARs and your application classes.
# # This 'find' command dynamically builds the classpath.
# RUN sh -c 'echo "CLASSPATH=$(find /app/lib -name "*.jar" | tr "\n" ":"):/app/classes" > /app/classpath.sh'
#
# EXPOSE 8080
#
# # Replace 'com.example.yourapp.YourApplication' with your actual main class.
# CMD ["sh", "-c", "java -cp $(cat /app/classpath.sh) com.example.yourapp.YourApplication"]
