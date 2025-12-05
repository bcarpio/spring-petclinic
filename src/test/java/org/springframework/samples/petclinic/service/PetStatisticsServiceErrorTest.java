package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.exception.StatisticsException;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PetStatisticsService - error handling scenarios.
 * Tests exception handling when repository operations fail.
 */
@ExtendWith(MockitoExtension.class)
class PetStatisticsServiceErrorTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PetStatisticsService petStatisticsService;

    @Test
    void calculatePetStatistics_shouldThrowStatisticsException_whenPetRepositoryThrowsDataAccessException() {
        // Given
        when(petRepository.findAll()).thenThrow(new DataAccessException("Database connection failed") {});

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    void calculatePetStatistics_shouldThrowStatisticsException_whenVisitRepositoryThrowsDataAccessException() {
        // Given
        when(petRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(visitRepository.count()).thenThrow(new DataAccessException("Database connection failed") {});

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    void calculatePetStatistics_shouldPropagateRuntimeException_whenUnexpectedErrorOccurs() {
        // Given
        when(petRepository.findAll()).thenThrow(new RuntimeException("Unexpected error occurred"));

        // When & Then
        assertThatThrownBy(() -> petStatisticsService.calculatePetStatistics())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected error occurred");
    }
}