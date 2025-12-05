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

### Step 3: Create PetStatisticsController ✅

**Status:** completed
**Description:** Create REST controller with GET /api/stats/pets endpoint that returns PetStatisticsDTO

**Files:**
- `src/main/java/org/springframework/samples/petclinic/controller/PetStatisticsController.java`

**Completed:** 2025-12-05T01:21:41.242612

**Cost:** $0.013689 (2123 input tokens, 488 output tokens)

---

### Step 4: Create custom exception for statistics errors ✅

**Status:** completed
**Description:** Create StatisticsException for handling database errors during statistics calculation

**Files:**
- `src/main/java/org/springframework/samples/petclinic/exception/StatisticsException.java`

**Completed:** 2025-12-05T01:22:04.994891

**Cost:** $0.012702 (2594 input tokens, 328 output tokens)

---

### Step 5: Create unit tests for PetStatisticsService - happy path ✅

**Status:** completed
**Description:** Create unit tests for successful statistics calculation: test total count, pets by type grouping, and average visits calculation (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/service/PetStatisticsServiceTest.java`

**Completed:** 2025-12-05T01:22:40.260534

**Cost:** $0.027327 (2944 input tokens, 1233 output tokens)

---

### Step 6: Create unit tests for PetStatisticsService - error handling ✅

**Status:** completed
**Description:** Create unit tests for database error scenarios: test exception handling when repository throws exceptions (1-3 test methods)

**Completed:** 2025-12-05T01:23:06.949484

**Cost:** $0.023226 (4137 input tokens, 721 output tokens)

---

### Step 7: Create unit tests for PetStatisticsController - success cases ✅

**Status:** completed
**Description:** Create controller tests for successful GET /api/stats/pets request: test 200 response, correct JSON structure, and proper service invocation (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/controller/PetStatisticsControllerTest.java`

**Completed:** 2025-12-05T01:23:37.925192

**Cost:** $0.031497 (4179 input tokens, 1264 output tokens)

---

### Step 8: Create unit tests for PetStatisticsController - error handling ✅

**Status:** completed
**Description:** Create controller tests for error scenarios: test 500 response when service throws StatisticsException (1-2 test methods)

**Completed:** 2025-12-05T01:24:03.828833

**Cost:** $0.025137 (5399 input tokens, 596 output tokens)

---

### Step 9: Create integration tests for pet statistics endpoint ⏳

**Status:** pending
**Description:** Create integration tests with @SpringBootTest: test end-to-end statistics calculation with real database and verify response structure (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/controller/PetStatisticsIntegrationTest.java`

---

## Total Cost

**Total:** $0.156744
**Input Tokens:** 23,678
**Output Tokens:** 5,714