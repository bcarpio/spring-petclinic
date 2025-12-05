# ADR-003: Controller Patterns

## Status

Accepted

## Context

We need consistent patterns for Spring MVC controllers that handle both web pages and REST API endpoints.

## Decision

Controllers use `@Controller` (not `@RestController`) and follow specific patterns for web vs API endpoints.

### Annotation Choice

Use `@Controller` for all controllers, even those with REST endpoints:

```java
@Controller
class VetController {
    // Web endpoint - returns view name
    @GetMapping("/vets.html")
    public String showVetList(Model model) {
        model.addAttribute("vets", vetRepository.findAll());
        return "vets/vetList";
    }

    // REST endpoint - uses @ResponseBody
    @GetMapping("/api/vets")
    @ResponseBody
    public Vets showResourcesVetList() {
        return new Vets(vetRepository.findAll());
    }
}
```

Do NOT use `@RestController` - we mix web and REST endpoints in the same controller.

### Constructor Injection

Always use constructor injection for dependencies:

```java
@Controller
class OwnerController {
    private final OwnerRepository owners;

    public OwnerController(OwnerRepository owners) {
        this.owners = owners;
    }
}
```

Do NOT use `@Autowired` on fields.

### Request Mapping Patterns

1. **Web endpoints**: Use `.html` suffix for clarity
   ```java
   @GetMapping("/owners/{ownerId}.html")
   @GetMapping("/owners/new")
   @PostMapping("/owners/{ownerId}/edit")
   ```

2. **REST/API endpoints**: Use `/api/` prefix
   ```java
   @GetMapping("/api/vets")
   @GetMapping("/api/stats/pets")
   ```

3. **Form handling**: GET for form display, POST for submission
   ```java
   @GetMapping("/owners/new")
   public String initCreationForm(Map<String, Object> model) { ... }

   @PostMapping("/owners/new")
   public String processCreationForm(@Valid Owner owner, BindingResult result) { ... }
   ```

### Model and View Patterns

1. **Pre-populate model data with @ModelAttribute**:
   ```java
   @ModelAttribute("owner")
   public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
       return ownerId == null ? new Owner() : owners.findById(ownerId).orElseThrow();
   }
   ```

2. **Configure data binding with @InitBinder**:
   ```java
   @InitBinder
   public void setAllowedFields(WebDataBinder dataBinder) {
       dataBinder.setDisallowedFields("id");
   }
   ```

3. **Use RedirectAttributes for flash messages**:
   ```java
   @PostMapping("/owners/{ownerId}/edit")
   public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result,
           @PathVariable("ownerId") int ownerId, RedirectAttributes redirectAttributes) {
       if (result.hasErrors()) {
           return "owners/createOrUpdateOwnerForm";
       }
       owner.setId(ownerId);
       owners.save(owner);
       redirectAttributes.addFlashAttribute("message", "Owner updated");
       return "redirect:/owners/{ownerId}";
   }
   ```

### Validation

1. **Use @Valid for Bean Validation**:
   ```java
   @PostMapping("/owners/new")
   public String processCreationForm(@Valid Owner owner, BindingResult result) {
       if (result.hasErrors()) {
           return "owners/createOrUpdateOwnerForm";
       }
       // ...
   }
   ```

2. **Custom validators via @InitBinder**:
   ```java
   @InitBinder("pet")
   public void initPetBinder(WebDataBinder dataBinder) {
       dataBinder.setValidator(new PetValidator());
   }
   ```

### REST API Response Patterns

1. **Use wrapper classes for collections** (not raw lists):
   ```java
   // Good - wrapper class
   @GetMapping("/api/vets")
   @ResponseBody
   public Vets showResourcesVetList() {
       return new Vets(vetRepository.findAll());
   }

   // Vets.java - wrapper class
   public class Vets {
       private List<Vet> vetList;
       // getters/setters
   }
   ```

2. **Simple POJOs for API responses** (not DTOs):
   ```java
   // Good - simple POJO in same package
   public class PetStatistics {
       private long totalPets;
       private Map<String, Long> petsByType;
       private double averageVisitsPerPet;
       // constructor, getters
   }
   ```

### Error Handling

1. **Use Spring's default error handling** - no custom `@ControllerAdvice` unless necessary
2. **Return appropriate view names for errors**
3. **For REST APIs, let Spring convert exceptions to JSON**

### Pagination

Use Spring Data's `Pageable` for paginated endpoints:

```java
@GetMapping("/owners")
public String processFindForm(@RequestParam(defaultValue = "1") int page,
        Owner owner, BindingResult result, Model model) {
    Page<Owner> ownersResults = owners.findByLastNameStartingWith(
        owner.getLastName(),
        PageRequest.of(page - 1, 5)
    );
    // ...
}
```

## Consequences

### Positive

- Consistent patterns across all controllers
- Clear separation between web and API endpoints
- Leverages Spring MVC conventions
- Easy to understand request flow

### Negative

- Mixing web and API in same controller can be confusing
- Need to remember `@ResponseBody` for API endpoints

## References

- Spring MVC documentation
- Spring PetClinic reference application patterns
