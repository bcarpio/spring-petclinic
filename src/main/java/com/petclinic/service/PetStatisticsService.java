package com.petclinic.service;

import com.petclinic.dto.PetStatisticsDTO;
import com.petclinic.exception.StatisticsCalculationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for calculating pet statistics.
 * Provides business logic for aggregating pet data and visit information.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetStatisticsService {

    private final EntityManager entityManager;

    /**
     * Calculates comprehensive statistics about pets in the clinic.
     * 
     * @return PetStatisticsDTO containing total pets, pets by type, and average visits
     * @throws StatisticsCalculationException if there's an error calculating statistics
     */
    public PetStatisticsDTO calculatePetStatistics() {
        try {
            log.debug("Calculating pet statistics");

            Long totalPets = getTotalPets();
            Map<String, Long> petsByType = getPetsByType();
            Double averageVisitsPerPet = getAverageVisitsPerPet();

            PetStatisticsDTO statistics = PetStatisticsDTO.builder()
                    .totalPets(totalPets)
                    .petsByType(petsByType)
                    .averageVisitsPerPet(averageVisitsPerPet)
                    .build();

            log.info("Successfully calculated pet statistics: totalPets={}, petTypes={}, avgVisits={}", 
                    totalPets, petsByType.size(), averageVisitsPerPet);

            return statistics;
        } catch (Exception e) {
            log.error("Error calculating pet statistics", e);
            throw new StatisticsCalculationException("Failed to calculate pet statistics", e);
        }
    }

    /**
     * Gets the total count of all pets in the clinic.
     * 
     * @return total number of pets
     */
    private Long getTotalPets() {
        log.debug("Fetching total pet count");
        
        Query query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Pet p");
        
        Long count = (Long) query.getSingleResult();
        log.debug("Total pets: {}", count);
        
        return count;
    }

    /**
     * Gets the count of pets grouped by their type.
     * 
     * @return map of pet type to count
     */
    private Map<String, Long> getPetsByType() {
        log.debug("Fetching pets grouped by type");
        
        Query query = entityManager.createQuery(
                "SELECT p.type, COUNT(p) FROM Pet p GROUP BY p.type");
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        Map<String, Long> petsByType = new HashMap<>();
        for (Object[] result : results) {
            String type = (String) result[0];
            Long count = (Long) result[1];
            petsByType.put(type.toLowerCase(), count);
        }
        
        log.debug("Pets by type: {}", petsByType);
        
        return petsByType;
    }

    /**
     * Calculates the average number of visits per pet.
     * 
     * @return average visits per pet, rounded to 1 decimal place
     */
    private Double getAverageVisitsPerPet() {
        log.debug("Calculating average visits per pet");
        
        // Get total number of visits
        Query visitQuery = entityManager.createQuery(
                "SELECT COUNT(v) FROM Visit v");
        Long totalVisits = (Long) visitQuery.getSingleResult();
        
        // Get total number of pets
        Long totalPets = getTotalPets();
        
        // Calculate average
        if (totalPets == 0) {
            log.debug("No pets found, returning 0.0 for average visits");
            return 0.0;
        }
        
        BigDecimal average = BigDecimal.valueOf(totalVisits)
                .divide(BigDecimal.valueOf(totalPets), 1, RoundingMode.HALF_UP);
        
        Double result = average.doubleValue();
        log.debug("Average visits per pet: {} (total visits: {}, total pets: {})", 
                result, totalVisits, totalPets);
        
        return result;
    }
}