# Code Generation Plan

**Issue:** #13 - [CPE-13] Add Pet Statistics API Endpoint
**Branch:** `13-cpe-13-add-pet-statistics-api-endpoint`
**Repository:** bcarpio/spring-petclinic
**Backend:** java
**Created:** 2025-12-05T01:45:55.322484

## Issue Description

h2. User Story

  \{noformat}
  As a clinic administrator,
  I want to see statistics about pets in the clinic,
  So that I can understand our patient demographics.
  \{noformat}

  h2. Endpoint

  Path: GET /api/stats/pets

  Response:
  \{code:json}
  {
    "totalPets": 15,
    "petsByType": {
      "dog": 8,
      "cat": 5,
      "bird": 2
    },
    "averageVisitsPerPet": 2.3
  }
  \{code}

  h2. Acceptance Criteria

* Returns total count of all pets
* Returns count of pets grouped by pet type
* Calculates average visits per pet
* Returns 200 on success
* Returns 500 on database error

  h2. Notes

  Follow existing controller and service patterns in the codebase.

## Cached Standards

<!-- Standards retrieved during plan generation (avoid re-querying) -->
<!-- STANDARDS_BEGIN -->

### Lambda Standards

None

### Terraform Standards

None

### Testing Standards

None

### Code Style Standards

None

<!-- STANDARDS_END -->

## Implementation Steps

### Step 1: Create PetStatistics response model ✅

**Status:** completed
**Description:** Create a simple POJO in the stats package to represent the pet statistics response with totalPets, petsByType map, and averageVisitsPerPet fields

**Files:**
- `src/main/java/org/springframework/samples/petclinic/stats/PetStatistics.java`

**Completed:** 2025-12-05T01:46:03.411883

**Cost:** $0.006798 (1006 input tokens, 252 output tokens)

---

### Step 2: Add custom query methods to PetRepository ✅

**Status:** completed
**Description:** Add @Query methods to PetRepository for counting pets by type using GROUP BY on pet type name

**Files:**
- `src/main/java/org/springframework/samples/petclinic/owner/PetRepository.java`

**KB Queries:**
- Spring Data JPA @Query with GROUP BY aggregation

**Completed:** 2025-12-05T01:46:33.876946

**Cost:** $0.015225 (1295 input tokens, 756 output tokens)

---

### Step 3: Add average visits calculation to VisitRepository ✅

**Status:** completed
**Description:** Add @Query method to VisitRepository to calculate average visits per pet using COUNT and division

**Files:**
- `src/main/java/org/springframework/samples/petclinic/owner/VisitRepository.java`

**KB Queries:**
- Spring Data JPA @Query for calculating averages

**Completed:** 2025-12-05T01:47:01.455564

**Cost:** $0.014928 (2046 input tokens, 586 output tokens)

---

### Step 4: Create PetStatisticsController with GET endpoint ✅

**Status:** completed
**Description:** Create controller in stats package with @GetMapping for /api/stats/pets that aggregates data from repositories and returns PetStatistics with @ResponseBody

**Files:**
- `src/main/java/org/springframework/samples/petclinic/stats/PetStatisticsController.java`

**Completed:** 2025-12-05T01:47:29.757963

**Cost:** $0.018861 (2642 input tokens, 729 output tokens)

---

### Step 5: Create unit tests for PetStatisticsController success cases 🔄

**Status:** in_progress
**Description:** Create controller unit tests using @WebMvcTest with @MockitoBean for repositories. Test successful GET request returns 200 with correct JSON structure and values (1-2 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/stats/PetStatisticsControllerTests.java`

---

### Step 6: Add unit tests for empty database scenario ⏳

**Status:** pending
**Description:** Add test methods to PetStatisticsControllerTests for edge case when no pets exist (returns zeros/empty map) and when no visits exist (1-2 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/stats/PetStatisticsControllerTests.java`

---

### Step 7: Add unit tests for repository error handling ⏳

**Status:** pending
**Description:** Add test methods to PetStatisticsControllerTests that mock repository exceptions and verify 500 status code is returned (1-2 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/stats/PetStatisticsControllerTests.java`

---

### Step 8: Create integration tests for statistics endpoint ⏳

**Status:** pending
**Description:** Create integration test class using @SpringBootTest and @AutoConfigureMockMvc to test the full endpoint with actual database queries against seeded test data (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/stats/PetStatisticsIntegrationTests.java`

---

## Total Cost

**Total:** $0.055812
**Input Tokens:** 6,989
**Output Tokens:** 2,323