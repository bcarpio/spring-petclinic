# ADR-001: Domain-Driven Package Structure

## Status

Accepted

## Context

We need to establish a consistent package organization strategy for the Spring PetClinic application that supports maintainability and aligns with domain-driven design principles.

## Decision

We will organize code by domain feature rather than by technical layer.

### Package Structure

```
org.springframework.samples.petclinic/
├── model/          # Base entity classes (BaseEntity, NamedEntity, Person)
├── owner/          # Owner domain: Owner, Pet, PetType, Visit + controllers + repositories
├── vet/            # Vet domain: Vet, Specialty + controller + repository
├── system/         # System concerns: configuration, error handling, welcome
└── PetClinicApplication.java
```

### Rules

1. **Group by domain, not by layer**: All classes related to a domain feature live in the same package
   - Controllers, entities, repositories, validators, formatters for a domain are co-located
   - Example: `owner/` contains `Owner.java`, `OwnerController.java`, `OwnerRepository.java`, `Pet.java`, `PetValidator.java`

2. **No separate layer packages**: Do NOT create these packages:
   - `controller/` - controllers go in their domain package
   - `service/` - no service layer (see ADR-002)
   - `repository/` - repositories go in their domain package
   - `dto/` - no DTOs (see ADR-005)
   - `exception/` - use Spring's built-in exception handling

3. **Base classes in `model/`**: Abstract base entities and shared utilities go in the `model/` package
   - `BaseEntity.java` - provides ID field
   - `NamedEntity.java` - provides name field
   - `Person.java` - provides firstName/lastName

4. **System concerns in `system/`**: Configuration and cross-cutting concerns
   - `CacheConfiguration.java`
   - `WebConfiguration.java`
   - `WelcomeController.java`
   - `CrashController.java` (for error page demonstration)

5. **New features get their own package**: When adding a new domain feature:
   - Create a new package at the same level as `owner/` and `vet/`
   - Place all related classes in that package
   - Example: Adding statistics feature -> create `stats/` package

### Example: Adding a New Feature

When adding a "Pet Statistics" feature:

```
org.springframework.samples.petclinic/
└── stats/
    ├── PetStatistics.java          # Model class (not DTO)
    ├── PetStatisticsController.java # Controller
    └── package-info.java           # Package documentation
```

## Consequences

### Positive

- High cohesion: related classes are together
- Easy to understand domain boundaries
- Simple navigation: find everything about "owners" in one place
- Supports vertical slicing for feature development

### Negative

- Unfamiliar to developers expecting layered architecture
- May need to import across packages for shared functionality

## References

- Domain-Driven Design by Eric Evans
- Package by Feature, not Layer (http://www.javapractices.com/topic/TopicAction.do?Id=205)
