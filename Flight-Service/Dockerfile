# Java 17 runtime
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# IMPORTANT: jar name must match your built jar
COPY target/Flight-Service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]