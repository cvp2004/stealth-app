# Evently - Event Ticketing Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Evently is a comprehensive event management and ticketing platform built with Spring Boot. It provides a complete solution for managing venues, events, shows, and ticket bookings with support for payments, refunds, and email notifications.

## 🚀 Features

### Core Functionality

- **Event Management**: Create and manage events with categories and status tracking
- **Venue Management**: Set up venues with customizable seating arrangements
- **Show Scheduling**: Schedule multiple shows for events at different venues and times
- **Ticket Booking**: Real-time seat selection and booking system
- **Payment Processing**: Integrated payment handling with status tracking
- **Refund Management**: Automated refund processing for cancellations
- **Email Notifications**: Asynchronous email notifications for bookings and cancellations

### Technical Features

- **RESTful API**: Comprehensive REST API for both users and administrators
- **Database Migrations**: Flyway-based database schema management
- **Caching**: Redis integration for distributed locking and performance
- **Security**: Header-based authentication for users and admins
- **Validation**: Comprehensive input validation and error handling
- **Pagination**: Efficient data pagination for large datasets
- **Async Processing**: Background email processing with scheduling

## 📄 Documentation Index

- **Top-level**

  - [Concurrency Mechanisms Tradeoffs](docs/concurrency-mechanisms-tradeoffs.md)
  - [Evently - Challenge (PDF)](docs/Evently%20-%20Challenge.pdf)

- **API Docs** (`docs/api-docs`)

  - [API Specification](docs/api-docs/api-spec.md)
  - [User API](docs/api-docs/user-api.md)
  - [Admin API](docs/api-docs/admin-api.md)

- **Database** (`docs/database`)

  - [Complete Schema (SQL)](docs/database/complete_schema.sql)

- **Diagrams** (`docs/Diagrams`)
  - [System Architecture Diagram](docs/Diagrams/System%20Architecture%20Diagram.png)
  - [Evently ER Diagram](docs/Diagrams/Evently%20ER%20Diagram.png)
  - [Booking Workflow](docs/Diagrams/Booking%20Workflow.png)

## 🏗️ Architecture

### Technology Stack

- **Backend**: Spring Boot 3.5.5 with Java 21
- **Database**: PostgreSQL 14 with Flyway migrations
- **Cache**: Redis for distributed locking and caching
- **Build Tool**: Maven
- **Containerization**: Docker Compose

### Database Schema

The platform uses a relational database with the following core entities:

- **Venues**: Physical locations with seating capacity
- **Seats**: Individual seat definitions with pricing
- **Events**: Event information and metadata
- **Shows**: Scheduled performances linking events to venues
- **Users**: User account management
- **Bookings**: User reservations for shows
- **Tickets**: Individual seat tickets within bookings
- **Payments**: Payment transaction records
- **Refunds**: Refund processing records
- **Emails**: Asynchronous email queue

## Visual Diagrams

### System Arcitecture Diagram

<img src="docs/Diagrams/System%20Architecture%20Diagram.png" alt="System Architecture Diagram" width="600" />

### Database ER Diagram

<img src="docs/Diagrams/Evently%20ER%20Diagram.png" alt="Database ER Diagram" width="600" />

### Booking Workflow Diagram

<img src="docs/Diagrams/Booking%20Workflow.png" alt="Booking Workflow Diagram" width="600" />

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Docker and Docker Compose
- Git

### Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd evently
   ```

2. **Start the required services**

   ```bash
   docker-compose up -d
   ```

   This will start PostgreSQL and Redis services.

3. **Build and run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

   Or on Windows:

   ```cmd
   mvnw.cmd spring-boot:run
   ```

## 📚 API Documentation

### Authentication

#### User APIs

All user API requests (except authentication) require the `X-User-ID` header with a valid user ID.

#### Admin APIs

All admin API requests require the `X-Admin-User` header set to `true`.

### Key Endpoints

#### User Authentication

- `POST /api/v1/auth/signup` - Register a new user
- `POST /api/v1/auth/signin` - User login

#### Event Management

- `GET /api/v1/user/event/list` - Browse available events
- `GET /api/v1/user/event/{id}` - Get event details
- `GET /api/v1/user/show/{showId}/seats` - View available seats

#### Booking Process

- `POST /api/v1/user/booking` - Create a booking (reserve seats)
- `POST /api/v1/user/booking/payment` - Process payment
- `GET /api/v1/user/booking/list` - View booking history
- `DELETE /api/v1/user/booking/cancel` - Cancel booking

#### Admin Management

- `GET /api/v1/admin/venue/list` - Manage venues
- `GET /api/v1/admin/event/list` - Manage events
- `GET /api/v1/admin/show/event/{eventId}/list` - View shows for an event
- `GET /api/v1/admin/user/list` - Manage users

For complete API documentation, see [API Specification](docs/api-docs/api-spec.md).

## 🔄 Booking Workflow

1. **Seat Selection**: Users browse available seats for a show
2. **Reservation**: Seats are temporarily reserved for 5 minutes
3. **Payment**: Users complete payment within the reservation window
4. **Confirmation**: Successful payment confirms the booking
5. **Tickets**: Individual tickets are generated for each seat
6. **Notifications**: Email confirmations are sent asynchronously

## 🛠️ Development

### Project Structure

```
src/
├── main/
│   ├── java/com/chaitanya/evently/
│   │   ├── Application.java              # Main application class
│   │   ├── config/                       # Configuration classes
│   │   ├── controller/                   # REST controllers
│   │   │   ├── admin/                    # Admin-specific endpoints
│   │   │   └── user/                     # User-specific endpoints
│   │   ├── dto/                          # Data Transfer Objects
│   │   ├── exception/                    # Exception handling
│   │   ├── model/                        # JPA entities
│   │   ├── repository/                   # Data access layer
│   │   ├── service/                      # Business logic
│   │   └── util/                         # Utility classes
│   └── resources/
│       ├── application.yml               # Application configuration
│       └── db/migration/                 # Database migrations
└── test/                                 # Test classes
```

### Database Migrations

The application uses Flyway for database migrations. Migration files are located in `src/main/resources/db/migration/` and are automatically applied on startup.

### Running Project

```bash
./mvnw spring-boot:run
```

### Building for Production

```bash
./mvnw clean package
java -jar target/evently-0.0.1-SNAPSHOT.jar
```

## 🔧 Configuration

### Environment Configuration

The application uses environment variables for configuration. Key variables include:

```bash
# Database Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=evently
POSTGRES_USER=user
POSTGRES_PASSWORD=pass

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# Application Configuration
SPRING_APP_NAME=evently
DEFAULT_PAGE_SIZE=50
```

### Application Properties

Key configuration options in `application.yml`:

```yaml
spring:
  application:
    name: ${SPRING_APP_NAME:evently}

  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:evently}?options=-c%20TimeZone%3DAsia/Kolkata
    username: ${POSTGRES_USER:user}
    password: ${POSTGRES_PASSWORD:pass}
    driver-class-name: org.postgresql.Driver

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

  flyway:
    enabled: ${FLYWAY_ENABLED:true}
    locations: classpath:db/migration
    baseline-on-migrate: ${FLYWAY_BASELINE_ON_MIGRATE:true}
    table: ${FLYWAY_TABLE:flyway_schema_history}

logging:
  level:
    org.flywaydb: ${FLYWAY_LOG_LEVEL:INFO}
    org.springframework: ${SPRING_LOG_LEVEL:INFO}

app:
  pagination:
    default-page-size: ${DEFAULT_PAGE_SIZE:50}
```

---

**Evently** - Your complete event ticketing solution built with Spring Boot and modern Java technologies.
