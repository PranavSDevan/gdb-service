FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl account-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/account-service/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]