package org.springframework.samples.petclinic.owner;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository class for <code>Pet</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface PetRepository extends Repository<Pet, Integer> {

	/**
	 * Retrieve all {@link PetType}s from the data store.
	 * @return a Collection of {@link PetType}s.
	 */
	@Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
	@Transactional(readOnly = true)
	List<PetType> findPetTypes();

	/**
	 * Retrieve a {@link Pet} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link Pet} if found
	 */
	@Transactional(readOnly = true)
	Pet findById(Integer id);

	/**
	 * Save a {@link Pet} to the data store, either inserting or updating it.
	 * @param pet the {@link Pet} to save
	 */
	void save(Pet pet);

	/**
	 * Count total number of pets in the clinic.
	 * @return total count of pets
	 */
	@Query("SELECT COUNT(p) FROM Pet p")
	@Transactional(readOnly = true)
	Long countAllPets();

	/**
	 * Count pets grouped by pet type name.
	 * @return list of objects containing pet type name and count
	 */
	@Query("SELECT p.type.name as typeName, COUNT(p) as count FROM Pet p GROUP BY p.type.name")
	@Transactional(readOnly = true)
	List<PetTypeCount> countPetsByType();

	/**
	 * Calculate average number of visits per pet.
	 * @return average visits per pet, or 0.0 if no pets exist
	 */
	@Query("SELECT COALESCE(AVG(SIZE(p.visits)), 0.0) FROM Pet p")
	@Transactional(readOnly = true)
	Double calculateAverageVisitsPerPet();

	/**
	 * Projection interface for pet type count query results.
	 */
	interface PetTypeCount {

		String getTypeName();

		Long getCount();

	}

}