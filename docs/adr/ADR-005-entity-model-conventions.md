# ADR-005: Entity and Model Conventions

## Status

Accepted

## Context

We need consistent conventions for JPA entities, model classes, and data structures in the Spring PetClinic application.

## Decision

### No DTOs - Use Entities Directly

Do NOT create separate DTO, Request, or Response classes. Use JPA entities directly:

```java
// Good - Entity used directly
@PostMapping("/owners/new")
public String processCreationForm(@Valid Owner owner, BindingResult result) {
    owners.save(owner);
    return "redirect:/owners/" + owner.getId();
}

// Bad - Unnecessary DTO
@PostMapping("/owners/new")
public String processCreationForm(@Valid OwnerDTO ownerDto, BindingResult result) {
    Owner owner = mapToEntity(ownerDto);  // Unnecessary mapping
    owners.save(owner);
}
```

### Simple POJOs for Non-Entity Data

For API responses that aggregate data (not persisted), use simple POJOs in the same domain package:

```java
// stats/PetStatistics.java - Simple POJO, not a DTO
public class PetStatistics {
    private long totalPets;
    private Map<String, Long> petsByType;
    private double averageVisitsPerPet;

    public PetStatistics(long totalPets, Map<String, Long> petsByType, double avgVisits) {
        this.totalPets = totalPets;
        this.petsByType = petsByType;
        this.averageVisitsPerPet = avgVisits;
    }

    // Getters only - immutable
    public long getTotalPets() { return totalPets; }
    public Map<String, Long> getPetsByType() { return petsByType; }
    public double getAverageVisitsPerPet() { return averageVisitsPerPet; }
}
```

### Wrapper Classes for Collections

For XML/JSON serialization of collections, use wrapper classes:

```java
// vet/Vets.java - Wrapper for List<Vet>
@XmlRootElement
public class Vets {
    private List<Vet> vets;

    public Vets() {
        this.vets = new ArrayList<>();
    }

    public Vets(Collection<Vet> vets) {
        this.vets = new ArrayList<>(vets);
    }

    @XmlElement
    public List<Vet> getVetList() {
        return vets;
    }
}
```

### Entity Inheritance Hierarchy

Use this base entity hierarchy:

```
BaseEntity (abstract)
├── id: Integer
├── isNew(): boolean
└── @MappedSuperclass

NamedEntity extends BaseEntity (abstract)
├── name: String
└── @MappedSuperclass

Person extends BaseEntity (abstract)
├── firstName: String
├── lastName: String
└── @MappedSuperclass
```

### Entity Class Structure

```java
@Entity
@Table(name = "owners")
public class Owner extends Person {

    // 1. Fields with JPA annotations
    @Column(name = "address")
    @NotBlank
    private String address;

    @Column(name = "city")
    @NotBlank
    private String city;

    @Column(name = "telephone")
    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Telephone must be 10 digits")
    private String telephone;

    // 2. Relationships
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    @OrderBy("name")
    private Set<Pet> pets = new LinkedHashSet<>();

    // 3. Getters and setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // 4. Relationship helper methods
    public void addPet(Pet pet) {
        if (pet.isNew()) {
            pets.add(pet);
        }
    }

    public Pet getPet(String name, boolean ignoreNew) {
        return pets.stream()
            .filter(p -> !ignoreNew || !p.isNew())
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
}
```

### Validation Annotations

Use Jakarta Bean Validation on entities:

```java
@NotBlank                           // Required string
@Size(max = 30)                     // Length constraint
@Pattern(regexp = "\\d{10}")        // Regex pattern
@DateTimeFormat(pattern = "yyyy-MM-dd")  // Date parsing
```

### Package Organization for Entities

Entities live in their domain package, NOT in a separate `model/` or `entity/` package:

```
owner/
├── Owner.java          # Entity
├── Pet.java            # Entity
├── PetType.java        # Entity
├── Visit.java          # Entity
├── OwnerController.java
└── OwnerRepository.java

model/
├── BaseEntity.java     # Abstract base only
├── NamedEntity.java    # Abstract base only
└── Person.java         # Abstract base only
```

### Do NOT Create

1. **DTO packages**: `dto/`, `request/`, `response/`
2. **DTO classes**: `OwnerDTO`, `OwnerRequest`, `OwnerResponse`
3. **Mapper classes**: `OwnerMapper`, `ModelMapper` configurations
4. **Value Objects** as separate classes (embed in entities)

### Exception for Statistics/Aggregates

For aggregate data that doesn't map to an entity, create a simple POJO in the domain package:

```java
// stats/PetStatistics.java
public class PetStatistics {
    // Immutable fields, constructor, getters only
}
```

This is NOT a DTO - it's a domain model representing calculated statistics.

## Consequences

### Positive

- Less boilerplate code
- No mapping logic between DTOs and entities
- Simpler codebase
- Entities serve as the single source of truth

### Negative

- Validation annotations on entities may become complex
- All entity fields are exposed in API responses
- Cannot have different views of the same data

### Mitigation

- Use `@JsonIgnore` to hide sensitive fields
- Use wrapper classes for collection responses
- Use Jackson annotations for custom serialization
- Create simple POJOs for aggregate data (not DTOs)

## References

- Jakarta Persistence (JPA) specification
- Spring Data JPA documentation
