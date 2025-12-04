package com.petclinic.controller;

import com.petclinic.dto.PetStatisticsDTO;
import com.petclinic.exception.StatisticsCalculationException;
import com.petclinic.service.PetStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for PetStatisticsController.
 * Tests successful GET requests returning 200 with correct statistics JSON.
 */
@WebMvcTest(PetStatisticsController.class)
@DisplayName("PetStatisticsController Success Cases Tests")
class PetStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetStatisticsService petStatisticsService;

    @Test
    @DisplayName("Should return 200 with statistics for multiple pet types")
    void shouldReturn200WithStatisticsForMultiplePetTypes() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 8L);
        petsByType.put("cat", 5L);
        petsByType.put("bird", 2L);

        PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                .totalPets(15L)
                .petsByType(petsByType)
                .averageVisitsPerPet(2.3)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets", is(15)))
                .andExpect(jsonPath("$.petsByType.dog", is(8)))
                .andExpect(jsonPath("$.petsByType.cat", is(5)))
                .andExpect(jsonPath("$.petsByType.bird", is(2)))
                .andExpect(jsonPath("$.averageVisitsPerPet", is(2.3)));

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 200 with statistics for single pet type")
    void shouldReturn200WithStatisticsForSinglePetType() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 10L);

        PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                .totalPets(10L)
                .petsByType(petsByType)
                .averageVisitsPerPet(3.5)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets", is(10)))
                .andExpect(jsonPath("$.petsByType.dog", is(10)))
                .andExpect(jsonPath("$.averageVisitsPerPet", is(3.5)));

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 200 with zero statistics when no pets exist")
    void shouldReturn200WithZeroStatisticsWhenNoPetsExist() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();

        PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                .totalPets(0L)
                .petsByType(petsByType)
                .averageVisitsPerPet(0.0)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets", is(0)))
                .andExpect(jsonPath("$.petsByType").isEmpty())
                .andExpect(jsonPath("$.averageVisitsPerPet", is(0.0)));

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 200 with rounded average visits")
    void shouldReturn200WithRoundedAverageVisits() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("cat", 3L);

        PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                .totalPets(3L)
                .petsByType(petsByType)
                .averageVisitsPerPet(3.3)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets", is(3)))
                .andExpect(jsonPath("$.petsByType.cat", is(3)))
                .andExpect(jsonPath("$.averageVisitsPerPet", is(3.3)));

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 200 with statistics for many pet types")
    void shouldReturn200WithStatisticsForManyPetTypes() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 12L);
        petsByType.put("cat", 8L);
        petsByType.put("bird", 3L);
        petsByType.put("rabbit", 2L);
        petsByType.put("hamster", 1L);

        PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                .totalPets(26L)
                .petsByType(petsByType)
                .averageVisitsPerPet(4.2)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets", is(26)))
                .andExpect(jsonPath("$.petsByType.dog", is(12)))
                .andExpect(jsonPath("$.petsByType.cat", is(8)))
                .andExpect(jsonPath("$.petsByType.bird", is(3)))
                .andExpect(jsonPath("$.petsByType.rabbit", is(2)))
                .andExpect(jsonPath("$.petsByType.hamster", is(1)))
                .andExpect(jsonPath("$.averageVisitsPerPet", is(4.2)));

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 200 with zero average when pets have no visits")
    void shouldReturn200WithZeroAverageWhenPetsHaveNoVisits() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 5L);
        petsByType.put("cat", 3L);

        PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                .totalPets(8L)
                .petsByType(petsByType)
                .averageVisitsPerPet(0.0)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets", is(8)))
                .andExpect(jsonPath("$.petsByType.dog", is(5)))
                .andExpect(jsonPath("$.petsByType.cat", is(3)))
                .andExpect(jsonPath("$.averageVisitsPerPet", is(0.0)));

        verify(petStatisticsService).calculatePetStatistics();
    }

    // Error handling tests

    @Test
    @DisplayName("Should return 500 when service throws StatisticsCalculationException")
    void shouldReturn500WhenServiceThrowsStatisticsCalculationException() throws Exception {
        // Given
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new StatisticsCalculationException("Database error occurred"));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 500 when service throws unexpected RuntimeException")
    void shouldReturn500WhenServiceThrowsUnexpectedRuntimeException() throws Exception {
        // Given
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 500 when service throws NullPointerException")
    void shouldReturn500WhenServiceThrowsNullPointerException() throws Exception {
        // Given
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new NullPointerException("Null value encountered"));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 500 when service throws IllegalStateException")
    void shouldReturn500WhenServiceThrowsIllegalStateException() throws Exception {
        // Given
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new IllegalStateException("Invalid state"));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(petStatisticsService).calculatePetStatistics();
    }

    @Test
    @DisplayName("Should return 500 when service throws StatisticsCalculationException with cause")
    void shouldReturn500WhenServiceThrowsStatisticsCalculationExceptionWithCause() throws Exception {
        // Given
        Exception cause = new RuntimeException("Root cause");
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new StatisticsCalculationException("Failed to calculate statistics", cause));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(petStatisticsService).calculatePetStatistics();
    }
}