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

docker-compose up -d zookeeper kafka redis auth-db restaurant-db order-db

docker-compose up eureka-server config-server

```

## 📈 Monitoring
- Health Endpoints: /actuator/health
- Metrics: /actuator/metrics
- Info: /actuator/inf