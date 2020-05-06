package org.springframework.samples.yogogym.web;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.yogogym.configuration.SecurityConfiguration;
import org.springframework.samples.yogogym.model.Challenge;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Exercise;
import org.springframework.samples.yogogym.model.Inscription;
import org.springframework.samples.yogogym.model.User;
import org.springframework.samples.yogogym.model.Enums.Status;
import org.springframework.samples.yogogym.service.ChallengeService;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.ExerciseService;
import org.springframework.samples.yogogym.service.InscriptionService;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(value = InscriptionController.class,
excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,classes = WebSecurityConfigurer.class),
excludeAutoConfiguration= SecurityConfiguration.class)
public class InscriptionControllerTest {
	
	
	private static final int testChallengeId1 = 1;
	private static final int testChallengeId2 = 2;
	private static final int testInscriptionId1 = 1;
	private static final int testInscriptionId2 = 2;
	private static final int testClientId1 = 1;
	private static final int testClientId2 = 2;
	private static final String testClientUsername1 = "client1";
	private static final String testClientUsername2 = "client2";
	
	
	private static final Calendar testInitialDate = Calendar.getInstance();
	private static final Calendar testEndDate = Calendar.getInstance();
	
	
	@MockBean
	private ChallengeService challengeService;
	@MockBean
	private ExerciseService exerciseService;
	@MockBean
	private InscriptionService inscriptionService;
	@MockBean
	private ClientService clientService;
	@Autowired
	private MockMvc mockMvc;
	
	@BeforeEach
	void setUp()
	{	
		testEndDate.add(Calendar.DAY_OF_MONTH, 1);
		Date initialDate = testInitialDate.getTime();
		Date endDate = testEndDate.getTime();
		
		// Challenge 1:
		Challenge challenge1 = createChallenge(testChallengeId1, "Challenge1 Name Test", initialDate, endDate);
		
		given(this.challengeService.findChallengeById(testChallengeId1)).willReturn(challenge1);
		
		// Challenge 2:
		Challenge challenge2 = createChallenge(testChallengeId2, "Challenge2 Name Test", initialDate, endDate);

		given(this.challengeService.findChallengeById(testChallengeId2)).willReturn(challenge2);
		
		// Inscription 1 (Submitted):
		
		Inscription inscription1 = new Inscription();
		inscription1.setChallenge(challenge1);
		inscription1.setId(testInscriptionId1);
		inscription1.setStatus(Status.SUBMITTED);
		
		given(this.inscriptionService.findInscriptionByInscriptionId(testInscriptionId1)).willReturn(inscription1);
		
		// Inscription 2 (Submitted):

		Inscription inscription2 = new Inscription();
		inscription2.setChallenge(challenge1);
		inscription2.setId(testInscriptionId2);
		inscription2.setStatus(Status.PARTICIPATING);

		given(this.inscriptionService.findInscriptionByInscriptionId(testInscriptionId2)).willReturn(inscription2);
		
		// Client 1:
		Client client1 = createClient(testClientId1,testClientUsername1);
		client1.addInscription(inscription1);
		
		given(this.clientService.findClientByInscriptionId(testInscriptionId1)).willReturn(client1);
		given(this.clientService.findClientByUsername(testClientUsername1)).willReturn(client1);
		
		// Client 2:
		Client client2 = createClient(testClientId2,testClientUsername2);
		client2.addInscription(inscription2);
		
		given(this.clientService.findClientByInscriptionId(testInscriptionId2)).willReturn(client2);
		given(this.clientService.findClientByUsername(testClientUsername2)).willReturn(client2);
		
		
		List<Client> clients = new ArrayList<Client>();
		clients.add(client1);
		clients.add(client2);
		given(this.clientService.findClientsWithOnlySubmittedInscriptions()).willReturn(clients);
		
	}
	
	
	// ADMIN:
	
	@WithMockUser(value = "admin1", authorities = {"admin"})
	@Test
	void testListSubmittedInscriptionsAdmin() throws Exception {
		mockMvc.perform(get("/admin/inscriptions/submitted")).andExpect(status().isOk()).andExpect(model().attributeExists("clients"))
			.andExpect(view().name("admin/challenges/submittedInscriptionsList"));
	}
	
	@WithMockUser(value = "admin1", authorities = {"admin"})
	@Test
	void testShowSubmittedInscriptionByIdAdmin() throws Exception {
		
		mockMvc.perform(get("/admin/inscriptions/submitted/{inscriptionId}", testInscriptionId1)).andExpect(status().isOk())
				.andExpect(model().attribute("inscription", hasProperty("id", is(testInscriptionId1))))
				.andExpect(model().attribute("inscription", hasProperty("status", is(Status.SUBMITTED))))
				.andExpect(view().name("admin/challenges/submittedChallengeDetails"));
	}
	
	@WithMockUser(value = "admin1", authorities = {"admin"})
	@Test
	void testShowSubmittedInscriptionByIdAdminNotSubmitted() throws Exception {
		
		mockMvc.perform(get("/admin/inscriptions/submitted/{inscriptionId}", testInscriptionId2)).andExpect(status().isOk())
				.andExpect(view().name("exception"));
	}
	
	@WithMockUser(value = "admin1", authorities = {"admin"})
	@Test
	void testEvaluateChallengeAdmin() throws Exception
	{						
		
		mockMvc.perform(post("/admin/challenges/submitted/{challengeId}/inscription/{inscriptionId}/evaluate",testChallengeId1,testInscriptionId1)
			.with(csrf())
			.param("status", String.valueOf(Status.COMPLETED)))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/admin/inscriptions/submitted"));
	}
	
	
	// CLIENT:
	
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testcreateInscriptionByChallengeId() throws Exception
	{						
		
		mockMvc.perform(get("/client/{clientUsername}/challenges/{challengeId}/inscription/create",testClientUsername1,testChallengeId2))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/client/"+testClientUsername1+"/challenges"));
	}
	
	@WithMockUser(value = "client2", authorities = {"client"})
	@Test
	void testsubmitInscription() throws Exception
	{						
		
		mockMvc.perform(post("/client/{clientUsername}/challenges/{challengeId}/inscription/{inscriptionId}/submit",testClientUsername2,testChallengeId1,testInscriptionId2)
		.with(csrf())
		.param("url","https://test.com"))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/client/"+testClientUsername2+"/challenges/mine/"+testInscriptionId1));
	}
	
	@WithMockUser(value = "client2", authorities = {"client"})
	@Test
	void testWrongClient() throws Exception {
		
		mockMvc.perform(get("/client/{clientUsername}/challenges/{challengeId}/inscription/create",testClientUsername1,testChallengeId2))
		.andExpect(view().name("exception"));
		
		mockMvc.perform(post("/client/{clientUsername}/challenges/{challengeId}/inscription/{inscriptionId}/submit",testClientUsername1,testChallengeId1,testInscriptionId2)
		.with(csrf())
		.param("url","https://test.com"))
		.andExpect(view().name("exception"));
	}
	
	
	// UTILS
	private Challenge createChallenge(int id, String name, Date initialDate, Date endDate) {

		Exercise exercise1 = new Exercise();
		exercise1.setName("Exercise Test");

		Challenge c = new Challenge();
		c.setId(id);
		c.setName(name);
		c.setDescription("Challenge Description Test");
		c.setInitialDate(initialDate);
		c.setEndDate(endDate);
		c.setPoints(100);
		c.setReward("Reward Test");
		c.setReps(100);
		c.setWeight(100.);
		c.setExercise(exercise1);

		return c;
	}
	
	private Client createClient(int id, String username) {
		
		Client client = new Client();
		User userClient = new User();
		userClient.setUsername(username);
		userClient.setEnabled(true);
		client.setUser(userClient);
		client.setId(id);
		
		return client;
	}
}
