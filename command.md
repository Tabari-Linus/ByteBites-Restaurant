

```
# Command to run the Spring Boot application
# To run the application, use the following commands:
cd servicename # Replace 'servicename' with the actual name of the service directory you want to run
./mvnw clean install
./mvnw clean package  
./mvnw spring-boot:run
```

```
# If you want to run the application in a specific profile, use:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# If you want to run the application with a specific port, use:
./mvnw spring-boot:run -Dserver.port=
```

```
# If you want to run the application with a specific profile and port, use:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8081
# If you want to run the application with a specific profile and port, use:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8081 -Dspring-boot.run.arguments="--customArgument=value"
# If you want to run the application with a specific profile, port, and custom arguments, use:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8081 -Dspring-boot.run.arguments="--customArgument=value"
```

```aiignore

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE DATABASE auth_db;
CREATE USER auth_user WITH PASSWORD 'auth_pass';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;


```


```bash
Docker Cmd
    ## delete all containers and their images
    docker-compose down --rmi all
    
    ## Run docker container and images in the background
    docker-compose up -d
    
    
    # Stop all services
docker-compose down

# Remove volumes to start fresh (this will delete existing data)
docker-compose down -v

# Optional: Remove orphaned volumes
docker volume prune -f

docker exec -it bytebites-postgres-notification psql -U mrlii -d postgres -c "SELECT datname FROM pg_database WHERE datname='notification_db';"

# connect to database
docker exec -it bytebites-postgres-notification psql -U mrlii -d notification_db

# check env vARS
docker exec -it bytebites-postgres-notification env | grep POSTGRES
```

```bash
# folder structure 
find src -name "*.java" | grep -i event
```