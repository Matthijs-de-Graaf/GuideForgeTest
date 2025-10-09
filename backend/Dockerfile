# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
<<<<<<< HEAD
FROM eclipse-temurin:21-jdk-slim
=======
FROM eclipse-temurin:21-jre
>>>>>>> 44266dd (Save local changes before switching branches)
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]