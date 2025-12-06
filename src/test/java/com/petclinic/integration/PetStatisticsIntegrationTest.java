package com.petclinic.integration;

import com.petclinic.dto.PetStatisticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Pet Statistics endpoint.
 * Tests end-to-end functionality with real database interactions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pet Statistics Integration Tests")
class PetStatisticsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/stats/pets";
    }

    @Test
    @DisplayName("Should return 200 with correct statistics for multiple pets and types")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/pets-with-visits.sql"})
    @Transactional
    void shouldReturn200WithCorrectStatisticsForMultiplePetsAndTypes() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isEqualTo(15L);
        assertThat(statistics.getPetsByType()).isNotNull();
        assertThat(statistics.getPetsByType().get("dog")).isEqualTo(8L);
        assertThat(statistics.getPetsByType().get("cat")).isEqualTo(5L);
        assertThat(statistics.getPetsByType().get("bird")).isEqualTo(2L);
        assertThat(statistics.getAverageVisitsPerPet()).isEqualTo(2.3);
    }

    @Test
    @DisplayName("Should return 200 with zero statistics when database is empty")
    @Sql(scripts = {"/test-data/cleanup.sql"})
    @Transactional
    void shouldReturn200WithZeroStatisticsWhenDatabaseIsEmpty() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isEqualTo(0L);
        assertThat(statistics.getPetsByType()).isEmpty();
        assertThat(statistics.getAverageVisitsPerPet()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 200 with correct statistics for single pet type")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/single-pet-type.sql"})
    @Transactional
    void shouldReturn200WithCorrectStatisticsForSinglePetType() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isEqualTo(5L);
        assertThat(statistics.getPetsByType()).hasSize(1);
        assertThat(statistics.getPetsByType().get("dog")).isEqualTo(5L);
        assertThat(statistics.getAverageVisitsPerPet()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 200 with correct average when pets have no visits")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/pets-without-visits.sql"})
    @Transactional
    void shouldReturn200WithCorrectAverageWhenPetsHaveNoVisits() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isGreaterThan(0L);
        assertThat(statistics.getPetsByType()).isNotEmpty();
        assertThat(statistics.getAverageVisitsPerPet()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 200 with correctly rounded average visits")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/pets-with-uneven-visits.sql"})
    @Transactional
    void shouldReturn200WithCorrectlyRoundedAverageVisits() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isEqualTo(3L);
        assertThat(statistics.getAverageVisitsPerPet()).isEqualTo(3.3);
    }

    @Test
    @DisplayName("Should return 200 with statistics for many different pet types")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/many-pet-types.sql"})
    @Transactional
    void shouldReturn200WithStatisticsForManyDifferentPetTypes() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isGreaterThan(0L);
        assertThat(statistics.getPetsByType()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(statistics.getPetsByType()).containsKeys("dog", "cat", "bird");
    }

    @Test
    @DisplayName("Should handle concurrent requests correctly")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/pets-with-visits.sql"})
    @Transactional
    void shouldHandleConcurrentRequestsCorrectly() throws InterruptedException {
        // Given
        int numberOfThreads = 5;
        Thread[] threads = new Thread[numberOfThreads];
        ResponseEntity<PetStatisticsDTO>[] responses = new ResponseEntity[numberOfThreads];

        // When - make concurrent requests
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.getForEntity(baseUrl, PetStatisticsDTO.class);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - all responses should be successful and consistent
        for (ResponseEntity<PetStatisticsDTO> response : responses) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalPets()).isEqualTo(15L);
            assertThat(response.getBody().getAverageVisitsPerPet()).isEqualTo(2.3);
        }
    }

    @Test
    @DisplayName("Should return consistent results across multiple calls")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/pets-with-visits.sql"})
    @Transactional
    void shouldReturnConsistentResultsAcrossMultipleCalls() {
        // When - make multiple sequential requests
        ResponseEntity<PetStatisticsDTO> response1 = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );
        ResponseEntity<PetStatisticsDTO> response2 = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );
        ResponseEntity<PetStatisticsDTO> response3 = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then - all responses should be identical
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        PetStatisticsDTO stats1 = response1.getBody();
        PetStatisticsDTO stats2 = response2.getBody();
        PetStatisticsDTO stats3 = response3.getBody();

        assertThat(stats1).isNotNull();
        assertThat(stats2).isNotNull();
        assertThat(stats3).isNotNull();

        assertThat(stats1.getTotalPets()).isEqualTo(stats2.getTotalPets()).isEqualTo(stats3.getTotalPets());
        assertThat(stats1.getAverageVisitsPerPet()).isEqualTo(stats2.getAverageVisitsPerPet()).isEqualTo(stats3.getAverageVisitsPerPet());
        assertThat(stats1.getPetsByType()).isEqualTo(stats2.getPetsByType()).isEqualTo(stats3.getPetsByType());
    }

    @Test
    @DisplayName("Should correctly aggregate data from large dataset")
    @Sql(scripts = {"/test-data/cleanup.sql", "/test-data/large-dataset.sql"})
    @Transactional
    void shouldCorrectlyAggregateDataFromLargeDataset() {
        // When
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(
                baseUrl,
                PetStatisticsDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isGreaterThan(50L);
        assertThat(statistics.getPetsByType()).isNotEmpty();
        assertThat(statistics.getAverageVisitsPerPet()).isGreaterThanOrEqualTo(0.0);
        
        // Verify sum of pets by type equals total pets
        long sumByType = statistics.getPetsByType().values().stream()
                .mapToLong(Long::longValue)
                .sum();
        assertThat(sumByType).isEqualTo(statistics.getTotalPets());
    }
}