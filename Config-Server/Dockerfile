FROM maven:3.9.0-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw || true
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre:slim
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]