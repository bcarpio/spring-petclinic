# ADR-002: No Service Layer Architecture

## Status

Accepted

## Context

Traditional enterprise Java applications use a three-tier architecture (Controller -> Service -> Repository). We need to decide whether to follow this pattern or adopt a simpler approach for PetClinic.

## Decision

We will NOT use a separate service layer. Controllers inject repositories directly.

### Architecture

```
Controller -> Repository -> Database
```

NOT:

```
Controller -> Service -> Repository -> Database
```

### Rules

1. **Inject repositories directly into controllers**:
   ```java
   @Controller
   class OwnerController {
       private final OwnerRepository owners;

       public OwnerController(OwnerRepository owners) {
           this.owners = owners;
       }
   }
   ```

2. **Do NOT create service interfaces or implementations**:
   - No `XxxService.java` interfaces
   - No `XxxServiceImpl.java` classes
   - No `service/` package

3. **Business logic placement**:
   - Simple validation: Use Bean Validation annotations on entities (`@NotBlank`, `@Size`)
   - Complex validation: Use custom `Validator` classes (e.g., `PetValidator`)
   - Data transformation: Do it in the controller or use a utility method
   - Query logic: Use repository methods with Spring Data query derivation

4. **When to add business logic classes**:
   - Validators: `PetValidator.java` for complex validation rules
   - Formatters: `PetTypeFormatter.java` for type conversion
   - These go in the same domain package, not in a separate `service/` package

### Example: Complex Query

Instead of a service method:

```java
// DON'T DO THIS
public class OwnerService {
    public Page<Owner> findByLastName(String name, Pageable pageable) {
        return ownerRepository.findByLastNameStartingWith(name, pageable);
    }
}
```

Put the query method directly in the repository:

```java
// DO THIS
public interface OwnerRepository extends JpaRepository<Owner, Integer> {
    Page<Owner> findByLastNameStartingWith(String lastName, Pageable pageable);
}
```

### Example: Business Logic

For aggregation or calculation logic that spans multiple repositories:

```java
// In the controller, not a service
@GetMapping("/api/stats/pets")
@ResponseBody
public PetStatistics getStatistics() {
    long totalPets = petRepository.count();
    Map<String, Long> byType = petRepository.countByType();
    double avgVisits = visitRepository.averageVisitsPerPet();

    return new PetStatistics(totalPets, byType, avgVisits);
}
```

Or add aggregation methods directly to the repository:

```java
public interface PetRepository extends JpaRepository<Pet, Integer> {
    @Query("SELECT p.type.name, COUNT(p) FROM Pet p GROUP BY p.type.name")
    List<Object[]> countByType();
}
```

## Consequences

### Positive

- Less boilerplate code
- Fewer files to maintain
- Simpler mental model
- Faster development for CRUD operations
- Repository methods are already transactional by default

### Negative

- Complex business logic may clutter controllers
- Harder to unit test complex business logic in isolation
- May need refactoring if business logic grows significantly

### Mitigation

If business logic becomes complex, extract it to:
- Validator classes for validation logic
- Static utility methods for calculations
- Custom repository implementations for complex queries

Do NOT create a service layer - instead, simplify the logic or split the domain.

## References

- YAGNI (You Aren't Gonna Need It)
- Spring Data JPA automatic transaction management
