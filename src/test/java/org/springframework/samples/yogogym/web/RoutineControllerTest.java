package org.springframework.samples.yogogym.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.yogogym.configuration.SecurityConfiguration;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.RoutineLine;
import org.springframework.samples.yogogym.model.Trainer;
import org.springframework.samples.yogogym.model.Training;
import org.springframework.samples.yogogym.model.User;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.RoutineService;
import org.springframework.samples.yogogym.service.TrainerService;
import org.springframework.samples.yogogym.service.TrainingService;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(value = RoutineController.class,
excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,classes = WebSecurityConfigurer.class),
excludeAutoConfiguration= SecurityConfiguration.class)
public class RoutineControllerTest {
	
	private static final int testRoutineId = 1;
	
	private static final String testTrainerUsername = "trainer1";
	
	private static final int testClientId = 1;
	
	private static final int testTrainingId = 1;
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	Calendar testInitialTrainingDate = Calendar.getInstance();
	Calendar testEndTrainingDate = testInitialTrainingDate;
	
	//Trainer 2
	private static final String testTrainerUsername_t2 = "trainer2";
	
	private static final int testClientId_t2 = 2;
	
	@MockBean
	private RoutineService routineService;
	
	@MockBean
	private ClientService clientService;

	@MockBean
	private TrainerService trainerService;
	
	@MockBean
	private TrainingService trainingService;
	
	@Autowired
	private MockMvc mockMvc;
		
	@BeforeEach
	void setUp()
	{
		//Trainer 1
		Collection<RoutineLine> routinesLines = new ArrayList<>();
		
		Routine routine= new Routine();
		routine.setName("Routine Test");
		routine.setDescription("Routine Description Test");
		routine.setRepsPerWeek(5);
		routine.setRoutineLine(routinesLines);
		
		Client client = new Client();
		User user_client = new User();
		user_client.setUsername("client1");
		user_client.setEnabled(true);
		client.setUser(user_client);
		client.setId(testClientId);
		
		Collection<Client> clients = new ArrayList<>();
		clients.add(client);
		
		Trainer trainer = new Trainer();
		User user_trainer = new User();
		user_trainer.setUsername(testTrainerUsername);
		user_trainer.setEnabled(true);
		trainer.setUser(user_trainer);
		trainer.setClients(clients);
				
		Date initialDate = testInitialTrainingDate.getTime();
		
		testEndTrainingDate.add(Calendar.DAY_OF_MONTH, 1);
		Date endDate = testEndTrainingDate.getTime();
		
		Collection<Routine> routines = new ArrayList<>();
		
		Training training = new Training();
		training.setId(testTrainingId);
		training.setName("training 1");
		training.setClient(client);
		training.setInitialDate(initialDate);
		training.setEndDate(endDate);
		training.setDiet(null);
		training.setRoutines(routines);
		
		given(this.clientService.findClientById(testClientId)).willReturn(client);
		given(this.trainerService.findTrainer(testTrainerUsername)).willReturn(trainer);
		given(this.trainingService.findTrainingById(testTrainingId)).willReturn(training);
		given(this.routineService.findRoutineById(testRoutineId)).willReturn(routine);
		
		//Trainer 2
		Client client_t2 = new Client();
		User user_client_t2 = new User();
		user_client_t2.setUsername("client2");
		user_client_t2.setEnabled(true);
		client_t2.setUser(user_client_t2);
		client_t2.setId(testClientId_t2);
		
		Collection<Client> clients_t2 = new ArrayList<>();
		clients_t2.add(client_t2);
		
		Trainer trainer_t2 = new Trainer();
		User user_trainer_t2 = new User();
		user_trainer_t2.setUsername(testTrainerUsername_t2);
		user_trainer_t2.setEnabled(true);
		trainer_t2.setUser(user_trainer_t2);
		trainer_t2.setClients(clients_t2);
		
		given(this.clientService.findClientById(testClientId_t2)).willReturn(client_t2);
		given(this.trainerService.findTrainer(testTrainerUsername_t2)).willReturn(trainer_t2);	
	}
	
	void testWrongAuth(int mode,String path,Object... uriVars) throws Exception
	{
		if(mode == 0)
		{
			mockMvc.perform(get(path,uriVars))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));			
		}
		else
		{
			mockMvc.perform(post(path,uriVars))
			.andExpect(status().isForbidden());
		}
	}
	
	@WithMockUser(username="client1", authorities= {"client"})
	@Test
	void testWrongAuthority() throws Exception
	{
		// Authority is not trainer
		testWrongAuth(0,"/trainer/{trainerUsername}/routines",testTrainerUsername);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,testTrainingId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,testTrainingId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/delete",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testTrainerWrongClients() throws Exception
	{
		// Wrong client id
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}",testTrainerUsername,testClientId_t2,testTrainingId,testRoutineId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId_t2,testTrainingId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId_t2,testTrainingId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId_t2,testTrainingId,testRoutineId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId_t2,testTrainingId,testRoutineId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/delete",testTrainerUsername,testClientId_t2,testTrainingId,testRoutineId);
	}
	
	@WithMockUser(username="trainer2", authorities= {"trainer"})
	@Test
	void testTrainerWrongAuthority() throws Exception
	{
		// Wrong trainer
		testWrongAuth(0,"/trainer/{trainerUsername}/routines",testTrainerUsername);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,testTrainingId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,testTrainingId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/delete",testTrainerUsername,testClientId,testTrainingId,testRoutineId);
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testNotExistingRoutine() throws Exception
	{
		final int badId = 100;
		// Wrong trainer
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}",testTrainerUsername,testClientId,testTrainingId,badId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,badId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,badId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,badId);
		testWrongAuth(1,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,badId);
		testWrongAuth(0,"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/delete",testTrainerUsername,testClientId,testTrainingId,badId);
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testGetRoutines() throws Exception
	{		
		mockMvc.perform(get("/trainer/{trainerUsername}/routines",testTrainerUsername))
		.andExpect(status().isOk())
		.andExpect(view().name("trainer/routines/routinesList"));
	}

	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testGetRoutineDetails() throws Exception
	{
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}",testTrainerUsername,testClientId,testTrainingId,testRoutineId))
			.andExpect(status().isOk())
			.andExpect(view().name("trainer/routines/routineDetails"))
			.andDo(print());
	}
				
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testInitCreateRoutineForm() throws Exception
	{
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,testTrainingId))
		.andExpect(status().isOk())
		.andExpect(view().name("trainer/routines/routinesCreateOrUpdate"))
		.andExpect(model().attributeExists("routine"));
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testProcessCreateRoutineForm() throws Exception
	{
		Collection<RoutineLine> routinesLines = new ArrayList<>();
		
		Routine routine= new Routine();
		routine.setName("Routine Test");
		routine.setDescription("Routine Description Test");
		routine.setRepsPerWeek(5);
		routine.setRoutineLine(routinesLines);
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/create",testTrainerUsername,testClientId,testTrainingId)
			.with(csrf())
			.param("name", routine.getName())
			.param("description", routine.getDescription())
			.param("repsPerWeek",routine.getRepsPerWeek().toString()))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/trainer/"+ testTrainerUsername + "/clients/" + testClientId + "/trainings/"+testTrainingId));
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testInitUpdateRoutineForm() throws Exception
	{
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,testRoutineId))
		.andExpect(status().isOk())
		.andExpect(view().name("trainer/routines/routinesCreateOrUpdate"))
		.andExpect(model().attributeExists("routine"));
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testProcessUpdateRoutineForm() throws Exception
	{
		Collection<RoutineLine> routinesLines = new ArrayList<>();
		
		Routine routine= new Routine();
		routine.setName("New Routine Test");
		routine.setDescription("New Routine Description Test");
		routine.setRepsPerWeek(10);
		routine.setRoutineLine(routinesLines);
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/edit",testTrainerUsername,testClientId,testTrainingId,testRoutineId)
			.with(csrf())
			.param("name", routine.getName())
			.param("description", routine.getDescription())
			.param("repsPerWeek",routine.getRepsPerWeek().toString()))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/trainer/" + testTrainerUsername + "/clients/" + testClientId + "/trainings/" + testTrainingId + "/routines/" + testRoutineId));
	}
	
	@WithMockUser(username="trainer1", authorities= {"trainer"})
	@Test
	void testDeleteRoutine() throws Exception
	{
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/delete",testTrainerUsername,testClientId,testTrainingId,testRoutineId))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/trainer/" + testTrainerUsername + "/routines"));
	}
}
