# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# EXPOSE the port
EXPOSE 8080

# Optimize Java for Containers (Free Tier limit is 512MB RAM)
# -Xmx350m: Limits max memory to 350MB (leaving room for OS)
# -Xss512k: Reduces thread stack size to save memory
# -XX:+UseSerialGC: Uses a simpler garbage collector that uses less CPU/RAM
ENTRYPOINT ["java", "-Xmx350m", "-Xss512k", "-XX:+UseSerialGC", "-jar", "-Dserver.port=${PORT}", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]