package org.springframework.samples.petclinic.dto;

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
     * Total number of pets in the clinic
     */
    private Long totalPets;

    /**
     * Count of pets grouped by pet type (e.g., "dog": 8, "cat": 5)
     */
    private Map<String, Long> petsByType;

    /**
     * Average number of visits per pet
     */
    private Double averageVisitsPerPet;
}