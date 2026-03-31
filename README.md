# HR Appraisal System - Backend

This repository encompasses the backend REST API for the HR Appraisal System. It manages the business logic, data persistence, security, and access control for the performance evaluation processes connecting Employees, Managers, and HR Administrators.

## 🚀 Tech Stack

The backend utilizes robust and enterprise-grade Java capabilities:

- **Java 17**: Core language ensuring modern features and LTS stability.
- **Spring Boot (v4.x)**: Handles autoconfiguration and dependency injection to drastically reduce boilerplate setup.
- **Spring Security & JWT**: Comprehensive role-based access control (RBAC). JSON Web Tokens handle stateless communication and secure API endpoint protection.
- **Spring Data JPA & Hibernate**: Simplifies database interactions, acting as the ORM layer mapped to SQL tables.
- **H2 Database (Runtime/Dev) / MySQL**: H2 is configured for rapid development, testing, and debugging, alongside the MySQL connector mapping for robust production pipelines.
- **Lombok**: Significantly reduces Java boilerplate (like Getters, Setters, Constructors) keeping entities and DTOs clean.
- **ModelMapper**: Maps entities seamlessly to Data Transfer Objects (DTOs) preventing internal structure exposure over the wire.
- **Jakarta Validation**: Executes rigid server-side property validations (e.g., `@NotNull`, `@Min`, `@Max`).
- **Spring Boot Mail**: Provides SMTP connectivity to handle email-based operations.

## 🏗️ Architecture & How It Works

### Flow & Core Mechanism
1. **Security & Filtering:**
   All incoming HTTP traffic is inspected by Spring Security filters. Endpoints labeled `/api/auth/**` are open (for login). Protected endpoints require a valid Bearer JWT. The JWT Filter validates the token structure, expiration, and extracts the Principal (user information) and authorities (Roles), injecting them into the `SecurityContext`.
2. **Controller Layer (`@RestController`):**
   Handles incoming HTTP requests, extracts parameters, and maps request bodies to DTOs.
3. **Service Layer (`@Service`):**
   This is the backbone of the application. Business rules are enforced here:
   - Verifying if an active appraisal cycle currently exists before accepting reviews.
   - Determining if a Manager owns the employee they are trying to review.
   - Transforming DTOs into Domain Entities using ModelMapper.
4. **Repository Layer (`@Repository`):**
   Spring Data JPA interfaces which provide powerful predefined CRUD operations and customized query methods based on method names (e.g., `findByEmployeeIdAndCycleId`).
5. **Database (H2 / MySQL):**
   Tables are auto-generated/updated based on `@Entity` definitions in Java classes, forming relationships like One-to-Many (Manager to Employees) and Many-to-One (Goals to Appraisals).

### Key Workflows:
- **Authentication**: Using `jjwt`, the system signs secure tokens on successful database checks.
- **Appraisal Cycle Management**: HR can initiate "Appraisal Cycles". These cycles define time bounds.
- **Assessments**: Employees access the current cycle to add self-ratings, while Managers subsequently review and provide final evaluation scores. 

## 🛠️ Getting Started

### Prerequisites
- JDK 17
- Maven (Embedded `mvnw` wrapper provided)
- MySQL Server (if running in production profile)

### Running the Application

1. **Using the Maven Wrapper:**
   Navigate to the root directory where `pom.xml` resides.
   ```bash
   ./mvnw spring-boot:run
   ```
2. **Database:**
   By default, you might connect to the H2 in-memory DB (accessible usually at `/h2-console` with credentials provided in `application.properties`). Ensure `application.properties` (or `application.yml`) is correctly wired for your MySQL setup if not using H2.
3. The API runs by default on port `8080`.

### Build & Package

To compile the code, execute tests, and package into a runnable `.jar` file:
```bash
./mvnw clean package
```
The resulting `.jar` file will be located in the `target/` directory and can be deployed in any environment using `java -jar <filename>.jar`.

## 🧠 Validations & Testing

- Server-side exceptions (Unauthorized, Not Found, Validation Failed) are managed gracefully by a Global Exception Handler (`@ControllerAdvice`), which bundles the errors into standardized readable JSON responses so the frontend can display them to the user.
- The `pom.xml` includes `spring-boot-starter-test` for invoking unit/integration evaluations locally before merges.
