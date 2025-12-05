# Code Generation Plan

**Issue:** #12 - [CPE-12] Add Pet Statistics API Endpoint
**Branch:** `12-cpe-12-add-pet-statistics-api-endpoint`
**Repository:** bcarpio/spring-petclinic
**Backend:** java
**Created:** 2025-12-05T01:20:38.229252

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

### Step 1: Create PetStatistics DTO ✅

**Status:** completed
**Description:** Create a DTO to represent the pet statistics response with totalPets, petsByType map, and averageVisitsPerPet fields

**Files:**
- `src/main/java/org/springframework/samples/petclinic/dto/PetStatisticsDTO.java`

**Completed:** 2025-12-05T01:20:45.969537

**Cost:** $0.006771 (1002 input tokens, 251 output tokens)

---

### Step 2: Create PetStatisticsService interface and implementation ✅

**Status:** completed
**Description:** Create service layer to calculate pet statistics including total count, grouping by type, and average visits per pet

**Files:**
- `src/main/java/org/springframework/samples/petclinic/service/PetStatisticsService.java`

**KB Queries:**
- Spring Data JPA aggregation queries
- JPA group by and count operations

**Completed:** 2025-12-05T01:21:16.881125

**Cost:** $0.016395 (1300 input tokens, 833 output tokens)

---

### Step 3: Create PetStatisticsController ⏳

**Status:** pending
**Description:** Create REST controller with GET /api/stats/pets endpoint that returns PetStatisticsDTO

**Files:**
- `src/main/java/org/springframework/samples/petclinic/controller/PetStatisticsController.java`

---

### Step 4: Create custom exception for statistics errors ⏳

**Status:** pending
**Description:** Create StatisticsException for handling database errors during statistics calculation

**Files:**
- `src/main/java/org/springframework/samples/petclinic/exception/StatisticsException.java`

---

### Step 5: Create unit tests for PetStatisticsService - happy path ⏳

**Status:** pending
**Description:** Create unit tests for successful statistics calculation: test total count, pets by type grouping, and average visits calculation (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/service/PetStatisticsServiceTest.java`

---

### Step 6: Create unit tests for PetStatisticsService - error handling ⏳

**Status:** pending
**Description:** Create unit tests for database error scenarios: test exception handling when repository throws exceptions (1-3 test methods)

---

### Step 7: Create unit tests for PetStatisticsController - success cases ⏳

**Status:** pending
**Description:** Create controller tests for successful GET /api/stats/pets request: test 200 response, correct JSON structure, and proper service invocation (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/controller/PetStatisticsControllerTest.java`

---

### Step 8: Create unit tests for PetStatisticsController - error handling ⏳

**Status:** pending
**Description:** Create controller tests for error scenarios: test 500 response when service throws StatisticsException (1-2 test methods)

---

### Step 9: Create integration tests for pet statistics endpoint ⏳

**Status:** pending
**Description:** Create integration tests with @SpringBootTest: test end-to-end statistics calculation with real database and verify response structure (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/controller/PetStatisticsIntegrationTest.java`

---

## Total Cost

**Total:** $0.023166
**Input Tokens:** 2,302
**Output Tokens:** 1,084