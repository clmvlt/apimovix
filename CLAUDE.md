# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is ApiMovix, a Spring Boot 3.2.3 REST API for managing pharmacy delivery operations. It uses Java 17, PostgreSQL, and includes features for:
- Command/Order management and tracking
- Pharmacy information and location management
- Tour planning and optimization
- Package tracking with status history
- Anomaly reporting with photo uploads
- Email notifications and PDF generation
- Mobile app update management

## Development Commands

### Build and Run
```bash
# Clean and compile
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Build JAR file
./mvnw clean package

# Run tests
./mvnw test
```

### Database
- PostgreSQL database required (configured in application.properties)
- Database schema auto-updates via Hibernate DDL
- Connection: `jdbc:postgresql://192.168.1.151:5432/movix`

### API Documentation
- Swagger UI available at: `http://localhost:8081/swagger-ui.html`
- OpenAPI docs at: `/v3/api-docs`
- Status endpoint: `/check`

## Architecture

### Core Package Structure
```
bzh.stack.apimovix/
├── controller/          # REST endpoints organized by domain
├── service/            # Business logic layer
├── repository/         # Data access (JPA repositories)
├── model/             # JPA entities with status tracking
├── dto/               # Data Transfer Objects by domain
├── mapper/            # MapStruct entity-DTO mappers
├── config/            # Spring configuration (OpenAPI, cache, web)
├── interceptor/       # Token validation and request logging
├── exception/         # Custom exceptions and global handler
└── util/             # Utility classes (MAPIR responses, constants)
```

### Key Architectural Patterns
- **Domain-driven controllers**: Each controller handles a specific business domain (Pharmacy, Command, Tour, etc.)
- **Status tracking with history**: Commands, packages, and tours maintain status history via separate history entities
- **Custom response wrapper**: `MAPIR` utility provides standardized HTTP responses
- **Token-based authentication**: Custom token interceptor validates bearer tokens
- **File upload handling**: Supports image uploads organized by date structure in `/uploads`
- **MapStruct mapping**: Entity-DTO conversion handled by MapStruct mappers

### Security & Authentication
- Bearer token authentication via `TokenInterceptor`
- Role-based access control using custom annotations (`@AdminRequired`, `@TokenRequired`, etc.)
- Development token available in OpenAPI config for local testing

### Key Business Entities
- **Command**: Orders with status tracking, linked to pharmacies and tours
- **Pharmacy**: Location and contact information with coordinate data
- **Tour**: Delivery routes with optimization features
- **PackageEntity**: Individual packages within commands
- **Anomalie**: Issue reporting with photo attachments

## Development Notes

### Database Configuration
- Uses PostgreSQL with connection details in `application.properties`
- JPA entities use UUID primary keys
- Status entities track state changes with timestamps

### File Handling
- Image uploads stored in `/uploads` with date-based folder structure
- PDF generation using iText7 for reports
- File serving via custom endpoints

### External Integrations
- Email service configured for OVH SMTP
- Route optimization service integration
- QR code generation for tracking