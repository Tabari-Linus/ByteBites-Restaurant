FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/restaurant-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]