FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/target/*.jar app.jar
USER spring:spring
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
