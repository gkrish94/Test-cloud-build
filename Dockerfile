FROM eclipse-temurin:17-jdk-focal

# Set working directory
WORKDIR /app

# Copy JAR file
COPY /workspace/target/*.jar app.jar

# Expose the port application runs on 8098
EXPOSE 8098

# Start the application using the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
