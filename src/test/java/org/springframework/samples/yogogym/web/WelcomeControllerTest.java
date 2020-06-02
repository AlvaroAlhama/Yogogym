package org.springframework.samples.yogogym.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.yogogym.configuration.SecurityConfiguration;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = WelcomeController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
public class WelcomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void setup() {

	}

	@WithMockUser()
	@Test
	void testWelcomeControllerSuccessful() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("welcome"))
				.andExpect(model().attributeExists("quote", "apiFunctional"));
	}
	
	@WithMockUser()
	@Test
	void testWelcomeControllerSuccessful2() throws Exception {
		mockMvc.perform(get("/welcome")).andExpect(status().isOk()).andExpect(view().name("welcome"))
				.andExpect(model().attributeExists("quote", "apiFunctional"));
	}

}
