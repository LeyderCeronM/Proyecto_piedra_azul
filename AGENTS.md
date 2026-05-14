# AI Agent Instructions

This file is the **central router for all AI agent workflows** in this repository. All agents must follow the documented workflows, reference the ADRs, and use the skill registry.

---

## Project Context

| Attribute | Value |
|-----------|-------|
| **Project** | Medical Services Network |
| **Description** | Desktop application for managing medical appointments at Centro de Salud Piedra Azul |
| **Stack** | Java 21, JavaFX, PostgreSQL, Spring Boot, Maven |
| **Architecture** | Microservices (ADR-001) |
| **Process** | SDD + TDD |

---

## ADR Cross-Reference

All architectural and process decisions are documented in ADRs. Agents MUST reference the relevant ADRs before making decisions.

| ADR | Title | When to Reference |
|-----|-------|-------------------|
| **ADR-001** | Microservices Architecture | When creating or modifying services |
| **ADR-002** | Java as Primary Language | When making language-level decisions |
| **ADR-003** | Design Patterns (Factory, Observer, Strategy) | When creating entities, syncing views, or implementing persistence behaviors |
| **ADR-004** | SOLID Principles | When making design decisions — applies to all code |
| **ADR-005** | Spring Boot for Microservices | When creating or configuring services |
| **ADR-006** | PostgreSQL as Database | When configuring data persistence |
| **ADR-007** | JavaFX for Desktop UI | When implementing UI components |

---

## Agent Workflow: SDD → TDD → Verify → Archive

### Phase Flow

```
User Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ORCHESTRATOR                               │
│  (delegates to sub-agents, manages state, enforces workflow)    │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  SDD PHASES (spec-driven development)                          │
├─────────────────────────────────────────────────────────────────┤
│  1. Explore     → Investigate codebase, compare approaches     │
│  2. Proposal    → Define intent, scope, capabilities           │
│  3. Spec        → Write requirements + scenarios (Given/When) │
│  4. Design      → Document architecture decisions              │
│  5. Tasks       → Break down into actionable checklist          │
├─────────────────────────────────────────────────────────────────┤
│  6. Apply       → Implement code (TDD enforcement here)        │
│  7. Verify       → Validate against spec + run tests           │
│  8. Archive      → Sync deltas to main specs, close change     │
└─────────────────────────────────────────────────────────────────┘
```

### SDD Commands

| Command | Phase | Purpose | Agent |
|---------|-------|---------|-------|
| `/sdd-init` | Init | Initialize SDD context, detect stack, bootstrap persistence | sdd-init |
| `/sdd-explore <topic>` | Explore | Investigate codebase, think through feature | sdd-explore |
| `/sdd-new <change>` | Proposal | Create change proposal from exploration | sdd-propose |
| `/sdd-ff <name>` | Fast-Forward | proposal → specs → design → tasks in one pass | orchestrator |
| `/sdd-continue` | Continue | Resume next dependency-ready phase | orchestrator |
| `/sdd-apply [change]` | Apply | Implement tasks, enforce TDD | sdd-apply |
| `/sdd-verify [change]` | Verify | Validate implementation against spec | sdd-verify |
| `/sdd-archive [change]` | Archive | Sync deltas, close change | sdd-archive |

### TDD Enforcement

During **Apply phase**, TDD is enforced:

```
┌─────────────────────────────────────────────────────┐
│                 TDD CYCLE                           │
│                                                     │
│  RED    → Write FAILING test first                 │
│           Run → MUST fail (compilation or assert)  │
│                                                     │
│  GREEN  → Write MINIMUM code to pass                │
│           No abstraction, no "future-proofing"      │
│                                                     │
│  REFACTOR → Clean test + code                      │
│           Run → ALL tests pass                      │
│                                                     │
│  Repeat for each behavior                          │
└─────────────────────────────────────────────────────┘
```

**When to apply TDD:**
- All business logic (domain, service layer)
- Use cases and handlers
- Adapters and gateways
- NOT: getters/setters, constants, data classes, framework code

---

## Skill Registry

### Global Skills (from opencode/skills/)

These skills are **always available** and trigger automatically based on context.

| Skill | Trigger Phrases | Phase | Purpose |
|-------|-----------------|-------|---------|
| **sdd-init** | "sdd init", "iniciar sdd", "openspec init" | Init | Initialize SDD context, detect stack |
| **sdd-explore** | "/sdd-explore" | Explore | Investigate codebase, analyze options |
| **sdd-propose** | "/sdd-new", "/sdd-propose" | Proposal | Create change proposal |
| **sdd-spec** | "/sdd-spec" | Spec | Write requirements + scenarios |
| **sdd-design** | "/sdd-design" | Design | Document architecture decisions |
| **sdd-tasks** | "/sdd-tasks" | Tasks | Break into implementation checklist |
| **sdd-apply** | "/sdd-apply" | Apply | Implement with TDD enforcement |
| **sdd-verify** | "/sdd-verify" | Verify | Validate against spec + run tests |
| **sdd-archive** | "/sdd-archive" | Archive | Sync deltas, close change |
| **tdd-cycle** | "tdd", "test driven", "red green refactor", "escribir test primero" | Apply | Enforce Red→Green→Refactor |
| **issue-creation** | "create issue", "new issue", "report bug", "crear issue" | Any | Create GitHub issues |
| **branch-pr** | "create pr", "open pull request", "crear pr" | Any | Create PRs with issue linkage |

### Project Skills (from .claude/skills/)

| Skill | Trigger Phrases | Purpose |
|-------|-----------------|---------|
| **create-adr** | "create adr", "new adr", "architecture decision record", "crear adr" | Create ADR documents |
| **tdd-cycle** | "tdd", "test driven", "red green refactor" | TDD enforcement (duplicate for project) |
| **create-monolith-layer** | "create monolith layer", "create layered architecture" | Scaffold layered architecture |
| **uml-generator** | "generate uml", "create uml diagrams", "diagramas uml" | Generate modular PlantUML diagrams |
| **solid-principles** | "solid", "refactor", "clean code", "single responsibility", "open closed", "liskov" | Apply SOLID principles to code design |

---

## Artifact Persistence Modes

SDD artifacts can be stored in different backends:

| Mode | Storage | When to Use |
|------|---------|-------------|
| **engram** | Engram (memory DB) | Solo development, fast iteration |
| **openspec** | openspec/ directory | Team projects, git-friendly |
| **hybrid** | Both | Shareable + cross-session recovery |
| **none** | Return only | Ephemeral, no persistence |

**First action required:** Run `/sdd-init` to bootstrap the chosen mode.

---

## Conventions

See [docs/conventions.md](./docs/conventions.md) for full details.
Key points agents MUST follow:

### Documentation Headers

All source files MUST include the `@author` tag:
```java
/**
 * Short description.
 *
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
```

### Interface Naming

Use **`I` prefix** for interfaces: `IUserRepository`, `IUserCreationStrategy`.
Implementations are plain classes without suffix.

### Commit Format (Conventional Commits)

```
<type>(<scope>): <description>

Types: feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert
Examples:
  feat(users): add user creation endpoint
  fix(appointments): resolve scheduling conflict
  docs(uml): update entity diagram
```

### Testing (TDD)

- **Framework**: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- **Naming**: `should{Behavior}_when{Condition}`
- **Pattern**: AAA (Arrange-Act-Assert)

### Code Structure (ADR-001)

```
services/
├── api-gateway/          # Routing & auth (port 8080)
├── users-service/        # User management (port 8081)
├── appointments-service/
├── professionals-service/
├── clinical-records-service/
├── reports-service/
└── audits-service/

shared/
└── medical-common/       # Shared libraries
    ├── model/
    ├── dto/
    └── exceptions/
```

---

## Enforcement Rules

1. **SDD is mandatory** for non-trivial changes
   - Trivial changes: typo fixes, config tweaks, dependency bumps
   - All other changes: must follow Explore → Proposal → Spec → Design → Tasks → Apply → Verify → Archive

2. **TDD is enforced** during Apply phase
   - Must write failing test FIRST (RED)
   - Then write minimum code to pass (GREEN)
   - Then refactor (REFACTOR)
   - Skip this for UI components (integration tests instead)

3. **ADRs must be referenced** before architectural decisions
   - New database? → ADR-006 (PostgreSQL)
   - New service? → create new ADR

4. **Artifact traceability** must be maintained
   - All SDD phases produce artifacts (stored in engram/openspec)
   - All artifacts are linked via observation IDs or file paths
   - Verification report proves compliance

5. **Git workflow** follows branch-pr skill
   - Branch naming: `type/description`
   - PR must link approved issue
   - Conventional commits required

---

## Related Documents

- `docs/architecture/system-overview.md` — system vision
- `docs/specs/system.spec.md` — system specification
- `docs/specs/features/users.spec.md` — feature specs
- `docs/conventions.md` — coding conventions
- `docs/modeling/uml/` — modular PlantUML diagrams (use uml-generator skill)
- `.atl/skill-registry.md` — skill registry (delegators use this)
- `ADRs/` — all architectural decisions