# ByteBites-Restaurant

A secure, cloud-native food restaurant platform built with Spring Boot 3.5.x and Java 21.
The ByteBites project is a well-structured microservices platform for food delivery, built using Spring Boot and Spring Cloud. The architecture demonstrates good understanding of microservices patterns, with proper separation of concerns, security implementation, and event-driven communication.
## 1. Architecture

- **Eureka Server** (Eureka) - Service registry
- **Config Server** - Centralized configuration
- **API Gateway** - Request routing and JWT validation
- **Auth Service** - Authentication, authorization, and User management
- **Restaurant Service** - Restaurant and menu management
- **Order Service** - Order processing
- **Notification Service** - Event-driven notifications service with push notification
- **Kafka & Kafka UI** - For event streaming and monitoring
- **Data Layer** - PostgresSQL Database integration for AUth, Restaurant, Order, and Notification 
## 2. Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.9+

### Local Development

1. **Clone and build**
```bash
git clone https://github.com/Tabari-Linus/ByteBites-Restaurant
cd ByteBites-Restaurant
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
4. **Start services in this order**
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
5. **End-to-End Execution flow**
```bash
# Register restaurant owner
curl -X POST http://localhost:8080/auth/admin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@gmail.com", 
    "password": "password123",
    "firstName": "admin",
    "lastName": "coder",
    "role": "ROLE_ADMIN"
}'

# Register restaurant owner
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@pizzapalace.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Pizza"
    "role": "ROLE_RESTAURANT_OWNER"
  }'
  
  # Create restaurant 
curl -X POST http://localhost:8080/api/restaurants \
  -H "Authorization: Bearer $JWT_OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace",
    "description": "Best pizza in town",
    "address": "123 Main St, Pizza City",
    "phone": "+1234567890",
    "email": "contact@pizzapalace.com"
  }'

# Save the restaurant ID from response
RESTAURANT_ID="550e8400-e29b-41d4-a716-446655440000"

# Admin Approve Restaurant
ADMIN_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
# Add menu item
curl -X POST "http://localhost:8080/api/restaurants/$RESTAURANT_ID/menu" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Margherita Pizza",
    "description": "Classic Italian pizza with fresh mozzarella",
    "price": 14.99,
    "category": "MAIN_COURSE"
  }'
# Save the menu item ID from response
MENU_ITEM_ID="550e8400-e29b-41d4-a716-446655440001"

# Register customer
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Customer"
  }'

# Save the customer JWT token
JWT_CUSTOMER_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Place order - this will publish OrderPlacedEvent
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $JWT_CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "'$RESTAURANT_ID'",
    "deliveryAddress": "456 Customer St, Customer City",
    "customerNotes": "Please ring doorbell twice",
    "items": [
      {
        "menuItemId": "'$MENU_ITEM_ID'",
        "quantity": 2,
        "specialInstructions": "Extra cheese please"
      }
    ]
  }'

# Save the order ID from response
ORDER_ID="550e8400-e29b-41d4-a716-446655440002"

# Restaurant owner confirms order - this will publish OrderStatusChangedEvent
curl -X PUT "http://localhost:8080/api/orders/$ORDER_ID/status" \
  -H "Authorization: Bearer $JWT_OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'

# Update to preparing
curl -X PUT "http://localhost:8080/api/orders/$ORDER_ID/status" \
  -H "Authorization: Bearer $JWT_OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PREPARING"
  }'

# Update to ready
curl -X PUT "http://localhost:8080/api/orders/$ORDER_ID/status" \
  -H "Authorization: Bearer $JWT_OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "READY"
  }'

# Update to delivered
curl -X PUT "http://localhost:8080/api/orders/$ORDER_ID/status" \
  -H "Authorization: Bearer $JWT_OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DELIVERED"
  }'
```

## 3. Sample API Endpoints

Here are some of the key API endpoints and their required roles:

| Endpoint                                   | Method | Role Required                                                                                   | Description                                          |
|:-------------------------------------------|:-------|:------------------------------------------------------------------------------------------------|:-----------------------------------------------------|
| `/auth/register`                           | POST   | Public                                                                                          | Register a new user.                                 |
| `/auth/login`                              | POST   | Public                                                                                          | Authenticate and get JWT token.                      |
| `/api/restaurants`                         | GET    | Authenticated                                                                                   | Get a list of all restaurants.                       |
| `/api/restaurants`                         | POST   | `ROLE_RESTAURANT_OWNER`                                                                         | Create a new restaurant.                             |
| `/api/restaurants/menu-items/{menuItemId}` | GET    | Authenticated (internal call from Order Service)                                                | Get details of a specific menu item.                 |
| `/api/orders`                              | POST   | `ROLE_CUSTOMER`                                                                                 | Place a new food order.                              |
| `/api/orders/{id}`                         | GET    | Resource owner (`ROLE_CUSTOMER`), `ROLE_RESTAURANT_OWNER` (for their restaurants), `ROLE_ADMIN` | Get details of a specific order.                     |
| `/api/orders`                              | GET    | `ROLE_CUSTOMER`                                                                                 | Get all orders placed by the authenticated customer. |
| `/ap/restaurant/{restaurantId}`            | GET    | `ROLE_RESTAURANT_OWNER`, `ROLE_ADMIN`                                                           | Get all orders for a specific restaurant.            |
| `/auth/users`                              | GET    | `ROLE_ADMIN` only                                                                               | View all users (planned for admin service).          |

---
## 4. 📈 Monitoring
- Health Endpoints: /actuator/health
- Metrics: /actuator/metrics
- Info: /actuator/info

```bash
# Verify Kafka is running
docker logs bytebites-kafka

Check kafka UI Dashboard
# Access http://localhost:8090 to view topics and messages
```

# Kafka Ui to show message brokers in effect
![Kafka Dashboard](docs/images/Kafka%20UI.png)