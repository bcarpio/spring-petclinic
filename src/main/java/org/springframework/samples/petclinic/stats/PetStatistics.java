package org.springframework.samples.petclinic.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response model for pet statistics.
 * Contains aggregated data about pets in the clinic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetStatistics {

	/**
	 * Total number of pets in the clinic
	 */
	private Integer totalPets;

	/**
	 * Count of pets grouped by pet type (e.g., "dog": 8, "cat": 5)
	 */
	private Map<String, Long> petsByType;

	/**
	 * Average number of visits per pet
	 */
	private Double averageVisitsPerPet;

}