# Code Generation Plan

**Issue:** #12 - [CPE-12] Add Pet Statistics API Endpoint
**Branch:** `12-cpe-12-add-pet-statistics-api-endpoint`
**Repository:** bcarpio/spring-petclinic
**Backend:** java
**Created:** 2025-12-04T23:08:19.834062

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
**Description:** Create a DTO to represent the pet statistics response with total count, pets by type map, and average visits per pet

**Files:**
- `src/main/java/com/petclinic/dto/PetStatisticsDTO.java`

**Completed:** 2025-12-04T23:08:27.902579

**Cost:** $0.006984 (993 input tokens, 267 output tokens)

---

### Step 2: Create PetStatisticsService ✅

**Status:** completed
**Description:** Create service class with business logic to calculate total pets, group by type, and compute average visits per pet

**Files:**
- `src/main/java/com/petclinic/service/PetStatisticsService.java`

**KB Queries:**
- Spring Data JPA aggregation queries
- JPA repository custom query methods

**Completed:** 2025-12-04T23:08:55.774971

**Cost:** $0.022437 (1309 input tokens, 1234 output tokens)

---

### Step 3: Create PetStatisticsController ✅

**Status:** completed
**Description:** Create REST controller with GET /api/stats/pets endpoint that delegates to PetStatisticsService and returns 200 with statistics

**Files:**
- `src/main/java/com/petclinic/controller/PetStatisticsController.java`

**Completed:** 2025-12-04T23:09:17.568997

**Cost:** $0.015597 (2544 input tokens, 531 output tokens)

---

### Step 4: Create unit tests for PetStatisticsService success cases ✅

**Status:** completed
**Description:** Create unit tests for service layer covering happy path scenarios: calculating total pets, grouping by type, and computing average visits

**Files:**
- `src/test/java/com/petclinic/service/PetStatisticsServiceTest.java`

**Completed:** 2025-12-04T23:09:49.016545

**Cost:** $0.060258 (3071 input tokens, 3403 output tokens)

---

### Step 5: Create unit tests for PetStatisticsService error handling ✅

**Status:** completed
**Description:** Create unit tests for service layer error scenarios: empty database, database connection failures, and null handling

**Files:**
- `src/test/java/com/petclinic/service/PetStatisticsServiceTest.java`

**Completed:** 2025-12-04T23:10:35.749421

**Cost:** $0.102663 (6066 input tokens, 5631 output tokens)

---

### Step 6: Create unit tests for PetStatisticsController success cases ✅

**Status:** completed
**Description:** Create controller unit tests using @WebMvcTest for successful GET requests returning 200 with correct statistics JSON

**Files:**
- `src/test/java/com/petclinic/controller/PetStatisticsControllerTest.java`

**Completed:** 2025-12-04T23:11:01.498523

**Cost:** $0.054879 (5438 input tokens, 2571 output tokens)

---

### Step 7: Create unit tests for PetStatisticsController error handling ⏳

**Status:** pending
**Description:** Create controller unit tests for error scenarios: service exceptions returning 500 status code

**Files:**
- `src/test/java/com/petclinic/controller/PetStatisticsControllerTest.java`

---

### Step 8: Create integration tests for pet statistics endpoint ⏳

**Status:** pending
**Description:** Create integration tests using @SpringBootTest to verify end-to-end functionality with real database interactions

**Files:**
- `src/test/java/com/petclinic/integration/PetStatisticsIntegrationTest.java`

**KB Queries:**
- Spring Boot test database configuration with H2

---

## Total Cost

**Total:** $0.262818
**Input Tokens:** 19,421
**Output Tokens:** 13,637