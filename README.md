# Taskmaster API

Simple REST API for managing users, projects, and tasks. Built with Spring Boot and secured with JWT.

## Features

* **Authentication:** Secure Signup & Login (JWT-based).
* **Project Management:** Create, update, view, and cancel projects.
* **Task Management:** Create, assign, update, and delete tasks.
* **Automation:**
  * Auto-completes projects when all tasks are done.
  * Background job monitors and marks overdue tasks.
* **Security:** Data isolation (Users can only access their own resources).

## Technologies

* **Java 17+**
* **Spring Boot 3** (Web, Data JPA, Security, Validation)
* **Oracle Database**
* **Docker** (for containerized Database)
* **Swagger/OpenAPI** (Documentation)

## Getting Started

### 1. Prerequisites

* Java 17 SDK installed.
* Maven installed.
* Docker Desktop installed and running.

### 2. Database Setup (Docker)

Spin up an Oracle Database container:

```bash
docker run -d --name taskmaster-oracle \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=SecretPassword123 \
  gvenzl/oracle-free

```

### 3. Database User Creation

Once the container is running, connect to it and create the application user:

```bash
# Enter the container
docker exec -it taskmaster-oracle sqlplus system/SecretPassword123

# Run inside SQLPlus:
CREATE USER taskmaster IDENTIFIED BY securePassword123;
GRANT CONNECT, RESOURCE, DBA TO taskmaster;
EXIT;

```

### 4. Configuration

Update `src/main/resources/application.yaml` with the following configuration:

```yaml
spring:
  application:
    name: taskmaster

  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/FREEPDB1
    username: taskmaster
    password: securePassword123
    driver-class-name: oracle.jdbc.OracleDriver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.OracleDialect

  data:
    web:
      pageable:
        one-indexed-parameters: true

jwt:
  secret: "your jwt secret"
  expiration: 7200000 # 2 hours in milliseconds

```

### 5. Run the Application

```bash
mvn spring-boot:run

```

## API Documentation

Once the app is running, access the interactive Swagger UI:

* **URL:** `http://localhost:8080/swagger-ui/index.html`