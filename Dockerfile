FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY delta-common-core/pom.xml delta-common-core/
COPY delta-common-entity/pom.xml delta-common-entity/
COPY delta-common-service/pom.xml delta-common-service/
COPY delta-admin/pom.xml delta-admin/
COPY delta-platform/pom.xml delta-platform/
COPY delta-message/pom.xml delta-message/

RUN mvn dependency:go-offline -B

COPY delta-common-core/src delta-common-core/src
COPY delta-common-entity/src delta-common-entity/src
COPY delta-common-service/src delta-common-service/src
COPY delta-admin/src delta-admin/src
COPY delta-platform/src delta-platform/src
COPY delta-message/src delta-message/src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/delta-admin/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
