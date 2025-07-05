FROM openjdk:21-jdk-slim

LABEL maintainer="Mr Lii <"
LABEL service="config-server"
LABEL authors="Mr Lii"

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY config-server/pom.xml ./config-server/

RUN ./mvnw dependency:go-offline -B -f config-server/pom.xml

COPY config-server/src ./config-server/src/

RUN ./mvnw clean package -B -f config-server/pom.xml -DskipTests

RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --ingroup spring spring

COPY --chown=spring:spring config-server/target/*.jar app.jar

USER spring

EXPOSE 8888

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]