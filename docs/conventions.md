# Project Conventions

## Technology Stack
- **Language**: Java 21 (ADR-002)
- **Framework**: Spring Boot 3.2.5 (ADR-005)
- **Database**: PostgreSQL (ADR-006)
- **Messaging**: RabbitMQ (AMQP)
- **Architecture**: Microservices (ADR-001)
- **Design Patterns**: Factory Method, Strategy, Observer (ADR-003)
- **Testing**: JUnit 5 + Mockito + TestContainers

---

## Documentation Headers

### All source files MUST include an `@author` tag

```java
/**
 * Short description of the class/purpose.
 *
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
```

### Template for classes:
```java
/**
 * [What this class does — one or two sentences]
 * [Additional details if needed — optional]
 *
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
public class MyClass { ... }
```

### Template for methods:
```java
/**
 * [What this method does — imperative, third person]
 *
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws SomeException when/why this is thrown
 */
```

### Template for interfaces:
```java
/**
 * [Contract description — what implementors must fulfill]
 * [Design pattern or architectural role, if applicable]
 *
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
public interface IMyInterface { ... }
```

> **Note**: The `@author` tag identifies the engineer responsible for the file.
> It does NOT change when the file is modified — it credits the original creator.

---

## Naming

| Element | Convention | Example |
|---------|-----------|---------|
| Packages | lowercase | `com.medical.users` |
| Classes | PascalCase | `UserService`, `PatientCreationStrategy` |
| **Interfaces** | **PascalCase with `I` prefix** | **`IUserRepository`, `IUserCreationStrategy`** |
| Implementations | PascalCase (no suffix) | `PatientCreationStrategy` (not `Impl`) |
| Methods | camelCase | `createUser()` |
| Variables | camelCase | `userId` |
| Constants | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| Enums | PascalCase | `UserRole`, `AppointmentStatus` |
| Database tables | snake_case plural | `users`, `user_roles` |
| Database columns | snake_case | `created_at`, `full_name` |
| Test classes | Same as tested class + `Test` | `UserServiceTest` |
| Test methods | `should{Behavior}_when{Condition}` | `shouldCreateUser_whenValidData` |

---

## Code Structure (Microservice)

```
src/main/java/com/medical/
├── UsersServiceApplication.java        # Spring Boot entry point
├── config/                             # Spring configuration
│   ├── SecurityConfig.java
│   └── RabbitMQConfig.java
├── controller/                         # REST endpoints
│   └── UserController.java
├── dto/                                # Data Transfer Objects
│   ├── CreateUserRequest.java
│   ├── UpdateUserRequest.java
│   ├── UserResponse.java
│   └── PatientValidation*.java
├── entities/                           # JPA entities
│   ├── User.java
│   ├── Patient.java
│   └── Professional.java
├── enums/                              # Enumerations
│   └── UserRole.java
├── factory/                            # Design patterns (Factory, Strategy)
│   ├── IUserCreationStrategy.java
│   ├── PatientCreationStrategy.java
│   ├── UserCreationFactory.java
│   └── ...
├── messenger/                          # Async messaging (RabbitMQ)
│   └── PatientValidationListener.java
├── repository/                         # Spring Data JPA repositories
│   ├── IUserRepository.java
│   └── IPatientRepository.java
└── service/                            # Business logic
    └── UserService.java
```

---

## Code Rules

- **Controller layer**: Only HTTP handling, delegates to services
- **Service layer**: Business logic and orchestration
- **Repository layer**: Data access via Spring Data JPA — no raw SQL
- **Factory/Strategy layer**: Encapsulates creation logic by type/role
- Use interfaces with `I` prefix for service contracts and repositories
- Prefer constructor injection via `@RequiredArgsConstructor` (Lombok)
- Avoid static state except constants
- Keep methods small and focused (Single Responsibility)
- Use `Optional` instead of null for nullable returns
- Validate inputs at service layer
- Use `@Transactional` for write operations

---

## Database Rules

- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: `ddl-auto: update` for development
- **Queries**: Derived query methods in repository interfaces
- **Transactions**: Declarative via `@Transactional`
- **Connections**: Managed by HikariCP connection pool

---

## Error Handling

- Use `IllegalArgumentException` for validation errors (caught by `@ExceptionHandler` in controller)
- Business rule violations → `IllegalArgumentException` with descriptive message
- Not found cases → `IllegalArgumentException` with "not found" message
- Never throw generic `Exception` or `RuntimeException` directly
- Log errors at service/messenger layer via SLF4J (`@Slf4j`)

---

## Testing Rules

- **Framework**: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- **Pattern**: AAA (Arrange — Act — Assert)
- **Naming**: `should{Behavior}_when{Condition}`
- Unit tests mock dependencies, integration tests use TestContainers
- One test class per production class
- Minimum per public method: happy path + at least one failure case

---

## Git & Commit Rules

- **Commit format**: `<type>(<scope>): <description>`
- **Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
- **Branch naming**: `type/description` (e.g., `feat/user-creation`, `fix/duplicate-email`)
- Never commit: `target/`, `.env`, `*.db`

---

## SOLID Principles

This project follows SOLID principles for maintainable, scalable code.

**Full documentation**: See [ADR-004](/ADRs/ADR-004-solid-principles.md)

**Quick reference**:
- **SRP**: One reason to change per class
- **OCP**: Open for extension, closed for modification
- **LSP**: Subclasses substitutable for base
- **ISP**: Small, focused interfaces
- **DIP**: Depend on abstractions

---

## Design Patterns

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Strategy** | `factory/` | Role-based user creation — each role has its own creation logic |
| **Factory** | `factory/UserCreationFactory` | Resolves the correct strategy by `UserRole` |
| **Observer** | `event/` (future) | View synchronization (JavaFX desktop) |

---

## Related Documents

- `ADRs/` — Architecture Decision Records
- `docs/modeling/uml/INDEX.md` — UML diagram documentation
