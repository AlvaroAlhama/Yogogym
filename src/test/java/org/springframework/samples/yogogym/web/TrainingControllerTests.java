package org.springframework.samples.yogogym.web;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.yogogym.configuration.SecurityConfiguration;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Diet;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.Trainer;
import org.springframework.samples.yogogym.model.Training;
import org.springframework.samples.yogogym.model.User;
import org.springframework.samples.yogogym.model.Enums.EditingPermission;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.TrainerService;
import org.springframework.samples.yogogym.service.TrainingService;
import org.springframework.samples.yogogym.service.exceptions.EndBeforeEqualsInitException;
import org.springframework.samples.yogogym.service.exceptions.EndInTrainingException;
import org.springframework.samples.yogogym.service.exceptions.InitInTrainingException;
import org.springframework.samples.yogogym.service.exceptions.LongerThan90DaysException;
import org.springframework.samples.yogogym.service.exceptions.PastEndException;
import org.springframework.samples.yogogym.service.exceptions.PastInitException;
import org.springframework.samples.yogogym.service.exceptions.PeriodIncludingTrainingException;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@WebMvcTest(value = TrainingController.class,
excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,classes = WebSecurityConfigurer.class),
excludeAutoConfiguration= SecurityConfiguration.class)
class TrainingControllerTests {
	
	private static final String TRAINER1_USERNAME = "trainer1";
	private static final String TRAINER2_USERNAME = "trainer2";
	private static final String CLIENT1_USERNAME = "client1";
	private static final String CLIENT2_USERNAME = "client2";
	
	private static final int CLIENT1_ID = 1;
	private static final int CLIENT2_ID = 2;
	
	private static final String NIF1 = "12345678F";
	private static final String NIF2 = "12345678G";
	
	private static final int CLIENT1_TRAINING1_ID = 1;
	private static final int CLIENT1_TRAINING2_ID = 2;
	private static final int CLIENT1_TRAINING3_ID = 3;
	private static final int CLIENT1_TRAINING4_ID = 4;
	private static final int CLIENT1_TRAINING5_ID = 5;
	private static final int CLIENT1_TRAINING6_ID = 6;
	private static final int CLIENT1_TRAINING8_ID = 8;
	private static final int CLIENT2_TRAINING7_ID = 7;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private static Date initialDate = null;
	private static Date endDate = null;
	
	@MockBean
	private ClientService clientService;
	
	@MockBean
	private TrainerService trainerService;
	
	@MockBean
	private TrainingService trainingService;
		
	@Autowired
	private MockMvc mockMvc;
		
	@BeforeEach
	void setup() {
		Calendar initCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		initialDate = initCal.getTime();
		endCal.add(Calendar.DAY_OF_MONTH, 7);
		endDate = endCal.getTime();
		
		//Trainer1
		Collection<Client> clientsTrainer1 = new ArrayList<>();
				
		Client client1 = new Client();
		User userClient1 = new User();
		userClient1.setUsername(CLIENT1_USERNAME);
		userClient1.setEnabled(true);
		client1.setUser(userClient1);
		client1.setId(CLIENT1_ID);
		client1.setNif(NIF1);
		client1.setIsPublic(true);
		
		clientsTrainer1.add(client1);
		
		Trainer trainer1 = new Trainer();
		User userTrainer1 = new User();
		userTrainer1.setUsername(TRAINER1_USERNAME);
		userTrainer1.setEnabled(true);
		trainer1.setUser(userTrainer1);
		trainer1.setClients(clientsTrainer1);
		
		Training training1 = new Training();
		training1.setId(CLIENT1_TRAINING1_ID);
		training1.setName("Training 1");
		training1.setInitialDate(initialDate);
		training1.setEndDate(endDate);
		training1.setEditingPermission(EditingPermission.TRAINER);
		training1.setAuthor(TRAINER1_USERNAME);
		training1.setDiet(null);
		training1.setRoutines(new ArrayList<>());

		Training training2 = new Training();
		BeanUtils.copyProperties(training1, training2);
		training2.setId(CLIENT1_TRAINING2_ID);
		training2.setName("Training 2");
		training2.setEditingPermission(EditingPermission.CLIENT);
		training2.setAuthor(client1.getUser().getUsername());
		
		Training training3 = new Training();
		BeanUtils.copyProperties(training1, training3);
		training3.setId(CLIENT1_TRAINING3_ID);
		training3.setName("Training 3");
		training3.setEditingPermission(EditingPermission.BOTH);
		training3.setAuthor(TRAINER1_USERNAME);
		
		Training training4 = new Training();
		training4.setId(CLIENT1_TRAINING4_ID);
		training4.setName("Training 4");
		training4.setInitialDate(initialDate);
		training4.setEndDate(endDate);
		training4.setEditingPermission(EditingPermission.TRAINER);
		training4.setAuthor(TRAINER1_USERNAME);
		Diet diet = new Diet();
		diet.setId(1);
		diet.setName("Diet");
		training4.setDiet(diet);
		Routine routine = new Routine();
		routine.setId(1);
		routine.setName("Routine");
		Collection<Routine> routineList = new ArrayList<>();
		routineList.add(routine);
		training4.setRoutines(routineList);
		
		Training training8 = new Training();
		training8.setId(CLIENT1_TRAINING8_ID);
		training8.setName("Training 8");
		training8.setInitialDate(initialDate);
		training8.setEndDate(endDate);
		training8.setEditingPermission(EditingPermission.BOTH);
		training8.setAuthor(CLIENT1_USERNAME);
		training8.setDiet(null);
		training8.setRoutines(new ArrayList<>());
		
		Training training5 = new Training();
		BeanUtils.copyProperties(training4, training5);
		training5.setId(CLIENT1_TRAINING5_ID);
		training5.setDiet(null);
		
		Training training6 = new Training();
		BeanUtils.copyProperties(training4, training6);
		training6.setId(CLIENT1_TRAINING6_ID);
		training6.setRoutines(new ArrayList<>());
		
		Collection<Training> trainingList1 = new ArrayList<>();
		trainingList1.add(training1);
		trainingList1.add(training2);
		trainingList1.add(training3);
		trainingList1.add(training4);
		trainingList1.add(training5);
		trainingList1.add(training6);
		trainingList1.add(training8);
		
		client1.setTrainings(trainingList1);
		
		Collection<Integer> trainingIdList = new ArrayList<>();
		trainingIdList.add(CLIENT1_TRAINING1_ID);
		trainingIdList.add(CLIENT1_TRAINING2_ID);
		trainingIdList.add(CLIENT1_TRAINING3_ID);
		trainingIdList.add(CLIENT1_TRAINING4_ID);
		trainingIdList.add(CLIENT1_TRAINING5_ID);
		trainingIdList.add(CLIENT1_TRAINING6_ID);
		
		//Trainer2
		Collection<Client> clientsTrainer2 = new ArrayList<>();
		
		Client client2 = new Client();
		User userClient2 = new User();
		userClient2.setUsername(CLIENT2_USERNAME);
		userClient2.setEnabled(true);
		client2.setUser(userClient2);
		client2.setId(CLIENT2_ID);
		client2.setNif(NIF2);
		client2.setIsPublic(true);
		
		clientsTrainer2.add(client2);
		
		Trainer trainer2 = new Trainer();
		User userTrainer2 = new User();
		userTrainer2.setUsername(TRAINER2_USERNAME);
		userTrainer2.setEnabled(true);
		trainer2.setUser(userTrainer2);
		trainer2.setClients(clientsTrainer2);
		
		Training training7 = new Training();
		training7.setId(CLIENT2_TRAINING7_ID);
		training7.setName("Training 2");
		training7.setInitialDate(initialDate);
		training7.setEndDate(endDate);
		training7.setEditingPermission(EditingPermission.TRAINER);
		training7.setAuthor(TRAINER2_USERNAME);
		training7.setDiet(null);
		training7.setRoutines(new ArrayList<>());
		
		Collection<Training> trainingList2 = new ArrayList<>();
		trainingList2.add(training7);
		
		given(this.clientService.findClientById(CLIENT1_ID)).willReturn(client1);
		given(this.clientService.findClientById(CLIENT2_ID)).willReturn(client2);
		given(this.clientService.findClientByUsername(CLIENT1_USERNAME)).willReturn(client1);
		given(this.clientService.findClientByUsername(CLIENT2_USERNAME)).willReturn(client2);
		given(this.trainerService.findTrainer(TRAINER1_USERNAME)).willReturn(trainer1);
		given(this.trainerService.findTrainer(TRAINER2_USERNAME)).willReturn(trainer2);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING1_ID)).willReturn(training1);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING2_ID)).willReturn(training2);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING3_ID)).willReturn(training3);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING4_ID)).willReturn(training4);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING5_ID)).willReturn(training5);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING6_ID)).willReturn(training6);
		given(this.trainingService.findTrainingById(CLIENT1_TRAINING8_ID)).willReturn(training8);
		given(this.trainingService.findTrainingFromClient(CLIENT1_ID)).willReturn(trainingList1);
		given(this.trainingService.findTrainingFromClient(CLIENT2_ID)).willReturn(trainingList2);
		
		//Copy Training
		given(this.trainingService.findTrainingWithPublicClient()).willReturn(trainingList1);
		given(this.trainingService.findTrainingIdFromClient(CLIENT1_ID)).willReturn(trainingIdList);
		given(this.trainingService.findTrainingIdFromClient(CLIENT2_ID)).willReturn(new ArrayList<>());
		given(this.clientService.isPublicByTrainingId(CLIENT1_TRAINING4_ID)).willReturn(true);		
	}
	
	//TRAINER
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(strings = {"/client/{clientUsername}/trainings",
		"/client/{clientUsername}/trainings/{trainingId}","/client/{clientUsername}/trainings/create",
		"/client/{clientUsername}/trainings/{trainingId}/edit","/client/{clientUsername}/trainings/{trainingId}/delete"})
	void testTrainerPerformGetClientSectionError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(get(path,CLIENT1_USERNAME,CLIENT1_TRAINING1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(get(path,CLIENT1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(strings = {"/client/{clientUsername}/trainings/create",
		"/client/{clientUsername}/trainings/{trainingId}/edit"})
	void testTrainerPerformPostClientSectionError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(post(path,CLIENT1_USERNAME,CLIENT1_TRAINING1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(post(path,CLIENT1_USERNAME)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(strings = {"/trainer/{trainerUsername}/trainings",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/create",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/delete"})
	void testTrainerPerformGetWrongUsernameError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(get(path,TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else if(path.contains("clientId")) {
			mockMvc.perform(get(path,TRAINER1_USERNAME,CLIENT1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(get(path,TRAINER1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(strings = {"/trainer/{trainerUsername}/clients/{clientId}/trainings/create",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit"})
	void testTrainerPerformPostWrongUsernameError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(post(path,TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(post(path,TRAINER1_USERNAME,CLIENT1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(strings = {"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/create",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/delete"})
	void testTrainerPerformGetWrongClientError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(get(path,TRAINER2_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(get(path,TRAINER2_USERNAME,CLIENT1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(strings = {"/trainer/{trainerUsername}/clients/{clientId}/trainings/create",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit"})
	void testTrainerPerformPostWrongClientError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(post(path,TRAINER2_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(post(path,TRAINER2_USERNAME,CLIENT1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerPerformGetNoEditingPermissionError() throws Exception {
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit",TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING2_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));
	}
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerPerformPostNoEditingPermissionError() throws Exception {
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit",TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING2_ID)
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));
	}
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerDeleteNoAuthor() throws Exception {
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/delete",TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING2_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));
	}
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerTrainingList() throws Exception {
		mockMvc.perform(get("/trainer/{trainerUsername}/trainings",TRAINER1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("trainer"))
				.andExpect(model().attributeExists("actualDate"))
				.andExpect(model().attribute("trainer", hasProperty("clients", hasSize(1))))
				.andExpect(model().attribute("trainer", hasProperty("clients", hasItem(allOf(hasProperty("id", equalTo(CLIENT1_ID)),hasProperty("trainings", hasSize(7)))))))
				.andExpect(model().attribute("trainer", hasProperty("clients", hasItem(allOf(hasProperty("id", equalTo(CLIENT1_ID)),hasProperty("trainings", hasItem(allOf(
					hasProperty("id", equalTo(CLIENT1_TRAINING1_ID)),hasProperty("name", is("Training 1")),
					hasProperty("initialDate", equalTo(initialDate)),hasProperty("endDate", equalTo(endDate)),
					hasProperty("editingPermission", equalTo(EditingPermission.TRAINER)),hasProperty("author", is(TRAINER1_USERNAME)),
					hasProperty("diet", nullValue()),hasProperty("routines", is(new ArrayList<>()))))))))))
				.andExpect(view().name("trainer/trainings/trainingsList"));
	}			
	
	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerTrainingDetails() throws Exception {
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}",TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID))
		 		.andExpect(status().isOk())
		 		.andExpect(model().attributeExists("client"))
				.andExpect(model().attributeExists("training"))
				.andExpect(model().attribute("training", hasProperty("name", is("Training 1"))))
				.andExpect(model().attribute("training", hasProperty("initialDate", equalTo(initialDate))))
				.andExpect(model().attribute("training", hasProperty("endDate", equalTo(endDate))))
				.andExpect(model().attribute("training", hasProperty("author", is(TRAINER1_USERNAME))))
				.andExpect(model().attribute("training", hasProperty("editingPermission", equalTo(EditingPermission.TRAINER))))
				.andExpect(model().attribute("training", hasProperty("routines", is(new ArrayList<>()))))
				.andExpect(model().attribute("training", hasProperty("diet", nullValue())))
				.andExpect(view().name("trainer/trainings/trainingsDetails"));
	}

	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerInitTrainingCreationForm() throws Exception {
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID))
		 		.andExpect(status().isOk())
		 		.andExpect(model().attributeExists("client"))
		 		.andExpect(model().attributeExists("training"))
				.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
				.with(csrf())	
			 	.param("name", "Training 2")
			 	.param("initialDate", dateFormat.format(initialDate))
			 	.param("endDate", dateFormat.format(endDate))
			 	.param("editingPermission", EditingPermission.TRAINER.toString())
			 	.param("author", TRAINER2_USERNAME))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/trainer/{trainerUsername}/clients/{clientId}/trainings/"+CLIENT2_TRAINING7_ID));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsEmptyParameters() throws Exception {
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
			 	.with(csrf())
			 	.param("name", "")
			 	.param("initialDate", "")
			 	.param("endDate", "")
			 	.param("editingPermission", EditingPermission.TRAINER.toString())
			 	.param("author", TRAINER2_USERNAME))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("training"))
				.andExpect(model().attributeHasFieldErrors("training", "name"))
				.andExpect(model().attributeHasFieldErrors("training", "initialDate"))
				.andExpect(model().attributeHasFieldErrors("training", "endDate"))
				.andExpect(model().errorCount(3))
				.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsPastInitDate() throws Exception {
		
		doThrow(new PastInitException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
		 	.with(csrf())
		 	.params(createTrainingClient2(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "initialDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsEndBeforeEqualsInit() throws Exception {
		
		doThrow(new EndBeforeEqualsInitException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
		 	.with(csrf())
		 	.params(createTrainingClient2(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsLongerThan90Days() throws Exception {
		
		doThrow(new LongerThan90DaysException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
		 	.with(csrf())
		 	.params(createTrainingClient2(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsInitInTraining() throws Exception {
		
		doThrow(new InitInTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
		 	.with(csrf())
		 	.params(createTrainingClient2(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "initialDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsEndInTraining() throws Exception {
		
		doThrow(new EndInTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
		 	.with(csrf())
		 	.params(createTrainingClient2(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=TRAINER2_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingCreationFormHasErrorsPeriodIncludingTraining() throws Exception {
		
		doThrow(new PeriodIncludingTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/create",TRAINER2_USERNAME,CLIENT2_ID)
		 	.with(csrf())
		 	.params(createTrainingClient2(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}

	@WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING1_ID,CLIENT1_TRAINING3_ID})
	void testTrainerInitTrainingUpdateForm(int trainingId) throws Exception {
		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,trainingId))
		 		.andExpect(status().isOk())
		  		.andExpect(model().attributeExists("client"))
		  		.andExpect(model().attributeExists("endDateAux"))
		  		.andExpect(model().attributeExists("actualDate"))
		  		.andExpect(model().attributeExists("training"))
		  		.andExpect(model().attribute("training", hasProperty("name", is(trainingId==1?"Training 1":"Training 3"))))
				.andExpect(model().attribute("training", hasProperty("initialDate", equalTo(initialDate))))
				.andExpect(model().attribute("training", hasProperty("endDate", equalTo(endDate))))
				.andExpect(model().attribute("training", hasProperty("author", is(TRAINER1_USERNAME))))
				.andExpect(model().attribute("training", hasProperty("editingPermission", equalTo(trainingId==1?EditingPermission.TRAINER:EditingPermission.BOTH))))
				.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}

    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
    @ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING1_ID,CLIENT1_TRAINING3_ID})
	void testTrainerProcessTrainingUpdateFormSuccess(int trainingId) throws Exception {   
    	
    	Calendar now = Calendar.getInstance();
    	now.add(Calendar.DAY_OF_MONTH, 14);
    	Date endDateUpdated = now.getTime();
    	
    	mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,trainingId)
    			.with(csrf())
    			.param("name", "Training 1 Updated")
    			.param("initialDate", dateFormat.format(initialDate))
				.param("endDate", dateFormat.format(endDateUpdated))
				.param("editingPermission", EditingPermission.BOTH.toString())
				.param("author", TRAINER1_USERNAME))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}"));
	}

    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
	@Test
	void testTrainerProcessTrainingUpdateFormHasErrorsEmptyParameters() throws Exception {
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
				.with(csrf())	
				.param("name", "")
				.param("initialDate", dateFormat.format(initialDate))
				.param("endDate", "")
				.param("editingPermission", EditingPermission.BOTH.toString())
				.param("author", TRAINER1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("training"))
				.andExpect(model().attributeHasFieldErrors("training", "name"))
				.andExpect(model().attributeHasFieldErrors("training", "endDate"))
				.andExpect(model().errorCount(2))
				.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
	}
   	
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
   	@Test
   	void testTrainerProcessTrainingUpdateFormHasErrorsPastEnd() throws Exception {
   		
   		doThrow(new PastEndException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
   	@Test
   	void testTrainerProcessTrainingUpdateFormHasErrorsEndBeforeEqualsInit() throws Exception {
   		
   		doThrow(new EndBeforeEqualsInitException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
   	@Test
   	void testTrainerProcessTrainingUpdateFormHasErrorsLongerThan90Days() throws Exception {
   		
   		doThrow(new LongerThan90DaysException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
   	@Test
   	void testTrainerProcessTrainingUpdateFormHasErrorsEndInTraining() throws Exception {
   		
   		doThrow(new EndInTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
   	@Test
   	void testTrainerProcessTrainingUpdateFormHasErrorsPeriodIncludingTraining() throws Exception {
   		
   		doThrow(new PeriodIncludingTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit", TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(true)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("trainer/trainings/trainingCreateOrUpdate"));
   	}
    
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
    @ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING1_ID,CLIENT1_TRAINING3_ID})
	void testTrainerProcessTrainingDeleteForm(int trainingId) throws Exception {
    	mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/delete", TRAINER1_USERNAME,CLIENT1_ID,trainingId))
    			.andExpect(status().is3xxRedirection())
    			.andExpect(view().name("redirect:/trainer/{trainerUsername}/trainings"));
    }
    
    //Copy training
    
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
    @ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING1_ID,CLIENT1_TRAINING3_ID})
   	void testGetTrainingListCopySuccessful(int trainingId) throws Exception {
   		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/copyTraining", TRAINER1_USERNAME,CLIENT1_ID,trainingId))
   				.andExpect(status().isOk())
   				.andExpect(view().name("trainer/trainings/listCopyTraining"));
   	}
    
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
    @ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING4_ID,CLIENT1_TRAINING5_ID,CLIENT1_TRAINING6_ID})
   	void testGetTrainingListCopyFailed(int trainingId) throws Exception {
   		mockMvc.perform(get("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/copyTraining", TRAINER1_USERNAME,CLIENT1_ID,trainingId))
   				.andExpect(status().isOk())
   				.andExpect(view().name("exception"));
   	}
    
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
    @ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING1_ID,CLIENT1_TRAINING3_ID})
	void testProcessCopyTrainingSuccess(int trainingId) throws Exception {
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/copyTraining", TRAINER1_USERNAME,CLIENT1_ID,trainingId)
				.with(csrf())
			 	.param("trainingIdToCopy", String.valueOf(CLIENT1_TRAINING4_ID))
			 	.param("trainerUsername", "trainer1"))
				.andExpect(status().is3xxRedirection())
		 		.andExpect(view().name("redirect:/trainer/{trainerUsername}/trainings"));
	}
    
    @WithMockUser(username=TRAINER1_USERNAME, authorities= {"trainer"})
    @ParameterizedTest
	@ValueSource(ints = {CLIENT1_TRAINING4_ID,CLIENT1_TRAINING5_ID,CLIENT1_TRAINING6_ID})
	void testProcessCopyTrainingFailed(int trainingId) throws Exception {
		mockMvc.perform(post("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/copyTraining", TRAINER1_USERNAME,CLIENT1_ID,trainingId)
				.with(csrf())
			 	.param("trainingIdToCopy", String.valueOf(CLIENT1_TRAINING4_ID))
			 	.param("trainerUsername", "trainer1"))
				.andExpect(status().isOk())
		 		.andExpect(view().name("exception"));
	}
    
    //CLIENT
    
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@ParameterizedTest
	@ValueSource(strings = {"/trainer/{trainerUsername}/trainings",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/create",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/delete"})
	void testClientPerformGetTrainerSectionError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(get(path,TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else if(path.contains("clientId")) {
			mockMvc.perform(get(path,TRAINER1_USERNAME,CLIENT1_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(get(path,TRAINER1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@ParameterizedTest
	@ValueSource(strings = {"/trainer/{trainerUsername}/clients/{clientId}/trainings/create",
		"/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit"})
	void testClientPerformPostTrainerSectionError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(post(path,TRAINER1_USERNAME,CLIENT1_ID,CLIENT1_TRAINING1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(post(path,TRAINER1_USERNAME,CLIENT1_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@ParameterizedTest
	@ValueSource(strings = {"/client/{clientUsername}/trainings",
		"/client/{clientUsername}/trainings/{trainingId}",
		"/client/{clientUsername}/trainings/create",
		"/client/{clientUsername}/trainings/{trainingId}/edit",
		"/client/{clientUsername}/trainings/{trainingId}/delete"})
	void testClientPerformGetWrongUsernameError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(get(path,CLIENT1_USERNAME,CLIENT1_TRAINING2_ID))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(get(path,CLIENT1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@ParameterizedTest
	@ValueSource(strings = {"/client/{clientUsername}/trainings/create",
		"/client/{clientUsername}/trainings/{trainingId}/edit"})
	void testClientPerformPostWrongUsernameError(String path) throws Exception {
		if(path.contains("trainingId")) {
			mockMvc.perform(post(path,CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
		else {
			mockMvc.perform(post(path,CLIENT1_USERNAME)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("exception"));
		}
	}
	
	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@Test
	void testClientPerformGetNoEditingPermissionError() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/trainings/{trainingId}/edit",CLIENT1_USERNAME,CLIENT1_TRAINING1_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));
	}
	
	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@Test
	void testClientPerformPostNoEditingPermissionError() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit",CLIENT1_USERNAME,CLIENT1_TRAINING1_ID)
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));
	}
	
	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@Test
	void testClientDeleteNoAuthor() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/trainings/{trainingId}/delete",CLIENT1_USERNAME,CLIENT1_TRAINING1_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("exception"));
	}
	
	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@Test
	void testClientTrainingList() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/trainings",CLIENT1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("trainings"))
				.andExpect(model().attribute("trainings", hasSize(7)))
				.andExpect(model().attribute("trainings", hasItem(allOf(hasProperty("id", equalTo(CLIENT1_TRAINING1_ID)),
					hasProperty("name", is("Training 1")),hasProperty("initialDate", equalTo(initialDate)),hasProperty("endDate", equalTo(endDate)),
					hasProperty("editingPermission", equalTo(EditingPermission.TRAINER)),hasProperty("author", is(TRAINER1_USERNAME)),
					hasProperty("diet", nullValue()),hasProperty("routines", is(new ArrayList<>()))))))
				.andExpect(view().name("client/trainings/trainingsList"));
	}	
	
	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@Test
	void testClientTrainingDetails() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/trainings/{trainingId}",CLIENT1_USERNAME,CLIENT1_TRAINING1_ID))
		 		.andExpect(status().isOk())
		 		.andExpect(model().attributeExists("client"))
				.andExpect(model().attributeExists("training"))
				.andExpect(model().attribute("training", hasProperty("name", is("Training 1"))))
				.andExpect(model().attribute("training", hasProperty("initialDate", equalTo(initialDate))))
				.andExpect(model().attribute("training", hasProperty("endDate", equalTo(endDate))))
				.andExpect(model().attribute("training", hasProperty("author", is(TRAINER1_USERNAME))))
				.andExpect(model().attribute("training", hasProperty("editingPermission", equalTo(EditingPermission.TRAINER))))
				.andExpect(model().attribute("training", hasProperty("routines", is(new ArrayList<>()))))
				.andExpect(model().attribute("training", hasProperty("diet", nullValue())))
				.andExpect(view().name("client/trainings/trainingsDetails"));
	}

	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientInitTrainingCreationForm() throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME))
		 		.andExpect(status().isOk())
		 		.andExpect(model().attributeExists("client"))
		 		.andExpect(model().attributeExists("training"))
				.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
				.with(csrf())	
			 	.param("name", "Training 2")
			 	.param("initialDate", dateFormat.format(initialDate))
			 	.param("endDate", dateFormat.format(endDate))
			 	.param("editingPermission", EditingPermission.CLIENT.toString())
			 	.param("author", CLIENT2_USERNAME))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/client/{clientUsername}/trainings/"+CLIENT2_TRAINING7_ID));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsEmptyParameters() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
			 	.with(csrf())
			 	.param("name", "")
			 	.param("initialDate", "")
			 	.param("endDate", "")
			 	.param("editingPermission", EditingPermission.CLIENT.toString())
			 	.param("author", CLIENT2_USERNAME))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("training"))
				.andExpect(model().attributeHasFieldErrors("training", "name"))
				.andExpect(model().attributeHasFieldErrors("training", "initialDate"))
				.andExpect(model().attributeHasFieldErrors("training", "endDate"))
				.andExpect(model().errorCount(3))
				.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsPastInitDate() throws Exception {
		
		doThrow(new PastInitException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
		 	.with(csrf())
		 	.params(createTrainingClient2(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "initialDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsEndBeforeEqualsInit() throws Exception {
		
		doThrow(new EndBeforeEqualsInitException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
		 	.with(csrf())
		 	.params(createTrainingClient2(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsLongerThan90Days() throws Exception {
		
		doThrow(new LongerThan90DaysException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
		 	.with(csrf())
		 	.params(createTrainingClient2(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsInitInTraining() throws Exception {
		
		doThrow(new InitInTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
		 	.with(csrf())
		 	.params(createTrainingClient2(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "initialDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsEndInTraining() throws Exception {
		
		doThrow(new EndInTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
		 	.with(csrf())
		 	.params(createTrainingClient2(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
	
	@WithMockUser(username=CLIENT2_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingCreationFormHasErrorsPeriodIncludingTraining() throws Exception {
		
		doThrow(new PeriodIncludingTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
		
		mockMvc.perform(post("/client/{clientUsername}/trainings/create",CLIENT2_USERNAME)
		 	.with(csrf())
		 	.params(createTrainingClient2(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}

	@WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@ParameterizedTest
   	@ValueSource(ints = {CLIENT1_TRAINING2_ID,CLIENT1_TRAINING3_ID})
	void testClientInitTrainingUpdateForm(int trainingId) throws Exception {
		mockMvc.perform(get("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,trainingId))
		 		.andExpect(status().isOk())
		  		.andExpect(model().attributeExists("client"))
		  		.andExpect(model().attributeExists("endDateAux"))
		  		.andExpect(model().attributeExists("actualDate"))
		  		.andExpect(model().attributeExists("training"))
		  		.andExpect(model().attribute("training", hasProperty("name", is(trainingId==2?"Training 2":"Training 3"))))
				.andExpect(model().attribute("training", hasProperty("initialDate", equalTo(initialDate))))
				.andExpect(model().attribute("training", hasProperty("endDate", equalTo(endDate))))
				.andExpect(model().attribute("training", hasProperty("author", is(trainingId==2?CLIENT1_USERNAME:TRAINER1_USERNAME))))
				.andExpect(model().attribute("training", hasProperty("editingPermission", equalTo(trainingId==2?EditingPermission.CLIENT:EditingPermission.BOTH))))
				.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}

    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
    @ParameterizedTest
   	@ValueSource(ints = {CLIENT1_TRAINING2_ID,CLIENT1_TRAINING3_ID})
	void testClientProcessTrainingUpdateFormSuccess(int trainingId) throws Exception {   
    	
    	Calendar now = Calendar.getInstance();
    	now.add(Calendar.DAY_OF_MONTH, 14);
    	Date endDateUpdated = now.getTime();
    	
    	mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,trainingId)
    			.with(csrf())
    			.param("name", "Training 1 Updated")
    			.param("initialDate", dateFormat.format(initialDate))
				.param("endDate", dateFormat.format(endDateUpdated))
				.param("editingPermission", EditingPermission.BOTH.toString())
				.param("author", trainingId==2 ? CLIENT1_USERNAME : TRAINER1_USERNAME))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/client/{clientUsername}/trainings/{trainingId}"));
	}

    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	@Test
	void testClientProcessTrainingUpdateFormHasErrorsEmptyParameters() throws Exception {
		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
				.with(csrf())	
				.param("name", "")
				.param("initialDate", dateFormat.format(initialDate))
				.param("endDate", "")
				.param("editingPermission", EditingPermission.CLIENT.toString())
				.param("author", CLIENT1_USERNAME))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("training"))
				.andExpect(model().attributeHasFieldErrors("training", "name"))
				.andExpect(model().attributeHasFieldErrors("training", "endDate"))
				.andExpect(model().errorCount(2))
				.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
	}
   	
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
   	@Test
   	void testClientProcessTrainingUpdateFormHasErrorsPastEnd() throws Exception {
   		
   		doThrow(new PastEndException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
   	@Test
   	void testClientProcessTrainingUpdateFormHasErrorsEndBeforeEqualsInit() throws Exception {
   		
   		doThrow(new EndBeforeEqualsInitException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
   	@Test
   	void testClientProcessTrainingUpdateFormHasErrorsLongerThan90Days() throws Exception {
   		
   		doThrow(new LongerThan90DaysException()).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
   	@Test
   	void testClientProcessTrainingUpdateFormHasErrorsEndInTraining() throws Exception {
   		
   		doThrow(new EndInTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
   	}
   	
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
   	@Test
   	void testClientProcessTrainingUpdateFormHasErrorsPeriodIncludingTraining() throws Exception {
   		
   		doThrow(new PeriodIncludingTrainingException("","")).when(this.trainingService).saveTraining(Mockito.any(Training.class),Mockito.any(Client.class));
   		
   		mockMvc.perform(post("/client/{clientUsername}/trainings/{trainingId}/edit", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID)
		 	.with(csrf())
		 	.params(updateTrainingClient1(false)))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("training"))
			.andExpect(model().attributeHasFieldErrors("training", "endDate"))
			.andExpect(model().errorCount(1))
			.andExpect(view().name("client/trainings/trainingCreateOrUpdate"));
   	}
    
    @WithMockUser(username=CLIENT1_USERNAME, authorities= {"client"})
	void testClientProcessTrainingDeleteForm() throws Exception {
    	mockMvc.perform(get("/client/{clientUsername}/trainings/{trainingId}/delete", CLIENT1_USERNAME,CLIENT1_TRAINING2_ID))
    			.andExpect(status().is3xxRedirection())
    			.andExpect(view().name("redirect:/client/{clientUsername}/trainings"));
    }
    
	private MultiValueMap<String,String> createTrainingClient2(boolean isPrincipalTrainer){
		
		MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
		
		params.add("name", "Training 2");
		params.add("initialDate", dateFormat.format(initialDate));
		params.add("endDate", dateFormat.format(endDate));
		params.add("editingPermission", EditingPermission.BOTH.toString());
		params.add("author", isPrincipalTrainer?TRAINER2_USERNAME:CLIENT2_USERNAME);
		
		return params;
	}
	
	private MultiValueMap<String,String> updateTrainingClient1(boolean isTraining1){
		
		MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
		
		params.add("name", "Training 1 Updated");
		params.add("initialDate", dateFormat.format(initialDate));
		params.add("endDate", dateFormat.format(endDate));
		params.add("editingPermission", EditingPermission.BOTH.toString());
		params.add("author", isTraining1?TRAINER1_USERNAME:CLIENT1_USERNAME);
		
		return params;
	}
	 
}