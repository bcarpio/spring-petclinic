package org.springframework.samples.petclinic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.exception.StatisticsException;
import org.springframework.samples.petclinic.service.PetStatisticsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for PetStatisticsController - error handling scenarios.
 * Tests error cases including database errors and service exceptions.
 */
@WebMvcTest(PetStatisticsController.class)
class PetStatisticsControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetStatisticsService petStatisticsService;

    @Test
    void getPetStatistics_shouldReturn500WhenServiceThrowsStatisticsException() throws Exception {
        // Given
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new StatisticsException("Database error occurred"));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getPetStatistics_shouldReturn500WhenServiceThrowsGenericException() throws Exception {
        // Given
        when(petStatisticsService.calculatePetStatistics())
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}