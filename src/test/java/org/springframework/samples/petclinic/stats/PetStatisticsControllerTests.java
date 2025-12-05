package org.springframework.samples.petclinic.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PetStatisticsController.
 * Tests the success cases for the GET /api/stats/pets endpoint.
 */
@WebMvcTest(PetStatisticsController.class)
class PetStatisticsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PetRepository petRepository;

	@Test
	void shouldReturnPetStatisticsWithCorrectStructure() throws Exception {
		// Given
		Long totalPets = 15L;
		Double averageVisits = 2.3;

		PetRepository.PetTypeCount dogCount = new PetRepository.PetTypeCount() {
			@Override
			public String getTypeName() {
				return "dog";
			}

			@Override
			public Long getCount() {
				return 8L;
			}
		};

		PetRepository.PetTypeCount catCount = new PetRepository.PetTypeCount() {
			@Override
			public String getTypeName() {
				return "cat";
			}

			@Override
			public Long getCount() {
				return 5L;
			}
		};

		PetRepository.PetTypeCount birdCount = new PetRepository.PetTypeCount() {
			@Override
			public String getTypeName() {
				return "bird";
			}

			@Override
			public Long getCount() {
				return 2L;
			}
		};

		List<PetRepository.PetTypeCount> petTypeCounts = Arrays.asList(dogCount, catCount, birdCount);

		given(petRepository.countAllPets()).willReturn(totalPets);
		given(petRepository.countPetsByType()).willReturn(petTypeCounts);
		given(petRepository.calculateAverageVisitsPerPet()).willReturn(averageVisits);

		// When & Then
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.totalPets", is(15)))
			.andExpect(jsonPath("$.petsByType.dog", is(8)))
			.andExpect(jsonPath("$.petsByType.cat", is(5)))
			.andExpect(jsonPath("$.petsByType.bird", is(2)))
			.andExpect(jsonPath("$.averageVisitsPerPet", is(2.3)));
	}

	@Test
	void shouldReturnOkStatusWithEmptyStatistics() throws Exception {
		// Given
		Long totalPets = 0L;
		Double averageVisits = 0.0;
		List<PetRepository.PetTypeCount> petTypeCounts = Arrays.asList();

		given(petRepository.countAllPets()).willReturn(totalPets);
		given(petRepository.countPetsByType()).willReturn(petTypeCounts);
		given(petRepository.calculateAverageVisitsPerPet()).willReturn(averageVisits);

		// When & Then
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.totalPets", is(0)))
			.andExpect(jsonPath("$.petsByType").isEmpty())
			.andExpect(jsonPath("$.averageVisitsPerPet", is(0.0)));
	}

	@Test
	void shouldReturnZeroTotalPetsWhenNoPetsExist() throws Exception {
		// Given - empty database scenario
		Long totalPets = 0L;
		Double averageVisits = 0.0;
		List<PetRepository.PetTypeCount> petTypeCounts = Collections.emptyList();

		given(petRepository.countAllPets()).willReturn(totalPets);
		given(petRepository.countPetsByType()).willReturn(petTypeCounts);
		given(petRepository.calculateAverageVisitsPerPet()).willReturn(averageVisits);

		// When & Then
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.totalPets", is(0)))
			.andExpect(jsonPath("$.petsByType").isEmpty())
			.andExpect(jsonPath("$.averageVisitsPerPet", is(0.0)));
	}

	@Test
	void shouldReturnZeroAverageVisitsWhenNoVisitsExist() throws Exception {
		// Given - pets exist but no visits
		Long totalPets = 5L;
		Double averageVisits = 0.0;

		PetRepository.PetTypeCount dogCount = new PetRepository.PetTypeCount() {
			@Override
			public String getTypeName() {
				return "dog";
			}

			@Override
			public Long getCount() {
				return 3L;
			}
		};

		PetRepository.PetTypeCount catCount = new PetRepository.PetTypeCount() {
			@Override
			public String getTypeName() {
				return "cat";
			}

			@Override
			public Long getCount() {
				return 2L;
			}
		};

		List<PetRepository.PetTypeCount> petTypeCounts = Arrays.asList(dogCount, catCount);

		given(petRepository.countAllPets()).willReturn(totalPets);
		given(petRepository.countPetsByType()).willReturn(petTypeCounts);
		given(petRepository.calculateAverageVisitsPerPet()).willReturn(averageVisits);

		// When & Then
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.totalPets", is(5)))
			.andExpect(jsonPath("$.petsByType.dog", is(3)))
			.andExpect(jsonPath("$.petsByType.cat", is(2)))
			.andExpect(jsonPath("$.averageVisitsPerPet", is(0.0)));
	}

	@Test
	void shouldReturn500WhenCountAllPetsThrowsException() throws Exception {
		// Given - repository throws exception when counting all pets
		given(petRepository.countAllPets()).willThrow(new RuntimeException("Database connection error"));

		// When & Then
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isInternalServerError());
	}

	@Test
	void shouldReturn500WhenCountPetsByTypeThrowsException() throws Exception {
		// Given - repository throws exception when counting pets by type
		Long totalPets = 15L;
		given(petRepository.countAllPets()).willReturn(totalPets);
		given(petRepository.countPetsByType()).willThrow(new RuntimeException("Query execution failed"));

		// When & Then
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isInternalServerError());
	}

}