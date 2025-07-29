# Waves - Heal Your Soul

A Spring Boot application for wellness and healing where people talk and share their experiences with mental health, wellness, and healing. The goal is to help each other heal and grow, not to drown in the waves of negativity, but to rise above them, one wave at a time.

Note: This is a work in progress and is not yet ready for production. 

Current features include:
- User registration and authentication [upcoming]
- User profile management [upcoming]
- Polls (to help people share their experiences and get feedback) [upcoming]
- Forum (to share a topic or story to discuss among users - more like reddit) [upcoming]
- And more to come...

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+

### Running the Application

1. **Clone and navigate to the project:**
   ```bash
   cd waves
   ```

2. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the application:**
   - Main application: http://localhost:8080
   - Health check: http://localhost:8080/health
   - H2 Database Console: http://localhost:8080/h2-console

### H2 Database Console Access
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `demo`
- **Password:** `demopassword`

## Development

### Project Structure
```
src/main/java/com/example/waves/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── entity/          # JPA entities
├── repository/      # Data repositories
├── service/         # Business logic
└── WavesApplication.java
```

### Key Features
- Spring Boot 3.5.4
- Spring Security
- Spring Data JPA
- H2 Database (in-memory)
- Lombok for boilerplate reduction

### Development Tools
- Spring DevTools (auto-restart)
- LiveReload enabled
- Comprehensive logging
- H2 Console for database inspection

## Configuration

The application uses the following configuration:
- **Database:** H2 in-memory
- **Security:** Basic Spring Security with form login
- **Port:** 8080
- **Logging:** DEBUG level for development

## Next Steps

1. Create your first entity in `src/main/java/com/example/waves/entity/`
2. Create repositories in `src/main/java/com/example/waves/repository/`
3. Add business logic in `src/main/java/com/example/waves/service/`
4. Create REST endpoints in `src/main/java/com/example/waves/controller/`

## Troubleshooting

- If you get port conflicts, change `server.port` in `application.properties`
- For database issues, check the H2 console at `/h2-console`
- Enable debug logging in `application.properties` for detailed logs 