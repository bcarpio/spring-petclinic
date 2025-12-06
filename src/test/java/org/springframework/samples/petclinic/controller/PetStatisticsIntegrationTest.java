package org.springframework.samples.petclinic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.PetStatisticsDTO;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Pet Statistics endpoint.
 * Tests end-to-end statistics calculation with real database
 * and verifies response structure and HTTP status codes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PetStatisticsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getPetStatistics_shouldReturnValidStatisticsWithRealDatabase() {
        // When
        String url = "http://localhost:" + port + "/api/stats/pets";
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(url, PetStatisticsDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics.getTotalPets()).isNotNull();
        assertThat(statistics.getTotalPets()).isGreaterThanOrEqualTo(0L);
        assertThat(statistics.getPetsByType()).isNotNull();
        assertThat(statistics.getAverageVisitsPerPet()).isNotNull();
        assertThat(statistics.getAverageVisitsPerPet()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void getPetStatistics_shouldReturnCorrectResponseStructure() {
        // When
        String url = "http://localhost:" + port + "/api/stats/pets";
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(url, PetStatisticsDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        
        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics).isNotNull();
        
        // Verify all required fields are present
        assertThat(statistics.getTotalPets()).isNotNull();
        assertThat(statistics.getPetsByType()).isNotNull();
        assertThat(statistics.getAverageVisitsPerPet()).isNotNull();
        
        // Verify petsByType is a valid map (can be empty if no pets)
        assertThat(statistics.getPetsByType()).isNotNull();
    }

    @Test
    void getPetStatistics_shouldCalculateStatisticsCorrectly() {
        // When
        String url = "http://localhost:" + port + "/api/stats/pets";
        ResponseEntity<PetStatisticsDTO> response = restTemplate.getForEntity(url, PetStatisticsDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PetStatisticsDTO statistics = response.getBody();
        assertThat(statistics).isNotNull();
        
        // Verify logical consistency
        long totalPets = statistics.getTotalPets();
        long sumOfPetsByType = statistics.getPetsByType().values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        // Total pets should equal sum of pets by type
        assertThat(sumOfPetsByType).isEqualTo(totalPets);
        
        // Average visits should be non-negative
        assertThat(statistics.getAverageVisitsPerPet()).isGreaterThanOrEqualTo(0.0);
        
        // If there are no pets, average should be 0.0
        if (totalPets == 0) {
            assertThat(statistics.getAverageVisitsPerPet()).isEqualTo(0.0);
        }
    }
}