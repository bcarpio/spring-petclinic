package com.petclinic.service;

import com.petclinic.dto.PetStatisticsDTO;
import com.petclinic.exception.StatisticsCalculationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PetStatisticsService.
 * Tests the happy path scenarios for calculating pet statistics.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PetStatisticsService Tests")
class PetStatisticsServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query totalPetsQuery;

    @Mock
    private Query petsByTypeQuery;

    @Mock
    private Query totalVisitsQuery;

    @InjectMocks
    private PetStatisticsService petStatisticsService;

    @BeforeEach
    void setUp() {
        // Common setup can be added here if needed
    }

    @Test
    @DisplayName("Should calculate statistics with multiple pets and types")
    void shouldCalculateStatisticsWithMultiplePetsAndTypes() {
        // Given
        Long totalPets = 15L;
        Long totalVisits = 35L;
        
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"Dog", 8L},
                new Object[]{"Cat", 5L},
                new Object[]{"Bird", 2L}
        );

        // Mock total pets count (called twice - once for total, once for average calculation)
        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        // Mock pets by type
        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        // Mock total visits
        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPets()).isEqualTo(15L);
        assertThat(result.getPetsByType()).hasSize(3);
        assertThat(result.getPetsByType().get("dog")).isEqualTo(8L);
        assertThat(result.getPetsByType().get("cat")).isEqualTo(5L);
        assertThat(result.getPetsByType().get("bird")).isEqualTo(2L);
        assertThat(result.getAverageVisitsPerPet()).isEqualTo(2.3);

        // Verify interactions
        verify(entityManager).createQuery("SELECT COUNT(p) FROM Pet p");
        verify(entityManager).createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type");
        verify(entityManager).createQuery("SELECT COUNT(v) FROM Visit v");
    }

    @Test
    @DisplayName("Should calculate statistics with single pet type")
    void shouldCalculateStatisticsWithSinglePetType() {
        // Given
        Long totalPets = 5L;
        Long totalVisits = 10L;
        
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"Dog", 5L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPets()).isEqualTo(5L);
        assertThat(result.getPetsByType()).hasSize(1);
        assertThat(result.getPetsByType().get("dog")).isEqualTo(5L);
        assertThat(result.getAverageVisitsPerPet()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Should handle zero pets correctly")
    void shouldHandleZeroPetsCorrectly() {
        // Given
        Long totalPets = 0L;
        Long totalVisits = 0L;
        List<Object[]> petTypeResults = Collections.emptyList();

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPets()).isEqualTo(0L);
        assertThat(result.getPetsByType()).isEmpty();
        assertThat(result.getAverageVisitsPerPet()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should calculate average visits with rounding")
    void shouldCalculateAverageVisitsWithRounding() {
        // Given
        Long totalPets = 3L;
        Long totalVisits = 10L; // 10/3 = 3.333... should round to 3.3
        
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"Cat", 3L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAverageVisitsPerPet()).isEqualTo(3.3);
    }

    @Test
    @DisplayName("Should handle pets with no visits")
    void shouldHandlePetsWithNoVisits() {
        // Given
        Long totalPets = 5L;
        Long totalVisits = 0L;
        
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"Dog", 3L},
                new Object[]{"Cat", 2L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPets()).isEqualTo(5L);
        assertThat(result.getAverageVisitsPerPet()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should convert pet types to lowercase")
    void shouldConvertPetTypesToLowercase() {
        // Given
        Long totalPets = 3L;
        Long totalVisits = 6L;
        
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"DOG", 1L},
                new Object[]{"Cat", 1L},
                new Object[]{"BIRD", 1L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPetsByType()).containsKeys("dog", "cat", "bird");
        assertThat(result.getPetsByType().get("dog")).isEqualTo(1L);
        assertThat(result.getPetsByType().get("cat")).isEqualTo(1L);
        assertThat(result.getPetsByType().get("bird")).isEqualTo(1L);
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @DisplayName("Should throw StatisticsCalculationException when database connection fails")
    void shouldThrowExceptionWhenDatabaseConnectionFails() {
        // Given
        when(entityManager.createQuery(anyString()))
                .thenThrow(new PersistenceException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics")
                .hasCauseInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException when total pets query fails")
    void shouldThrowExceptionWhenTotalPetsQueryFails() {
        // Given
        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenThrow(new PersistenceException("Query execution failed"));

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics")
                .hasCauseInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException when pets by type query fails")
    void shouldThrowExceptionWhenPetsByTypeQueryFails() {
        // Given
        Long totalPets = 10L;
        
        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenThrow(new PersistenceException("Group by query failed"));

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics")
                .hasCauseInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException when visits query fails")
    void shouldThrowExceptionWhenVisitsQueryFails() {
        // Given
        Long totalPets = 10L;
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"Dog", 5L},
                new Object[]{"Cat", 5L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenThrow(new PersistenceException("Visit count query failed"));

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics")
                .hasCauseInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException on null result from total pets query")
    void shouldThrowExceptionOnNullTotalPetsResult() {
        // Given
        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics");
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException on null result from pets by type query")
    void shouldThrowExceptionOnNullPetsByTypeResult() {
        // Given
        Long totalPets = 10L;
        
        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics");
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException on null result from visits query")
    void shouldThrowExceptionOnNullVisitsResult() {
        // Given
        Long totalPets = 10L;
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{"Dog", 5L},
                new Object[]{"Cat", 5L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics");
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException when EntityManager is null")
    void shouldThrowExceptionWhenEntityManagerIsNull() {
        // Given
        PetStatisticsService serviceWithNullEM = new PetStatisticsService(null);

        // When & Then
        assertThatThrownBy(() -> serviceWithNullEM.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics");
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException on unexpected runtime exception")
    void shouldThrowExceptionOnUnexpectedRuntimeException() {
        // Given
        when(entityManager.createQuery(anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle empty result list for pets by type gracefully")
    void shouldHandleEmptyPetsByTypeList() {
        // Given
        Long totalPets = 0L;
        Long totalVisits = 0L;
        List<Object[]> petTypeResults = Collections.emptyList();

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        when(entityManager.createQuery("SELECT COUNT(v) FROM Visit v"))
                .thenReturn(totalVisitsQuery);
        when(totalVisitsQuery.getSingleResult())
                .thenReturn(totalVisits);

        // When
        PetStatisticsDTO result = petStatisticsService.calculatePetStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPets()).isEqualTo(0L);
        assertThat(result.getPetsByType()).isEmpty();
        assertThat(result.getAverageVisitsPerPet()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should throw StatisticsCalculationException when pet type is null in result")
    void shouldThrowExceptionWhenPetTypeIsNull() {
        // Given
        Long totalPets = 5L;
        List<Object[]> petTypeResults = Arrays.asList(
                new Object[]{null, 5L}
        );

        when(entityManager.createQuery("SELECT COUNT(p) FROM Pet p"))
                .thenReturn(totalPetsQuery);
        when(totalPetsQuery.getSingleResult())
                .thenReturn(totalPets);

        when(entityManager.createQuery("SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type"))
                .thenReturn(petsByTypeQuery);
        when(petsByTypeQuery.getResultList())
                .thenReturn(petTypeResults);

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(StatisticsCalculationException.class)
                .hasMessageContaining("Failed to calculate pet statistics");
    }
}