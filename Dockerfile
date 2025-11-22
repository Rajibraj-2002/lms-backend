# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
# Fix permission issue during build
RUN chmod +x mvnw
# Build the jar file, skipping tests to save time
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar
# Expose the port
EXPOSE 8080
# Command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]