package com.petclinic.controller;

import com.petclinic.dto.PetStatisticsDTO;
import com.petclinic.exception.StatisticsCalculationException;
import com.petclinic.service.PetStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for pet statistics endpoints.
 * Provides endpoints to retrieve aggregated statistics about pets in the clinic.
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class PetStatisticsController {

    private final PetStatisticsService petStatisticsService;

    /**
     * Retrieves comprehensive statistics about pets in the clinic.
     * 
     * @return ResponseEntity containing PetStatisticsDTO with 200 status on success,
     *         or 500 status on database error
     */
    @GetMapping("/pets")
    public ResponseEntity<PetStatisticsDTO> getPetStatistics() {
        try {
            log.info("Received request for pet statistics");
            
            PetStatisticsDTO statistics = petStatisticsService.calculatePetStatistics();
            
            log.info("Successfully retrieved pet statistics");
            return ResponseEntity.ok(statistics);
            
        } catch (StatisticsCalculationException e) {
            log.error("Failed to calculate pet statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error while retrieving pet statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}