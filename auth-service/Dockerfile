FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl auth-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/auth-service/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]