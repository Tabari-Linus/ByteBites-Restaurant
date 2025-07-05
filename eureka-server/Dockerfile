FROM openjdk:21-jdk-slim AS builder
WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

COPY config-server/pom.xml ./config-server/
COPY eureka-server/pom.xml ./eureka-server/
COPY shared/common/pom.xml ./shared/common/
COPY api-gateway/pom.xml ./api-gateway/
COPY auth-service/pom.xml ./auth-service/
COPY restaurant-service/pom.xml ./restaurant-service/
COPY order-service/pom.xml ./order-service/
COPY notification-service/pom.xml ./notification-service/

RUN ./mvnw dependency:go-offline

COPY config-server/src ./config-server/src/
COPY eureka-server/src ./eureka-server/src/
COPY shared/common/src ./shared/common/src/
COPY api-gateway/src ./api-gateway/src/
COPY auth-service/src ./auth-service/src/
COPY restaurant-service/src ./restaurant-service/src/
COPY order-service/src ./order-service/src/
COPY notification-service/src ./notification-service/src/

RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

EXPOSE 8761

COPY --from=builder /app/eureka-server/target/eureka-server-*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]