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
2. **Create .env file**
```bash
 create file with the files 
 db_username
 db_password
 auth_db_name
 restaurant_db_name
 order_db_name
 notification_db_name
 jwt_secret_key
 EMAIL_USERNAME
 EMAIL_PASSWORD
```

3. **Docker Setup**
```bash
# Build all services needed for local development
docker-compose up -d
````
4. **Run services in this order**
```bash
# Terminal 1: Discovery Server
cd discovery-server
./mvnw clean package spring-boot:run

# Terminal 2: Config Server
cd config-server  
./mvnw clean package spring-boot:run

# Terminal 3: API Gateway
cd api-gateway
./mvnw clean package spring-boot:run

# Terminal 4: Auth Service
cd auth-service
./mvnw clean package spring-boot:run

# Terminal 5: Restaurant Service (with Kafka)
cd restaurant-service
./mvnw clean package spring-boot:run

# Terminal 6: Order Service (with Kafka)
cd services/order-service
./mvnw clean package spring-boot:run

# Terminal 7: Notification Service (Kafka Consumer)
cd services/notification-service
./mvnw clean package spring-boot:run
```

5. ## 📈 Monitoring
- Health Endpoints: /actuator/health
- Metrics: /actuator/metrics
- Info: /actuator/inf

```bash
# Verify Kafka is running
docker logs bytebites-kafka

Check kafka UI Dashboard
# Access http://localhost:8090 to view topics and messages
```

# Kafka Ui to show message brokers in effect
![Kafka Dashboard](docs/images/Kafka%20UI.png)