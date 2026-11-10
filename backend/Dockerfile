# ----------------------------
#  Backend – Spring Boot
# ----------------------------
# Build stage (uses Maven wrapper)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
# download dependencies first (cache layer)
RUN ./mvnw -B dependency:go-offline
# copy source code
COPY src src
# package the application (skip tests for speed in CI)
RUN ./mvnw -B package -DskipTests

# Runtime stage (thin JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

