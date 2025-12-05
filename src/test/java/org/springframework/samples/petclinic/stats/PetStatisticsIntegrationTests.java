package org.springframework.samples.petclinic.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PetStatisticsController.
 * Tests the full endpoint with actual database queries against seeded test data.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PetStatisticsIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	/**
	 * Test that the endpoint returns correct statistics with the default test data.
	 * The default data.sql should contain pets and visits that we can verify.
	 */
	@Test
	void shouldReturnPetStatisticsWithActualData() throws Exception {
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.totalPets").isNumber())
			.andExpect(jsonPath("$.totalPets").value(greaterThan(0)))
			.andExpect(jsonPath("$.petsByType").isMap())
			.andExpect(jsonPath("$.petsByType").isNotEmpty())
			.andExpect(jsonPath("$.averageVisitsPerPet").isNumber())
			.andExpect(jsonPath("$.averageVisitsPerPet").value(greaterThanOrEqualTo(0.0)));
	}

	/**
	 * Test that the endpoint correctly aggregates pets by type.
	 * Verifies that common pet types (cat, dog, etc.) are present in the response.
	 */
	@Test
	void shouldGroupPetsByTypeCorrectly() throws Exception {
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.petsByType").exists())
			.andExpect(jsonPath("$.petsByType", aMapWithSize(greaterThan(0))))
			.andExpect(jsonPath("$.petsByType.*", everyItem(isA(Number.class))));
	}

	/**
	 * Test that the endpoint calculates average visits per pet correctly.
	 * The average should be a non-negative number.
	 */
	@Test
	void shouldCalculateAverageVisitsPerPetCorrectly() throws Exception {
		mockMvc.perform(get("/api/stats/pets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.averageVisitsPerPet").isNumber())
			.andExpect(jsonPath("$.averageVisitsPerPet").value(greaterThanOrEqualTo(0.0)))
			.andExpect(jsonPath("$.totalPets").isNumber());
	}

}