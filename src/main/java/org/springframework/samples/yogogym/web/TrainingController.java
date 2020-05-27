package org.springframework.samples.yogogym.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.RoutineLine;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TrainingController {
	
	//GENERAL
	private static final String EXCEPTION = "exception";
	private static final String ACTUAL_DATE = "actualDate";
	private static final String END_DATE_AUX = "endDateAux";
	private static final String CLIENT = "client";
	private static final String TRAINING = "training";
	private static final String TRAININGS = "trainings";
	private static final String DELETE_MESSAGE = "deleteMessage";
	private static final String DELETED_SUCCESSFULLY = "The training was deleted successfully";
	private static final String NOT_HAVE_PUBLIC = "notHaveTrainingsPublic";
	
	//TRAINER
	private static final String TRAINER_TRAINING_LIST = "trainer/trainings/trainingsList";
	private static final String TRAINER_TRAINING_LIST_REDIRECT_URL = "redirect:/trainer/{trainerUsername}/trainings";
	private static final String TRAINER_TRAINING_DETAILS = "trainer/trainings/trainingsDetails";
	private static final String TRAINER_TRAINING_CREATE_UPDATE = "trainer/trainings/trainingCreateOrUpdate";
	private static final String TRAINER_TRAINING_CREATE_UPDATE_REDIRECT = "redirect:/trainer/{trainerUsername}/clients/{clientId}/trainings/";
	private static final String TRAINER_TRAINING_CREATE_UPDATE_REDIRECT_ID = "redirect:/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}";
	private static final String TRAINER_TRAINING_LIST_COPY = "trainer/trainings/listCopyTraining";
	
	//CLIENT
	private static final String CLIENT_TRAINING_LIST = "client/trainings/trainingsList";
	private static final String CLIENT_TRAINING_LIST_REDIRECT_URL = "redirect:/client/{clientUsername}/trainings/";
	private static final String CLIENT_TRAINING_DETAILS = "client/trainings/trainingsDetails";
	private static final String CLIENT_TRAINING_CREATE_UPDATE = "client/trainings/trainingCreateOrUpdate";
	private static final String CLIENT_TRAINING_CREATE_UPDATE_REDIRECT_ID = "redirect:/client/{clientUsername}/trainings/{trainingId}";
	
	//ERRORS
	private static final String INITIAL_DATE = "initialDate";
	private static final String END_DATE = "endDate";
	private static final String INITIAL_DATE_IN_PAST = "The initial date cannot be in the past";
	private static final String END_DATE_IN_PAST = "The end date cannot be in the past";
	private static final String END_BEFORE_INIT = "The end date must be after the initial date";
	private static final String LONGER_THAN_90 = "The training cannot be longer than 90 days";

	private final ClientService clientService;
	private final TrainerService trainerService;
	private final TrainingService trainingService;

	@Autowired
	public TrainingController(final ClientService clientService, final TrainerService trainerService,
			final TrainingService trainingService) {
		this.clientService = clientService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
	}
	
	@Autowired
	private HttpSession httpSession;
	
	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	// TRAINER

	@GetMapping("/trainer/{trainerUsername}/trainings")
	public String clientTrainingList(@PathVariable("trainerUsername") String trainerUsername, Model model) {
		
		Boolean isLogged = isLoggedUser(trainerUsername,true);
		
		if(Boolean.FALSE.equals(isLogged))
			return EXCEPTION;
		
		Trainer trainer = this.trainerService.findTrainer(trainerUsername);
		model.addAttribute("trainer", trainer);
		
		Calendar now = Calendar.getInstance();
		Date date = now.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");		
		String actualDate = dateFormat.format(date);
		
		model.addAttribute(ACTUAL_DATE, actualDate);
		
		return TRAINER_TRAINING_LIST;
	}

	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}")
	public String clientTrainingDetails(@PathVariable("trainerUsername") String trainerUsername,
			@PathVariable("clientId") int clientId, @PathVariable("trainingId") int trainingId, Model model) {
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(Boolean.FALSE.equals(isClientOfLogged))
			return EXCEPTION;
		
		Client client = this.clientService.findClientById(clientId);
		Training training = this.trainingService.findTrainingById(trainingId);

		Calendar now = Calendar.getInstance();
		Date date = now.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");		
		String actualDate = dateFormat.format(date);
		
		model.addAttribute(ACTUAL_DATE, actualDate);
		
		model.addAttribute(CLIENT, client);
		model.addAttribute(TRAINING, training);
		if((training.getRoutines().isEmpty() || training.getRoutines()==null) && training.getDiet()==null) {
			model.addAttribute("hasNotRoutine",true);
		}

		return TRAINER_TRAINING_DETAILS;
	}

	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/create")
	public String initTrainingCreateForm(@PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername, ModelMap model) {
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(Boolean.FALSE.equals(isClientOfLogged))
			return EXCEPTION;
		
		Training training = new Training();
		Client client = this.clientService.findClientById(clientId);

		model.addAttribute(CLIENT, client);
		model.addAttribute(TRAINING, training);

		return TRAINER_TRAINING_CREATE_UPDATE;
	}

	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/create")
	public String processTrainingCreateForm(@Valid Training training, BindingResult result,
			@PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername,
			ModelMap model) {
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(Boolean.FALSE.equals(isClientOfLogged))
			return EXCEPTION;
		
		Client client = this.clientService.findClientById(clientId);
		model.addAttribute(CLIENT, client);
		
		if (result.hasErrors()) {
			model.put(TRAINING, training);
			return TRAINER_TRAINING_CREATE_UPDATE;
		} else {
			if(!training.getAuthor().equals(trainerUsername)||training.getEditingPermission().equals(EditingPermission.CLIENT)) {
				return EXCEPTION;
			}
			
			Boolean saveTrainingSuccessful = trySaveTraining(training,client,result);
			
			if(Boolean.FALSE.equals(saveTrainingSuccessful)) {
				return TRAINER_TRAINING_CREATE_UPDATE;
			}
			else {
				List<Training> allTrainingsClient = new ArrayList<>(this.trainingService.findTrainingFromClient(clientId));
				Training newTraining = allTrainingsClient.get(allTrainingsClient.size()-1);
				return TRAINER_TRAINING_CREATE_UPDATE_REDIRECT+newTraining.getId();
			}
			
		} 
	}
	
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit")
	public String initTrainingUpdateForm(@PathVariable("trainingId") int trainingId, @PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername, Model model) {
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(Boolean.FALSE.equals(isClientOfLogged)||training.getEditingPermission().equals(EditingPermission.CLIENT)) {
			return EXCEPTION;
		}
		
		Client client = this.clientService.findClientById(clientId);
		
		model.addAttribute(END_DATE_AUX, training.getEndDate());
		model.addAttribute(ACTUAL_DATE, getActualDate());
		model.addAttribute(TRAINING, training);
		model.addAttribute(CLIENT, client);
		return TRAINER_TRAINING_CREATE_UPDATE;
	}
	
	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/edit")
	public String processTrainingUpdateForm(@Valid Training training, BindingResult result, 
		@PathVariable("trainingId") int trainingId, @PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername, ModelMap model) {
		
		Training oldTraining = this.trainingService.findTrainingById(trainingId);
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(Boolean.FALSE.equals(isClientOfLogged)||oldTraining.getEditingPermission().equals(EditingPermission.CLIENT)) {
			return EXCEPTION;
		}

		Client client = this.clientService.findClientById(clientId);
		Date now = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String actualDate = dateFormat.format(now);
		
		model.addAttribute(END_DATE_AUX, oldTraining.getEndDate());
		model.addAttribute(ACTUAL_DATE, actualDate);
		model.addAttribute(CLIENT, client);
		
		training.setId(trainingId);
		
		if (result.hasErrors()) {
			model.put(TRAINING, training);
			return TRAINER_TRAINING_CREATE_UPDATE;
		} 
		else {
			
			String oldTrainingEndDate = dateFormat.format(oldTraining.getEndDate());
			String newTrainingEndDate = dateFormat.format(training.getEndDate());
			
			if(oldTraining.getEndDate().before(now)&&!newTrainingEndDate.equals(oldTrainingEndDate)
				||training.getEditingPermission().equals(EditingPermission.CLIENT)
				||(!oldTraining.getAuthor().equals(trainerUsername)&&!training.getEditingPermission().equals(oldTraining.getEditingPermission()))) {
				return EXCEPTION;
			}
			
			training.setAuthor(oldTraining.getAuthor());
			training.setInitialDate(oldTraining.getInitialDate());
			training.setDiet(oldTraining.getDiet());
			training.setRoutines(oldTraining.getRoutines());
			training.setId(trainingId);
			
			Boolean saveTrainingSuccessful = trySaveTraining(training,client,result);
			
			return Boolean.FALSE.equals(saveTrainingSuccessful)?TRAINER_TRAINING_CREATE_UPDATE:TRAINER_TRAINING_CREATE_UPDATE_REDIRECT_ID;
			
		}
	}
	
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/delete")
	public String processDeleteTrainingForm(@PathVariable("trainingId") int trainingId, @PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername,RedirectAttributes redirectAttrs) {
		
		Client client = this.clientService.findClientById(clientId);
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(training==null||Boolean.FALSE.equals(isClientOfLogged)||!training.getAuthor().equals(trainerUsername)) {
			return EXCEPTION;
		}
		else {
			this.trainingService.deleteTraining(training,client);
			redirectAttrs.addFlashAttribute(DELETE_MESSAGE, DELETED_SUCCESSFULLY);
			return TRAINER_TRAINING_LIST_REDIRECT_URL;
		}
	}
	
	//Copy training
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/copyTraining")
	public String getTrainingListCopy(@PathVariable("trainingId") int trainingId, @PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername, ModelMap model) {
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		
		if(Boolean.FALSE.equals(isClientOfLogged)||training.getEditingPermission().equals(EditingPermission.CLIENT)||!isTrainingOfClient(trainingId,clientId)||!isTrainingEmpty(trainingId)) {
			return EXCEPTION;
		}
		
		Collection<Training> trainings = this.trainingService.findTrainingWithPublicClient();		
		if(trainings.isEmpty()) {
			model.addAttribute(NOT_HAVE_PUBLIC, true);
		}else {
			Collection<Training> tr = trainings.stream().filter(Objects::nonNull).filter(t->t.getDiet()!=null || !t.getRoutines().isEmpty()).collect(Collectors.toList());
			if(tr.isEmpty()) {
				model.addAttribute(NOT_HAVE_PUBLIC, true);
			}else {
				model.addAttribute(TRAININGS, tr);
			}
		}
		return TRAINER_TRAINING_LIST_COPY;
	}
	
	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/copyTraining")
	public String processTrainingCopy(@ModelAttribute("trainingIdToCopy") int idTrainingToCopy, @PathVariable("trainingId") int trainingId, @PathVariable("clientId") int clientId, @PathVariable("trainerUsername") String trainerUsername, ModelMap model) {
		
		Client client = this.clientService.findClientById(clientId);
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Boolean isClientOfLogged = isClientOfLoggedTrainer(clientId,trainerUsername);
		Boolean isTrainingOfClient = isTrainingOfClient(trainingId,clientId);
		Boolean isTrainingEmpty = isTrainingEmpty(trainingId);
		Boolean isPublic = this.clientService.isPublicByTrainingId(idTrainingToCopy);
		
		if(Boolean.FALSE.equals(isClientOfLogged)||training.getEditingPermission().equals(EditingPermission.CLIENT)||Boolean.FALSE.equals(isTrainingOfClient)||Boolean.FALSE.equals(isTrainingEmpty)||Boolean.FALSE.equals(isPublic)) {
			return EXCEPTION;
		}
		Training trainingToCopy = this.trainingService.findTrainingById(idTrainingToCopy);
		
		Training nuevo = copyTrainingInfo(trainingToCopy,training);
		
		try {
			this.trainingService.saveTraining(nuevo,client);
		}catch(Exception e) {
			return EXCEPTION;
		}
		
		return TRAINER_TRAINING_LIST_REDIRECT_URL;
	}

	//CLIENT
		
	@GetMapping("/client/{clientUsername}/trainings")
	public String getTrainingList(@PathVariable("clientUsername") String clientUsername, Model model) {
		model.getAttribute("training_id");
		
		Boolean isLogged = isLoggedUser(clientUsername,false);
		
		if(Boolean.FALSE.equals(isLogged))
			return EXCEPTION;
		
		Client client = this.clientService.findClientByUsername(clientUsername);
		Collection<Training> trainings = this.trainingService.findTrainingFromClient(client.getId());
		
		model.addAttribute(TRAININGS,trainings);
		
		return CLIENT_TRAINING_LIST;
	}
	
	@GetMapping("/client/{clientUsername}/trainings/{trainingId}")
	public String getTrainingDetails(@PathVariable("clientUsername") String clientUsername, @PathVariable("trainingId") int trainingId, Model model) {
		
		//Sirve para guardar sesión id para utilizar Spotify
		httpSession.setAttribute("train", trainingId);
		
		Boolean isLogged = isLoggedUser(clientUsername,false);
		
		if(Boolean.FALSE.equals(isLogged))
			return EXCEPTION;
		
		Training training = this.trainingService.findTrainingById(trainingId);
		Client client = this.clientService.findClientByUsername(clientUsername);
		
		model.addAttribute(TRAINING,training);
		model.addAttribute(CLIENT,client);
		
		return CLIENT_TRAINING_DETAILS;
	}
	
	@GetMapping("/client/{clientUsername}/trainings/create")
	public String initTrainingCreateForm(@PathVariable("clientUsername") String clientUsername, ModelMap model) {
		
		Boolean isLogged = isLoggedUser(clientUsername,false);

		if(Boolean.FALSE.equals(isLogged))
			return EXCEPTION;
		
		Training training = new Training();
		Client client = this.clientService.findClientByUsername(clientUsername);

		model.addAttribute(TRAINING, training);
		model.addAttribute(CLIENT, client);

		return CLIENT_TRAINING_CREATE_UPDATE;
	}

	@PostMapping("/client/{clientUsername}/trainings/create")
	public String processTrainingCreateForm(@Valid Training training, BindingResult result,
			@PathVariable("clientUsername") String clientUsername, ModelMap model) {
		
		Boolean isLogged = isLoggedUser(clientUsername,false);

		if(Boolean.FALSE.equals(isLogged))
			return EXCEPTION;
		
		Client client = this.clientService.findClientByUsername(clientUsername);
		model.addAttribute(CLIENT, client);
		
		if (result.hasErrors()) {
			model.put(TRAINING, training);
			return CLIENT_TRAINING_CREATE_UPDATE;
		} else {
			if(!training.getAuthor().equals(clientUsername)||training.getEditingPermission().equals(EditingPermission.TRAINER)) {
				return EXCEPTION;
			}
			
			Boolean saveTrainingSuccessful = trySaveTraining(training,client,result);
			
			if(Boolean.FALSE.equals(saveTrainingSuccessful)) {
				return CLIENT_TRAINING_CREATE_UPDATE;
			}
			else {
				List<Training> allTrainingsClient = new ArrayList<>(this.trainingService.findTrainingFromClient(client.getId()));
				Training newTraining = allTrainingsClient.get(allTrainingsClient.size()-1);
				return CLIENT_TRAINING_LIST_REDIRECT_URL+newTraining.getId();
			}
		
		}
	}
	
	@GetMapping("/client/{clientUsername}/trainings/{trainingId}/edit")
	public String initTrainingUpdateForm(@PathVariable("trainingId") int trainingId, @PathVariable("clientUsername") String clientUsername, Model model) {
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Boolean isLogged = isLoggedUser(clientUsername,false);
		
		if(Boolean.FALSE.equals(isLogged)||training.getEditingPermission().equals(EditingPermission.TRAINER)) {
			return EXCEPTION;
		}
		
		Client client = this.clientService.findClientByUsername(clientUsername);
		
		model.addAttribute(END_DATE_AUX, training.getEndDate());
		model.addAttribute(ACTUAL_DATE, getActualDate());
		model.addAttribute(TRAINING, training);
		model.addAttribute(CLIENT, client);
		return CLIENT_TRAINING_CREATE_UPDATE;
	}
	
	@PostMapping("/client/{clientUsername}/trainings/{trainingId}/edit")
	public String processTrainingUpdateForm(@Valid Training training, BindingResult result, 
		@PathVariable("trainingId") int trainingId, @PathVariable("clientUsername") String clientUsername, ModelMap model) {
		
		Training oldTraining = this.trainingService.findTrainingById(trainingId);
		
		Boolean isLogged = isLoggedUser(clientUsername,false);
		
		if(Boolean.FALSE.equals(isLogged)||oldTraining.getEditingPermission().equals(EditingPermission.TRAINER)) {
			return EXCEPTION;
		}

		Client client = this.clientService.findClientByUsername(clientUsername);
		Date now = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String actualDate = dateFormat.format(now);
		
		model.addAttribute(END_DATE_AUX, oldTraining.getEndDate());
		model.addAttribute(ACTUAL_DATE, actualDate);
		model.addAttribute(CLIENT, client);
		
		training.setId(trainingId);
		
		if (result.hasErrors()) {
			model.put(TRAINING, training);
			return CLIENT_TRAINING_CREATE_UPDATE;
		} 
		else {
						
			String oldTrainingEndDate = dateFormat.format(oldTraining.getEndDate());
			String newTrainingEndDate = dateFormat.format(training.getEndDate());
			
			if(oldTraining.getEndDate().before(now)&&!newTrainingEndDate.equals(oldTrainingEndDate)
				||training.getEditingPermission().equals(EditingPermission.TRAINER)
				||(!oldTraining.getAuthor().equals(clientUsername)&&!training.getEditingPermission().equals(oldTraining.getEditingPermission()))) {
				return EXCEPTION;
			}
			
			training.setAuthor(oldTraining.getAuthor());
			training.setInitialDate(oldTraining.getInitialDate());
			training.setDiet(oldTraining.getDiet());
			training.setRoutines(oldTraining.getRoutines());
			training.setId(trainingId);
			
			Boolean saveTrainingSuccessful = trySaveTraining(training,client,result);
			
			return Boolean.FALSE.equals(saveTrainingSuccessful)?CLIENT_TRAINING_CREATE_UPDATE:CLIENT_TRAINING_CREATE_UPDATE_REDIRECT_ID;
		}
	}
	
	@GetMapping("/client/{clientUsername}/trainings/{trainingId}/delete")
	public String processDeleteTrainingForm(@PathVariable("trainingId") int trainingId, @PathVariable("clientUsername") String clientUsername,RedirectAttributes redirectAttrs) {
		
		Client client = this.clientService.findClientByUsername(clientUsername);
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Boolean isLogged = isLoggedUser(clientUsername,false);
				
		if(training==null||Boolean.FALSE.equals(isLogged)||!training.getAuthor().equals(clientUsername)) {
			return EXCEPTION;
		}
		else {
			this.trainingService.deleteTraining(training,client);
			redirectAttrs.addFlashAttribute(DELETE_MESSAGE, DELETED_SUCCESSFULLY);
			return CLIENT_TRAINING_LIST_REDIRECT_URL;
		}
	}
	
	//Derivative
	
	private Boolean isClientOfLoggedTrainer(final int clientId, final String trainerUsername) {		
		Trainer trainer = this.trainerService.findTrainer(trainerUsername);
		Client client = this.clientService.findClientById(clientId);
		
		return isLoggedUser(trainerUsername,true) && trainer.getClients().contains(client);
	}
	
	private Boolean isLoggedUser(final String usernameURL, boolean isTrainer) {
		
		User user;
		
		if(isTrainer) {
			Trainer trainer = this.trainerService.findTrainer(usernameURL);
			user = trainer.getUser();
		}
		else {
			Client client = this.clientService.findClientByUsername(usernameURL);
			user = client.getUser();
		}
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = "";
		
		if(principal instanceof UserDetails) {
			username = ((UserDetails)principal).getUsername();
		}
		else {
			username = principal.toString();
		}
		
		return user.getUsername().equals(username);
	}
	
	private String getActualDate() {
		Date now = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(now);
	}
	
	//Copy Training
	
	private boolean isTrainingEmpty(int trainingId) {
		Training training = this.trainingService.findTrainingById(trainingId);
		return training.getDiet()==null && training.getRoutines().isEmpty();
	}

	private boolean isTrainingOfClient(int trainingId, int clientId) {
		Collection<Integer> list = this.trainingService.findTrainingIdFromClient(clientId);
		return list.contains(trainingId);
	}
	
	private Training copyTrainingInfo(Training trainingToCopy, Training emptyTraining) {
		Training nuevo = new Training();
		if(trainingToCopy.getDiet()!=null) {
			nuevo.setDiet(trainingToCopy.getDiet());
		}
		if(trainingToCopy.getRoutines()!=null) {
			Collection<Routine> routines = new ArrayList<>();
			for(Routine r : trainingToCopy.getRoutines()) {
				Routine nueva = new Routine();
				if(r.getRoutineLine()!=null) {
					Collection<RoutineLine> routinesLines = new ArrayList<>();
					for(RoutineLine rl : r.getRoutineLine()) {
						RoutineLine nuevaRl = new RoutineLine();
						nuevaRl.setExercise(rl.getExercise());
						nuevaRl.setReps(rl.getReps());
						nuevaRl.setSeries(rl.getSeries());
						nuevaRl.setTime(rl.getTime());
						nuevaRl.setWeight(rl.getWeight());
						routinesLines.add(nuevaRl);
					}
					nueva.setRoutineLine(routinesLines);
				}
				nueva.setDescription(r.getDescription());
				nueva.setName(r.getName());
				nueva.setRepsPerWeek(r.getRepsPerWeek());
				routines.add(nueva);
			}
			nuevo.setRoutines(routines);
		}
		nuevo.setAuthor(emptyTraining.getAuthor());
		nuevo.setEditingPermission(emptyTraining.getEditingPermission());
		nuevo.setEndDate(emptyTraining.getEndDate());
		nuevo.setId(emptyTraining.getId());
		nuevo.setInitialDate(emptyTraining.getInitialDate());
		nuevo.setName(emptyTraining.getName());
		
		return nuevo;
	}
	
	private Boolean trySaveTraining(Training training, Client client, BindingResult result) {
		
		Boolean isSuccessful = true;
		
		try {			
			this.trainingService.saveTraining(training,client);
		} 
		catch (PastInitException e) {
			result.rejectValue(INITIAL_DATE, null, INITIAL_DATE_IN_PAST);
			isSuccessful = false;
		}
		catch (PastEndException e) {
			result.rejectValue(END_DATE, null, END_DATE_IN_PAST);
			isSuccessful = false;
		}
		catch (EndBeforeEqualsInitException e) {
			result.rejectValue(END_DATE, null, END_BEFORE_INIT);
			isSuccessful = false;
		}
		catch (LongerThan90DaysException e) {
			result.rejectValue(END_DATE, null, LONGER_THAN_90);
			isSuccessful = false;
		}
		catch (Exception e) {
			this.rejectTrainingDateError(e, result);
			isSuccessful = false;
		}
		
		return isSuccessful;
	}
	
	private void rejectTrainingDateError(Exception e, BindingResult result) {
		if(e instanceof InitInTrainingException) {
			InitInTrainingException ex = (InitInTrainingException) e;
			result.rejectValue(INITIAL_DATE, null, "The training cannot start in a period "
				+ "with other training (The other training is from " + ex.getInitAssoc() + " to " + ex.getEndAssoc() + ")");
		}
		else if (e instanceof EndInTrainingException) {
			EndInTrainingException ex = (EndInTrainingException) e;
			result.rejectValue(END_DATE, null, "The training cannot end in a period "
				+ "with other training (The other training is from " + ex.getInitAssoc() + " to " + ex.getEndAssoc() + ")");
		}
		else if (e instanceof PeriodIncludingTrainingException) {
			PeriodIncludingTrainingException ex = (PeriodIncludingTrainingException) e;
			result.rejectValue(END_DATE, null, "The training cannot be in a period "
				+ "which includes another training (The other training is from " + ex.getInitAssoc() + " to " + ex.getEndAssoc() + ")");
		}
	}
	
}
