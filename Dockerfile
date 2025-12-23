FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY certs ./certs

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/certs ./certs

EXPOSE 8443
EXPOSE 6666/udp

ENTRYPOINT ["java", "-jar", "app.jar"]