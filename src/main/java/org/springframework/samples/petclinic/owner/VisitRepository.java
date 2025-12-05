package org.springframework.samples.petclinic.owner;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository class for <code>Visit</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface VisitRepository extends Repository<Visit, Integer> {

	/**
	 * Save a <code>Visit</code> to the data store, either inserting or updating it.
	 * @param visit the <code>Visit</code> to save
	 * @see BaseEntity#isNew
	 */
	void save(Visit visit);

	/**
	 * Retrieve all {@link Visit}s for a particular {@link Pet}.
	 * @param petId the ID of the {@link Pet}
	 * @return a <code>List</code> of matching {@link Visit}s (or an empty
	 * <code>List</code> if none found)
	 */
	@Query("SELECT v FROM Visit v WHERE v.pet.id = :petId")
	@Transactional(readOnly = true)
	List<Visit> findByPetId(Integer petId);

	/**
	 * Calculate average number of visits per pet.
	 * Uses COUNT of visits divided by COUNT of distinct pets.
	 * @return average visits per pet, or 0.0 if no pets exist
	 */
	@Query("SELECT CASE WHEN COUNT(DISTINCT v.pet.id) > 0 THEN CAST(COUNT(v) AS double) / COUNT(DISTINCT v.pet.id) ELSE 0.0 END FROM Visit v")
	@Transactional(readOnly = true)
	Double calculateAverageVisitsPerPet();

}