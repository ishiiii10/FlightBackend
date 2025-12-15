# ✈️ Flight Booking System – Microservices Architecture (Spring Boot + MongoDB + Eureka + API Gateway + Config Server)
## 📌 Basic Architecture

```
                                        ┌──────────────────────────┐
                                        │        USER / UI         │
                                        │ (Postman / Frontend App) │
                                        └──────────────┬───────────┘
                                                       │
                      (1) Login / Signup               │
                                                       ▼
                                        ┌──────────────────────────┐
                                        │      API GATEWAY         │
                                        │  (Single Entry Point)    │
                                        └──────┬─────────┬────────┘
                                               │         │
                (Auth Requests)                │         │     (Flight & Booking APIs)
                                               │         │
                                               ▼         ▼
                              ┌────────────────────┐   ┌────────────────────┐
                              │    AUTH SERVICE    │   │   FLIGHT SERVICE   │
                              │  /auth/login       │   │  /flight/search    │
                              │  /auth/signup      │   │  /flight/inventory │
                              └──────┬─────────────┘   └──────┬─────────────┘
                                     │                         │
                                     │ JWT Token               │ MySql (flight_db)
                                     │                         │
                                     ▼                         ▼
                             ┌────────────────┐        ┌────────────────────┐
                             │   User DB      │        │  Flight DB         │
                             └────────────────┘        └────────────────────┘

                                               ▲
                                               │
                    (2) Booking API via Gateway │
                                               │
                                               ▼
                              ┌────────────────────┐
                              │  BOOKING SERVICE   │
                              │ /booking/{flightId}│
                              │ /ticket/{pnr}      │
                              │ /cancel/{pnr}      │
                              └──────┬─────────────┘
                                     │
                                     │ MySql (booking_db)
                                     ▼
                             ┌────────────────────┐
                             │ Booking DB         │
                             └────────────────────┘

                                     │
                                     │  (Internal Feign call)
                                     ▼
                          ┌────────────────────────────┐
                          │    FLIGHT SERVICE (again)  │
                          │ Validate flight, seats     │
                          └────────────────────────────┘

                                     │
                                     │ Publish Events to MQ
                                     ▼
          ┌────────────────────────────────────────────────────────────┐
          │                           RABBITMQ                         │
          │────────────────────────────────────────────────────────────│
          │  Exchange: booking.exchange                                 │
          │-------------------------------------------------------------│
          │  Queue 1: seat-booked-queue         (SeatBookedEvent)       │
          │  Queue 2: booking-confirmed-queue   (BookingConfirmedEvent) │
          │  Queue 3: seat-released-queue       (SeatReleasedEvent)     │
          └────────────────────────────────────────────────────────────┘

                    ▲                         ▲                        ▲
                    │                         │                        │
                    │                         │                        │
        (SeatBookedEvent)          (BookingConfirmedEvent)   (SeatReleasedEvent)
                    │                         │                        │
                    │                         │                        │
                    ▼                         ▼                        ▼

       ┌──────────────────────┐     ┌──────────────────────┐     ┌──────────────────────┐
       │   FLIGHT SERVICE     │     │   EMAIL SERVICE       │     │   FLIGHT SERVICE     │
       │ Update Seat Count    │     │ Sends Email Ticket    │     │ Restore Seat Count   │
       └──────────────────────┘     └──────────────────────┘     └──────────────────────┘

                                               │
                                               │
                                 (E-Ticket email sent to user)
                                               │
                                               ▼
                                    ┌────────────────────┐
                                    │     USER EMAIL      │
                                    └────────────────────┘
```

## 🚀 Features
```
   Microservice                  Responsibilities
 ---------------------------  -----------------------------------------------------
 Flight Service               Manage airlines, flights, and seat inventory
 Booking Service              Booking, cancellation, ticket generation, history
 API Gateway                  Single entry point, routing, JWT authentication
 Eureka Server                Service registry for service discovery
 Config Server                Centralized config for all microservices
 RabbitMQ Messaging           Event-based communication (booking → email service)
 Email Notification Service   Sends confirmation/cancellation emails
 MySQL Databases              Independent DB per microservice
 JWT Security                 Secured API Gateway with login + signup
 Docker & Docker Compose      Full containerized deployment of all services
```
## 🛠️ Tech Stack
```
   Layer/Component              Technology
 ---------------------------  -----------------------------------------------
 API Gateway                 Spring Cloud Gateway (WebMVC Mode)
 Service Discovery           Eureka Server (Netflix OSS)
 Config Management           Spring Cloud Config Server (Git-backed)
 Flight Service              Spring Boot 3, JPA, MySQL
 Booking Service             Spring Boot 3, JPA, MySQL
 Messaging                  RabbitMQ (AMQP)
 Auth & Security            Spring Security + JWT
 Inter-service Calls        OpenFeign + RestTemplate
 Resilience                 Resilience4j Circuit Breaker + Retry
 Testing                    JUnit 5, Mockito
 Build Tool                 Maven
 Containerization           Docker & Docker Compose
 Language                   Java 17                       
```
## 📂 Project Structure
```
Flight-Booking-System/
 ├── api-gateway/
 ├── booking-service/
 ├── flight-service/
 ├── email-service/
 ├── service-registry/
 ├── config-server/
 ├── docker-compose.yml
 └── README.md
```


## ⚙️ Setup & Installation
### 1️⃣ Clone the repository
git clone https://github.com/ishiiii10/Flight-Booking-Microservice-Architecture-integrating-Security-and-Docker
cd Flight-Booking-Microservice-Architecture-integrating-Security-and-Docker

### 2️⃣ Start MySql
MySQL URL: jdbc:mysql://localhost:3306
User: root
Pass: root (or your own)

## 📌 Microservice Configuration
### Eureka Server — application.properties
server.port=8761
spring.application.name=eureka-server
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false

### Config Server — application.properties
server.port=8888
spring.application.name=config-server
spring.cloud.config.server.git.uri=https://github.com/ishiiii10/config-server

### Run the application
mvn spring-boot:run

### 🧪 Running Tests
mvn test
Test coverage includes:
Flight Services Tests
Booking Service Tests

## 🧑‍💻 Available API Endpoints
| **Service**         | **Method** | **Endpoint**                | **Description**                  | **Auth Required** |
| ------------------- | ---------- | --------------------------- | -------------------------------- | ----------------- |
| **Auth Service**    | POST       | `/auth/signup`              | Register a new user              | ❌                 |
| **Auth Service**    | POST       | `/auth/login`               | Login & generate JWT token       | ❌                 |
| **Flight Service**  | POST       | `/flight/airline`           | Create a new airline             | ✔️                |
| **Flight Service**  | GET        | `/flight/airline/all`       | Get all airlines                 | ❌                 |
| **Flight Service**  | POST       | `/flight/airline/inventory` | Add flight inventory             | ✔️                |
| **Flight Service**  | POST       | `/flight/search`            | Search flights                   | ❌                 |
| **Flight Service**  | GET        | `/flight/{flightId}`        | Get flight by ID                 | ❌                 |
| **Booking Service** | POST       | `/booking/{flightId}`       | Book flight seats                | ✔️                |
| **Booking Service** | DELETE     | `/booking/cancel/{pnr}`     | Cancel a booking                 | ✔️                |
| **Booking Service** | GET        | `/booking/ticket/{pnr}`     | Get ticket details               | ✔️                |
| **Booking Service** | GET        | `/booking/history/{email}`  | Get booking history              | ✔️                |
| **Email Service**   | —          | *(RabbitMQ Event Listener)* | Sends booking confirmation email | ❌                 |

### 🧰 Email-Service
```
| Component   | Value                  |
| ----------- | ---------------------- |
| Exchange    | `booking-exchange`     |
| Queue       | `booking.email`        |
| Routing Key | `booking.notification` |
    
```

## 📌 Sonar Qube Summary
<img width="705" height="354" alt="Screenshot 2025-12-02 at 2 36 23 AM" src="https://github.com/user-attachments/assets/11e0b845-e269-4f24-9262-2b72803d9176" />

## 📌 Eureka Server Dashboard
<img width="1470" height="916" alt="Screenshot 2025-12-02 at 1 21 41 AM" src="https://github.com/user-attachments/assets/2d4577cc-3b9e-4906-9326-276f8e5d855f" />

## 📌 RabbitMq Dashboard
<img width="423" height="185" alt="Screenshot 2025-12-02 at 12 56 25 AM" src="https://github.com/user-attachments/assets/0dd0c8f6-e3e2-40a0-8518-74a27a38bea7" />
<img width="1470" height="680" alt="Screenshot 2025-12-02 at 2 39 10 AM" src="https://github.com/user-attachments/assets/d27f0222-823a-4e84-81aa-ae97b1957505" />






