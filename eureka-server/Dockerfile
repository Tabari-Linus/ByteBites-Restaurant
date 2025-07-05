FROM openjdk:21-jdk-slim

LABEL maintainer="Mr Lii <"
LABEL service="eureka-server"
LABEL authors="Mr Lii"

RUN mkdir -p /app
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY eureka-server/pom.xml ./eureka-server/

RUN ./mvnw dependency:go-offline -B -f eureka-server/pom.xml

COPY eureka-server/src ./eureka-server/src/

# Build application
RUN ./mvnw clean package -B -f eureka-server/pom.xml -DskipTests

# Create non-root user
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --ingroup spring spring

# Copy jar file
COPY --chown=spring:spring eureka-server/target/*.jar app.jar

USER spring

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]