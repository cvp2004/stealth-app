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

#### Visual Diagrams

- [**System Architecture Diagram**](docs/Diagrams/System%20Architecture%20Diagram.png) - High-level system architecture and component relationships
- [**Database ER Diagram**](docs/Diagrams/Evently%20ER%20Diagram.png) - Complete entity-relationship diagram of the database schema
- [**Booking Workflow Diagram**](docs/Diagrams/Booking%20Workflow.png) - Step-by-step booking process flow

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

4. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Health Check: `http://localhost:8080/actuator/health`

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

## 📚 API Documentation

### Authentication

#### User APIs

All user API requests (except authentication) require the `X-User-ID` header with a valid user ID.

#### Admin APIs

All admin API requests require the `X-Admin-User` header set to `true`.

### Key Endpoints

#### User Authentication

- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - User login

#### Event Management

- `GET /api/v1/user/events` - Browse available events
- `GET /api/v1/user/events/{id}` - Get event details
- `GET /api/v1/user/shows/{id}/seats` - View available seats

#### Booking Process

- `POST /api/v1/user/bookings` - Create a booking (reserve seats)
- `POST /api/v1/user/bookings/payment` - Process payment
- `GET /api/v1/user/bookings` - View booking history
- `DELETE /api/v1/user/bookings/cancel` - Cancel booking

#### Admin Management

- `POST /api/v1/admin/venues` - Create venues
- `POST /api/v1/admin/events` - Create events
- `POST /api/v1/admin/shows` - Schedule shows
- `GET /api/v1/admin/users` - Manage users

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

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package
java -jar target/evently-0.0.1-SNAPSHOT.jar
```

## 📊 Monitoring and Health Checks

The application includes Spring Boot Actuator for monitoring:

- Health Check: `/actuator/health`
- Application Info: `/actuator/info`
- Metrics: `/actuator/metrics`

## 🔧 Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
spring:
  application:
    name: evently
  datasource:
    url: jdbc:postgresql://localhost:5432/evently
    username: user
    password: pass
  redis:
    host: localhost
    port: 6379
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  pagination:
    default-page-size: 50
```

---

**Evently** - Your complete event ticketing solution built with Spring Boot and modern Java technologies.
