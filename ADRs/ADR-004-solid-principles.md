# ADR-004: SOLID Design Principles

## Status
Accepted

## Date
2026-04-08

## Context

Medical Services Network needs a code design standard for maintainability, scalability, and testability. SOLID principles (Robert C. Martin) are the industry standard for object-oriented design.

The team applied DIP and OCP during repository development but without formal ADR documentation. This creates a traceability gap.

References:
- SOLID: https://en.wikipedia.org/wiki/SOLID_(object-oriented_design)
- SRP: https://refactoring.guru/smells/divergent-change
- OCP: https://refactoring.guru/who-is-the-driver
- LSP: https://refactoring.guru/liskov-substitution
- ISP: https://refactoring.guru/smells/parallel-inheritance-hierarchies
- DIP: https://refactoring.guru/who-wants-a-strong-coupling

## Decision

The 5 SOLID principles are adopted as design standard:

| Principle | Name | Application |
|-----------|------|------------|
| **S** | Single Responsibility | One reason to change per class |
| **O** | Open/Closed | Extension without modification |
| **L** | Liskov Substitution | Subclasses substitutable |
| **I** | Interface Segregation | Small interfaces |
| **D** | Dependency Inversion | Depend on abstractions |

### Project Implementation

| Principle | Implementation |
|-----------|---------------|
| **SRP** | `UserFactory` creates, `UserRepository` persists |
| **OCP** | `PersistenceStrategy` + strategies (ADR-003) |
| **LSP** | `DefaultUserFactory implements UserFactory` |
| **ISP** | `ConnectionProvider` (small interface) |
| **DIP** | `UserRepository` depends on `ConnectionProvider` (interface) |

## Rationale

- **Maintainability**: Isolated changes
- **Testability**: DIP enables mocking
- **OCP**: Extend without modifying via Strategy
- **Scalability**: New features without breaking
- **Communication**: Common team language

## Alternatives Considered

| Alternative | Why Rejected |
|------------|-------------|
| No documentation | Traceability gap, implicit decisions |
| Single ADR for patterns + principles | Clear separation: principles vs patterns |

## Consequences

### Positive
- Common language for design decisions
- Complete traceability
- Easier code review
- New members understand the standard

### Negative
- More documentation (worth vs benefit)

## Applies To

All code in `src/main/java/com/medical/`:
- `model/` — entities follow SRP
- `repository/` — DIP + OCP (Strategy)
- `service/` — DIP
- `factory/` — SRP, DIP
- `db/` — DIP (ConnectionProvider)

## Related

- ADR-001: Microservices Architecture (structure)
- ADR-002: Java as Primary Language
- ADR-003: Design Patterns (applies SOLID)
  - Factory → SRP, DIP
  - Observer → DIP
  - Strategy → OCP