package com.petclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for pet statistics.
 * Contains aggregated information about pets in the clinic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetStatisticsDTO {
    
    /**
     * Total number of pets in the clinic.
     */
    private Long totalPets;
    
    /**
     * Map of pet types to their counts.
     * Key: pet type (e.g., "dog", "cat", "bird")
     * Value: count of pets of that type
     */
    private Map<String, Long> petsByType;
    
    /**
     * Average number of visits per pet.
     */
    private Double averageVisitsPerPet;
}