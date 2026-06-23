FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl eureka-server -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/eureka-server/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]