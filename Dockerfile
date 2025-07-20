# Use a basic Alpine Linux base image.
FROM alpine:latest

# Set the working directory inside the container.
WORKDIR /app

# Install OpenJDK 17 and Maven.
# 'apk update' updates the package index.
# 'apk add openjdk17 maven' installs both OpenJDK 17 and Maven.
# 'rm -rf /var/cache/apk/*' cleans up the apk cache to keep the image size small.
RUN apk update && \
    apk add --no-cache openjdk17 maven && \
    rm -rf /var/cache/apk/*

# Copy your entire Spring Boot project directory into the container.
# Make sure your 'pom.xml' (or 'build.gradle') and 'src' directory are in the root
# of the context directory when you build the image.
# The '.' copies everything from the current directory on your host to /app in the container.
COPY . .

# Expose the port your Spring Boot application listens on.
# Make sure this matches the port your Spring Boot app is configured to use (e.g., 8080 by default).
EXPOSE 8080

# Command to run your Spring Boot application directly from source using Maven.
# 'mvn spring-boot:run' compiles your application and runs it.
# This avoids creating a separate fat JAR.
CMD ["mvn", "spring-boot:run"]

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
