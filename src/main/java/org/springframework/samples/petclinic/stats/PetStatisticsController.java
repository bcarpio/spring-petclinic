package org.springframework.samples.petclinic.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for pet statistics endpoints.
 * Provides aggregated data about pets in the clinic.
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class PetStatisticsController {

	private final PetRepository petRepository;

	/**
	 * Get aggregated statistics about pets in the clinic.
	 * Returns total count, pets grouped by type, and average visits per pet.
	 *
	 * @return ResponseEntity containing PetStatistics with HTTP 200 on success,
	 *         or HTTP 500 on database error
	 */
	@GetMapping("/pets")
	public ResponseEntity<PetStatistics> getPetStatistics() {
		try {
			log.debug("Fetching pet statistics");

			// Get total count of pets
			Long totalPets = petRepository.countAllPets();
			log.debug("Total pets: {}", totalPets);

			// Get pets grouped by type
			List<PetRepository.PetTypeCount> petTypeCounts = petRepository.countPetsByType();
			Map<String, Long> petsByType = new HashMap<>();
			for (PetRepository.PetTypeCount typeCount : petTypeCounts) {
				petsByType.put(typeCount.getTypeName(), typeCount.getCount());
			}
			log.debug("Pets by type: {}", petsByType);

			// Calculate average visits per pet
			Double averageVisitsPerPet = petRepository.calculateAverageVisitsPerPet();
			log.debug("Average visits per pet: {}", averageVisitsPerPet);

			// Build response
			PetStatistics statistics = PetStatistics.builder()
				.totalPets(totalPets.intValue())
				.petsByType(petsByType)
				.averageVisitsPerPet(averageVisitsPerPet)
				.build();

			log.info("Successfully retrieved pet statistics");
			return ResponseEntity.ok(statistics);

		} catch (Exception e) {
			log.error("Error retrieving pet statistics", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}