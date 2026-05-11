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

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/delta-admin/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 --start-period=60s \
  CMD curl -sf http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dfile.encoding=UTF-8", \
  "-jar", "app.jar"]
