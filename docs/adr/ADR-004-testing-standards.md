# ADR-004: Testing Standards

## Status

Accepted

## Context

We need consistent testing patterns for the Spring PetClinic application, covering controller tests, integration tests, and component tests.

## Decision

### Test Requirements by Component Type

Not all components require dedicated unit tests. The testing requirements vary by component type:

| Component Type | Dedicated Tests Required | Test Approach |
|---------------|-------------------------|---------------|
| `*Controller.java` | **Yes** | `@WebMvcTest` unit tests |
| `*Repository.java` | No | Tested via integration tests or implicitly via controller tests |
| `*Validator.java` | **Yes** | Unit tests with assertions |
| `*Formatter.java` | **Yes** | Unit tests |
| Entity/Model classes | No | Tested implicitly via controller/integration tests |
| Utility classes | **Yes** (if complex logic) | Unit tests |

**Rationale**: Spring Data JPA repositories are interfaces with generated implementations - there's no custom code to test. They are verified through integration tests that exercise the actual queries. Controllers contain HTTP handling logic that should be explicitly tested.

### Test Class Naming

Test classes use the plural `Tests` suffix (not `Test`):

```
OwnerControllerTests.java    # Correct
OwnerControllerTest.java     # Wrong
```

### Test Package Structure

Tests mirror the source package structure:

```
src/test/java/org/springframework/samples/petclinic/
├── owner/
│   ├── OwnerControllerTests.java
│   ├── PetControllerTests.java
│   ├── PetTypeFormatterTests.java
│   ├── PetValidatorTests.java
│   └── VisitControllerTests.java
├── vet/
│   ├── VetControllerTests.java
│   └── VetTests.java
├── model/
│   └── ValidatorTests.java
├── system/
│   ├── CrashControllerTests.java
│   └── CrashControllerIntegrationTests.java
└── service/
    ├── ClinicServiceTests.java
    └── EntityUtils.java
```

### Test Types and Annotations

1. **Controller Unit Tests** - Use `@WebMvcTest`:
   ```java
   @WebMvcTest(OwnerController.class)
   class OwnerControllerTests {
       @Autowired
       private MockMvc mockMvc;

       @MockitoBean
       private OwnerRepository owners;

       @Test
       void testInitCreationForm() throws Exception {
           mockMvc.perform(get("/owners/new"))
               .andExpect(status().isOk())
               .andExpect(view().name("owners/createOrUpdateOwnerForm"));
       }
   }
   ```

2. **Repository/Integration Tests** - Use `@DataJpaTest`:
   ```java
   @DataJpaTest
   class ClinicServiceTests {
       @Autowired
       private OwnerRepository owners;

       @Test
       void shouldFindOwnersByLastName() {
           Page<Owner> owners = this.owners.findByLastNameStartingWith(
               "Davis", PageRequest.of(0, 5));
           assertThat(owners).hasSize(2);
       }
   }
   ```

3. **Full Integration Tests** - Use `@SpringBootTest`:
   ```java
   @SpringBootTest
   @AutoConfigureMockMvc
   class CrashControllerIntegrationTests {
       @Autowired
       private MockMvc mockMvc;

       @Test
       void testTriggerException() throws Exception {
           mockMvc.perform(get("/oups"))
               .andExpect(status().isOk());
       }
   }
   ```

### Mocking Dependencies

Use `@MockitoBean` (not `@Mock` with `@InjectMocks`):

```java
@WebMvcTest(VetController.class)
class VetControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VetRepository vetRepository;

    @BeforeEach
    void setup() {
        Vet james = new Vet();
        james.setFirstName("James");
        james.setLastName("Carter");
        given(vetRepository.findAll()).willReturn(Lists.newArrayList(james));
    }
}
```

### Assertions

Use AssertJ assertions (not JUnit assertions):

```java
// Good - AssertJ
assertThat(owners).hasSize(2);
assertThat(owner.getFirstName()).isEqualTo("George");
assertThat(pets).extracting("name").containsExactly("Leo", "Basil");

// Avoid - JUnit
assertEquals(2, owners.size());
assertTrue(owners.contains(owner));
```

### MockMvc Patterns

1. **Test request and response**:
   ```java
   mockMvc.perform(get("/owners/new"))
       .andExpect(status().isOk())
       .andExpect(view().name("owners/createOrUpdateOwnerForm"))
       .andExpect(model().attributeExists("owner"));
   ```

2. **Test form submission**:
   ```java
   mockMvc.perform(post("/owners/new")
           .param("firstName", "Joe")
           .param("lastName", "Bloggs")
           .param("address", "123 Caramel Street")
           .param("city", "London")
           .param("telephone", "1316761638"))
       .andExpect(status().is3xxRedirection());
   ```

3. **Test validation errors**:
   ```java
   mockMvc.perform(post("/owners/new")
           .param("firstName", "")  // Empty - should fail validation
           .param("lastName", "Bloggs"))
       .andExpect(status().isOk())
       .andExpect(model().attributeHasErrors("owner"));
   ```

4. **Test JSON responses**:
   ```java
   mockMvc.perform(get("/api/vets"))
       .andExpect(status().isOk())
       .andExpect(content().contentType(MediaType.APPLICATION_JSON))
       .andExpect(jsonPath("$.vetList[0].firstName").value("James"));
   ```

### Test Data Setup

1. **Use `@BeforeEach` for test data**:
   ```java
   @BeforeEach
   void setup() {
       Owner george = new Owner();
       george.setId(1);
       george.setFirstName("George");
       george.setLastName("Franklin");
       given(owners.findById(1)).willReturn(Optional.of(george));
   }
   ```

2. **Use helper classes for complex entities**:
   ```java
   // EntityUtils.java in service package
   public class EntityUtils {
       public static <T extends BaseEntity> T getById(Collection<T> entities, int id) {
           return entities.stream()
               .filter(e -> e.getId().equals(id))
               .findFirst()
               .orElse(null);
       }
   }
   ```

### Test Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

This includes:
- JUnit 5
- Mockito
- AssertJ
- Spring Test / MockMvc
- JSONPath

## Consequences

### Positive

- Consistent test patterns across the codebase
- Fast controller tests with `@WebMvcTest`
- Proper isolation with `@MockitoBean`
- Readable assertions with AssertJ

### Negative

- Need to learn Spring-specific test annotations
- Multiple test annotation types to choose from

## References

- Spring Boot Testing documentation
- AssertJ documentation
