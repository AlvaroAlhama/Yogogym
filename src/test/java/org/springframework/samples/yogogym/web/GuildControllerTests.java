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
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.yogogym.configuration.SecurityConfiguration;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Guild;
import org.springframework.samples.yogogym.model.Trainer;
import org.springframework.samples.yogogym.model.User;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.GuildService;
import org.springframework.samples.yogogym.service.TrainerService;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = GuildController.class,
excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,classes = WebSecurityConfigurer.class),
excludeAutoConfiguration= SecurityConfiguration.class)

public class GuildControllerTests {
	
	private static final int testGuildId1 = 1;
	private static final int testGuildId2 = 2;
	private static final int testClientId1 = 1;
	private static final String testClientUsername1 = "client1";
	private static final int testClientId2 = 2;
	private static final String testClientUsername2 = "client3";
	private static final String testTrainerUsername= "trainer1";
	private static final int testTrainerId = 1;
	
	
	@MockBean
	private GuildService guildService;
	
	@MockBean
	private ClientService clientService;
	
	@MockBean
	private TrainerService trainerService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@BeforeEach
	void setUp(){	
	
		Guild guild1 = new Guild();
		guild1.setId(testGuildId1);
		guild1.setCreator(testClientUsername1);
		guild1.setDescription("We are connecting the world");
		guild1.setName("Connecting");
		guild1.setLogo("https://omega2001.es/wp-content/uploads/2016/02/red-informatica-1080x675.jpg");
		
		given(this.guildService.findGuildById(testGuildId1)).willReturn(guild1);
		
		Guild guild2 = new Guild();
		guild2.setId(testGuildId2);
		guild2.setCreator("client2");
		guild2.setDescription("We are connecting the world");
		guild2.setName("Connecting the world");
		guild2.setLogo("https://omega2001.es/wp-content/uploads/2016/02/red-informatica-1080x675.jpg");
		
		Collection<Guild> guilds = new ArrayList<>();
		guilds.add(guild1);
		guilds.add(guild2);
		
		Client client1 = new Client();
		User userClient1 = new User();
		userClient1.setUsername(testClientUsername1);
		userClient1.setEnabled(true);
		client1.setUser(userClient1);
		client1.setId(testClientId1);
		client1.setGuild(guild1);
		
		Client client2 = new Client();
		User userClient2 = new User();
		userClient2.setUsername(testClientUsername2);
		userClient2.setEnabled(true);
		client2.setUser(userClient2);
		client2.setId(testClientId2);
		client2.setGuild(guild2);
		
		Collection<Client> clients = new ArrayList<>();
		clients.add(client1);
		clients.add(client2);
		
		Trainer trainer = new Trainer();
		User user_trainer = new User();
		user_trainer.setUsername(testTrainerUsername);
		user_trainer.setEnabled(true);
		trainer.setUser(user_trainer);
		trainer.setClients(clients);
		trainer.setId(testTrainerId);
		
		given(this.guildService.findAllGuild()).willReturn(guilds);
		given(this.guildService.findGuildById(testGuildId2)).willReturn(guild2);
		given(this.clientService.findClientById(testClientId1)).willReturn(client1);
		given(this.clientService.findClientByUsername(testClientUsername1)).willReturn(client1);
		given(this.clientService.findClientById(testClientId2)).willReturn(client2);
		given(this.clientService.findClientByUsername(testClientUsername2)).willReturn(client2);
		given(this.trainerService.findTrainer(testTrainerUsername)).willReturn(trainer);
	}

	//HU5 - CRUD DE GREMIOS
	@WithMockUser(value = "CarlosD", authorities = {"client"})
	@Test
	void testListGuildsClient() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/guilds", testClientUsername1)).andExpect(status().isOk()).andExpect(model().attributeExists("allGuilds"))
			.andExpect(view().name("client/guilds/guildsList"));
	}
	
	@WithMockUser(value = "CarlosD", authorities = {"client"})
	@Test
	void testShowGuildByIdClient() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}",testClientUsername1, testClientId1)).andExpect(status().isOk())
				.andExpect(model().attribute("guild", hasProperty("name", is("Connecting"))))
				.andExpect(model().attribute("guild", hasProperty("description", is("We are connecting the world"))))
				.andExpect(model().attribute("guild", hasProperty("creator", is("client1"))))
				.andExpect(model().attribute("guild", hasProperty("logo", is("https://omega2001.es/wp-content/uploads/2016/02/red-informatica-1080x675.jpg"))))
				.andExpect(view().name("client/guilds/guildsDetails"));
	}
	//GOOD
	@WithMockUser(username = "client1", authorities = {"client"})
	@Test
	void testInitCreateGuildForm() throws Exception{
		
		mockMvc.perform(get("/client/{clientUsername}/guilds/create", testClientUsername2))
		.andExpect(status().isOk())
		.andExpect(view().name("client/guilds/guildsCreateOrUpdate"))
		.andExpect(model().attributeExists("guild"));
	}
	//CREATE
	@WithMockUser(username = "client1", authorities = {"client"})
	@Test
	void testProcessCreateGuildForm() throws Exception{
		mockMvc.perform(post("/client/{clientUsername}/guilds/create", testClientUsername2)
				.with(csrf())
				.param("name", "Guild Example")
				.param("description", "This is an example")
				.param("creator", "VictorM")
				.param("logo", "https://omega2001.es/wp-content/uploads/2016/02/red-informatica-1080x675.jpg"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/client/"+testClientUsername2+"/guilds"));
	}
	
	@WithMockUser(username = "client2", authorities = {"client"})
	@Test
	void testProcessCreateGuildHasErrorEmptyParameters() throws Exception {
		
		mockMvc.perform(post("/client/{clientUsername}/guilds/create", testClientUsername2)
				.with(csrf())
				.param("name", "")
				.param("description", "")
				.param("creator", "")
				.param("logo", ""))
			.andExpect(status().isOk()).andExpect(model().attributeHasErrors("guild"))
			.andExpect(model().attributeHasFieldErrors("guild", "name"))
			.andExpect(model().attributeHasFieldErrors("guild", "description"))
			.andExpect(model().attributeHasFieldErrors("guild", "creator"))
			.andExpect(model().attributeHasFieldErrors("guild", "logo"))
			.andExpect(view().name("client/guilds/guildsCreateOrUpdate"));
	}
	
	@WithMockUser(username = "client1", authorities = {"client"})
	@Test
	void testProcessCreateGuildHasErrorSameCreator() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/guilds/create", testClientUsername1)
				.with(csrf())
				.param("name", "Guild Test 1")
				.param("description", "This is an example 1")
				.param("creator", testClientUsername1)
				.param("logo", "https://omega2001.es/wp-content/uploads/2016/02/red-informatica-1080x675.jpg"))
		.andExpect(status().isOk()).andExpect(model().attributeHasErrors("guild"))
		.andExpect(model().attributeHasFieldErrors("guild", "creator"))
		.andExpect(view().name("client/guilds/guildsCreateOrUpdate"));
	}
	
	@WithMockUser(username = "client4", authorities = {"client"})
	@Test
	void testProcessCreateGuildHasErrorSameName() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/guilds/create", testClientUsername2)
				.with(csrf())
				.param("name", "Connecting")
				.param("description", "This is an example 2")
				.param("creator", testClientUsername2)
				.param("logo", "https://omega2001.es/wp-content/uploads/2016/02/red-informatica-1080x675.jpg"))
		.andExpect(status().isOk()).andExpect(model().attributeHasErrors("guild"))
		.andExpect(model().attributeHasFieldErrors("guild", "name"))
		.andExpect(view().name("client/guilds/guildsCreateOrUpdate"));
	}
	
	@WithMockUser(username = "client4", authorities = {"client"})
	@Test
	void testProcessCreateGuildHasErrorBadURL() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/guilds/create", "client4")
				.with(csrf())
				.param("name", "Texting Guild 3")
				.param("description", "This is an example 3")
				.param("creator", "client4")
				.param("logo", "amigo"))
		.andExpect(status().isOk()).andExpect(model().attributeHasErrors("guild"))
		.andExpect(model().attributeHasFieldErrors("guild", "logo"))
		.andExpect(view().name("client/guilds/guildsCreateOrUpdate"));
	}
	//DELETE
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testDeleteGuild() throws Exception{
		
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/delete",testClientUsername1,testGuildId1))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/client/{clientUsername}/guilds"));
	}
	
	@WithMockUser(value = "client5", authorities = {"client"})
	@Test
	void testDeleteGuildYouAreNotTheCreatorError() throws Exception{
		
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/delete","client5",testGuildId1))
		.andExpect(view().name("exception"));
	}
	//UPDATE
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testInitUpdateGuild() throws Exception{
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/edit",testClientUsername1,testGuildId1))
		.andExpect(status().isOk())
		.andExpect(view().name("client/guilds/guildsCreateOrUpdate"))
		.andExpect(model().attributeExists("guild"));	
	}
	
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testProcessUpdateGuildForm() throws Exception{
		mockMvc.perform(post("/client/{clientUsername}/guilds/{guildId}/edit", testClientUsername1,testGuildId1)
				.with(csrf())
				.param("name", "New Texting Guild")
				.param("description", "This is an example updated")
				.param("creator", testClientUsername1)
				.param("logo","https://ej.jpg"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/client/"+testClientUsername1+"/guilds/"+testGuildId1));
	}
	
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testProcessUpdateGuildFormHasErrorEmptyParameters() throws Exception{
		mockMvc.perform(post("/client/{clientUsername}/guilds/{guildId}/edit", testClientUsername1,testGuildId1)
				.with(csrf())
				.param("name", "")
				.param("description", "")
				.param("creator", "")
				.param("logo", ""))
			.andExpect(status().isOk()).andExpect(model().attributeHasErrors("guild"))
			.andExpect(model().attributeHasFieldErrors("guild", "name"))
			.andExpect(model().attributeHasFieldErrors("guild", "description"))
			.andExpect(model().attributeHasFieldErrors("guild", "creator"))
			.andExpect(model().attributeHasFieldErrors("guild", "logo"))
			.andExpect(view().name("client/guilds/guildsCreateOrUpdate"));
	}
	
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testProcessUpdateGuildFormHasErrorSameName() throws Exception{
		
		mockMvc.perform(post("/client/{clientUsername}/guilds/{guildId}/edit", testClientUsername1,testGuildId1)
				.with(csrf())
				.param("name", "Connecting")
				.param("description", "descrption")
				.param("creator", "client1")
				.param("logo", "https://hjhjhjhh.jpg"))
			.andExpect(status().isFound());
	}
	
	@WithMockUser(value = "client1", authorities = {"client"})
	@Test
	void testProcessUpdateGuildFormHasErrorBadURL() throws Exception{
		mockMvc.perform(post("/client/{clientUsername}/guilds/{guildId}/edit", testClientUsername1,testGuildId1)
				.with(csrf())
				.param("name", "Testin")
				.param("description", "descrption")
				.param("creator", "client1")
				.param("logo", "jhjhjhh.jpg"))
			.andExpect(status().isOk()).andExpect(model().attributeHasErrors("guild"))
			.andExpect(model().attributeHasFieldErrors("guild", "logo"))
			.andExpect(view().name("client/guilds/guildsCreateOrUpdate"));
	}
	
	@WithMockUser(value="client3", authorities = {"client"})
	@Test
	void testProcessUpdateGuildFormNotGuildCreatorError() throws Exception{
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/edit", "client4",testGuildId1))
		.andExpect(status().isOk())
		.andExpect(view().name("exception"));
	}
	
	//HU14 - UNIRSE Y SALIRSE DE LOS GREMIOS
	
	//JOIN
	@WithMockUser(value = "client4", authorities = {"client"})
	@Test
	void testJoinGuild() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/join","client4",testGuildId1))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/client/{clientUsername}/guilds"));
	}
	//LEAVE
	@WithMockUser(value = "client3", authorities = {"client"})
	@Test
	void testLeaveGuild() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/leave",testClientUsername2,testGuildId2))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/client/{clientUsername}/guilds"));
	}
	
	@WithMockUser(value = "client3", authorities = {"client"})
	@Test
	void testLeaveGuildYouAreNotInError() throws Exception
	{
		mockMvc.perform(get("/client/{clientUsername}/guilds/{guildId}/leave",testClientUsername2,testGuildId1))
		.andExpect(status().isOk())
		.andExpect(view().name("exception"));
	}
}

