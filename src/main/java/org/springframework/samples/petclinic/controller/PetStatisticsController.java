package org.springframework.samples.petclinic.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.PetStatisticsDTO;
import org.springframework.samples.petclinic.service.PetStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for pet statistics endpoints.
 * Provides aggregated information about pets in the clinic.
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class PetStatisticsController {

    private final PetStatisticsService petStatisticsService;

    /**
     * Retrieves statistics about pets in the clinic.
     * 
     * @return ResponseEntity containing PetStatisticsDTO with total pets, 
     *         pets grouped by type, and average visits per pet
     */
    @GetMapping("/pets")
    public ResponseEntity<PetStatisticsDTO> getPetStatistics() {
        log.info("GET /api/stats/pets - Retrieving pet statistics");
        
        try {
            PetStatisticsDTO statistics = petStatisticsService.calculatePetStatistics();
            log.info("Successfully retrieved pet statistics: totalPets={}", statistics.getTotalPets());
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error retrieving pet statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}