package org.springframework.samples.petclinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.samples.petclinic.dto.PetStatisticsDTO;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating pet statistics.
 * Provides aggregated information about pets in the clinic.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PetStatisticsService {

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    /**
     * Calculates comprehensive statistics about pets in the clinic.
     *
     * @return PetStatisticsDTO containing total pets, pets by type, and average visits per pet
     */
    public PetStatisticsDTO calculatePetStatistics() {
        log.debug("Calculating pet statistics");

        List<Pet> allPets = petRepository.findAll();
        long totalPets = allPets.size();

        Map<String, Long> petsByType = calculatePetsByType(allPets);
        Double averageVisitsPerPet = calculateAverageVisitsPerPet(totalPets);

        log.debug("Pet statistics calculated: totalPets={}, petsByType={}, averageVisitsPerPet={}",
                totalPets, petsByType, averageVisitsPerPet);

        return PetStatisticsDTO.builder()
                .totalPets(totalPets)
                .petsByType(petsByType)
                .averageVisitsPerPet(averageVisitsPerPet)
                .build();
    }

    /**
     * Groups pets by their type and counts them.
     *
     * @param pets list of all pets
     * @return map of pet type name to count
     */
    private Map<String, Long> calculatePetsByType(List<Pet> pets) {
        return pets.stream()
                .collect(Collectors.groupingBy(
                        pet -> pet.getType().getName().toLowerCase(),
                        Collectors.counting()
                ));
    }

    /**
     * Calculates the average number of visits per pet.
     *
     * @param totalPets total number of pets in the clinic
     * @return average visits per pet, or 0.0 if no pets exist
     */
    private Double calculateAverageVisitsPerPet(long totalPets) {
        if (totalPets == 0) {
            return 0.0;
        }

        long totalVisits = visitRepository.count();
        return Math.round((double) totalVisits / totalPets * 10.0) / 10.0;
    }
}