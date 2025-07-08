# ByteBites-Restaurant

A secure, cloud-native food restaurant platform built with Spring Boot 3.5.x and Java 21.

## 🏗️ Architecture

- **Eureka Server** (Eureka) - Service registry
- **Config Server** - Centralized configuration
- **API Gateway** - Request routing and JWT validation
- **Auth Service** - Authentication and authorization
- **Restaurant Service** - Restaurant and menu management
- **Order Service** - Order processing
- **Notification Service** - Event-driven notifications

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.9+

### Local Development

1. **Clone and build**
```bash
git clone https://github.com/Tabari-Linus/ByteBites-Restaurant
cd ByteBites-Restaurant
./mvnw clean compile
````
2. **Docker Setup**
```bash
# Build all services needed for local development
docker-compose up -d postgres-auth postgres-restaurant postgres-order kafka zookeeper
````
3. **Run services**
```bash
# Start the services in the following order
# Make sure to run these commands in the root directory of the project
cd eureka-server && ./mvnw spring-boot:run
cd ../config-server && ./mvnw spring-boot:run
cd ../api-gateway && ./mvnw spring-boot:run
````

```bash
# Start the services in the following order
# Make sure to run these commands in the root directory of the project
cd auth-service && ./mvnw spring-boot:run
cd ../restaurant-service && ./mvnw spring-boot:run
cd ../order-service && ./mvnw spring-boot:run
cd ../notification-service && ./mvnw spring-boot:run


```

## 📈 Monitoring
- Health Endpoints: /actuator/health
- Metrics: /actuator/metrics
- Info: /actuator/inf