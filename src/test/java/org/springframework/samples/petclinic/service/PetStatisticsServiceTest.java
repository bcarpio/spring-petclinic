package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.dto.PetStatisticsDTO;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PetStatisticsService - happy path scenarios.
 * Tests successful statistics calculation including total count,
 * pets by type grouping, and average visits calculation.
 */
@ExtendWith(MockitoExtension.class)
class PetStatisticsServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PetStatisticsService petStatisticsService;

    private List<Pet> testPets;

    @BeforeEach
    void setUp() {
        // Create test pet types
        PetType dogType = new PetType();
        dogType.setName("Dog");

        PetType catType = new PetType();
        catType.setName("Cat");

        PetType birdType = new PetType();
        birdType.setName("Bird");

        // Create test pets
        Pet dog1 = new Pet();
        dog1.setName("Max");
        dog1.setType(dogType);

        Pet dog2 = new Pet();
        dog2.setName("Buddy");
        dog2.setType(dogType);

        Pet dog3 = new Pet();
        dog3.setName("Charlie");
        dog3.setType(dogType);

        Pet cat1 = new Pet();
        cat1.setName("Whiskers");
        cat1.setType(catType);

        Pet cat2 = new Pet();
        cat2.setName("Mittens");
        cat2.setType(catType);

        Pet bird1 = new Pet();
        bird1.setName("Tweety");
        bird1.setType(birdType);

        testPets = Arrays.asList(dog1, dog2, dog3, cat1, cat2, bird1);
    }

    @Test
    void calculatePetStatistics_shouldReturnCorrectTotalCount() {
        // Given
        when(petRepository.findAll()).thenReturn(testPets);
        when(visitRepository.count()).thenReturn(14L);

        // When
        PetStatisticsDTO statistics = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalPets()).isEqualTo(6L);
    }

    @Test
    void calculatePetStatistics_shouldGroupPetsByTypeCorrectly() {
        // Given
        when(petRepository.findAll()).thenReturn(testPets);
        when(visitRepository.count()).thenReturn(14L);

        // When
        PetStatisticsDTO statistics = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(statistics).isNotNull();
        Map<String, Long> petsByType = statistics.getPetsByType();
        assertThat(petsByType).isNotNull();
        assertThat(petsByType).hasSize(3);
        assertThat(petsByType.get("dog")).isEqualTo(3L);
        assertThat(petsByType.get("cat")).isEqualTo(2L);
        assertThat(petsByType.get("bird")).isEqualTo(1L);
    }

    @Test
    void calculatePetStatistics_shouldCalculateAverageVisitsPerPetCorrectly() {
        // Given
        when(petRepository.findAll()).thenReturn(testPets);
        when(visitRepository.count()).thenReturn(14L);

        // When
        PetStatisticsDTO statistics = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getAverageVisitsPerPet()).isEqualTo(2.3);
    }
}