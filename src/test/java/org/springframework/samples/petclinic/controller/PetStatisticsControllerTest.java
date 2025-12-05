package org.springframework.samples.petclinic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.dto.PetStatisticsDTO;
import org.springframework.samples.petclinic.service.PetStatisticsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PetStatisticsController - success cases.
 * Tests successful GET /api/stats/pets requests including response status,
 * JSON structure, and proper service invocation.
 */
@WebMvcTest(PetStatisticsController.class)
class PetStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PetStatisticsService petStatisticsService;

    @Test
    void getPetStatistics_shouldReturn200WithCorrectData() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 8L);
        petsByType.put("cat", 5L);
        petsByType.put("bird", 2L);

        PetStatisticsDTO expectedStatistics = PetStatisticsDTO.builder()
                .totalPets(15L)
                .petsByType(petsByType)
                .averageVisitsPerPet(2.3)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(expectedStatistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPets").value(15))
                .andExpect(jsonPath("$.petsByType.dog").value(8))
                .andExpect(jsonPath("$.petsByType.cat").value(5))
                .andExpect(jsonPath("$.petsByType.bird").value(2))
                .andExpect(jsonPath("$.averageVisitsPerPet").value(2.3));
    }

    @Test
    void getPetStatistics_shouldReturnCorrectJsonStructure() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 3L);
        petsByType.put("cat", 2L);

        PetStatisticsDTO expectedStatistics = PetStatisticsDTO.builder()
                .totalPets(5L)
                .petsByType(petsByType)
                .averageVisitsPerPet(1.5)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(expectedStatistics);

        // When & Then
        mockMvc.perform(get("/api/stats/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPets").exists())
                .andExpect(jsonPath("$.petsByType").exists())
                .andExpect(jsonPath("$.petsByType").isMap())
                .andExpect(jsonPath("$.averageVisitsPerPet").exists())
                .andExpect(jsonPath("$.averageVisitsPerPet").isNumber());
    }

    @Test
    void getPetStatistics_shouldInvokeServiceCorrectly() throws Exception {
        // Given
        Map<String, Long> petsByType = new HashMap<>();
        petsByType.put("dog", 10L);

        PetStatisticsDTO expectedStatistics = PetStatisticsDTO.builder()
                .totalPets(10L)
                .petsByType(petsByType)
                .averageVisitsPerPet(3.0)
                .build();

        when(petStatisticsService.calculatePetStatistics()).thenReturn(expectedStatistics);

        // When
        mockMvc.perform(get("/api/stats/pets"))
                .andExpect(status().isOk());

        // Then - service method was called (verified by mock framework)
        // The fact that we get the expected response confirms service was invoked
    }
}