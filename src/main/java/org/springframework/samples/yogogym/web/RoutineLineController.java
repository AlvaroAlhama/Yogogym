
package org.springframework.samples.yogogym.web;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Exercise;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.RoutineLine;
import org.springframework.samples.yogogym.model.Trainer;
import org.springframework.samples.yogogym.model.Training;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.ExerciseService;
import org.springframework.samples.yogogym.service.RoutineLineService;
import org.springframework.samples.yogogym.service.RoutineService;
import org.springframework.samples.yogogym.service.TrainerService;
import org.springframework.samples.yogogym.service.TrainingService;
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
public class RoutineLineController {

	private final RoutineService routineService;
	private final ExerciseService exerciseService;
	private final ClientService clientService;
	private final RoutineLineService routineLineService;
	private final TrainerService trainerService;
	private final TrainingService trainingService;
	
	@Autowired
	public RoutineLineController(final RoutineService routineService, final ExerciseService exerciseService,
			final ClientService clientService, final RoutineLineService routineLineService, final TrainerService trainerService, final TrainingService trainingService) {
		this.routineService = routineService;
		this.exerciseService = exerciseService;
		this.clientService = clientService;
		this.routineLineService = routineLineService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
	
	}

	@InitBinder("routineLine")
	public void initRoutineLineBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new RoutineLineValidator(routineLineService,exerciseService));
	}
	
	// TRAINER
	@GetMapping("/trainer/routineLine/ExerciseDetails/{exerciseId}")
	public String getEsto(@PathVariable("exerciseId") final int exerciseId, Model model)
	{
		Exercise exercise = this.exerciseService.findExerciseById(exerciseId);
		
		model.addAttribute("exercise",exercise);
		
		return "trainer/routines/routineLineExerciseDetails";
	}
	
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/routineLine/create")
	public String initRoutineLineCreateForm(@PathVariable("clientId") int clientId,
			@PathVariable("routineId") int routineId, @PathVariable("trainerUsername")final String trainerUsername, @PathVariable("trainingId")final int trainingId, final ModelMap model) {
		
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isClientOfLoggedTrainer(clientId) || !isLoggedTrainer(trainerUsername))
			return "exception";
		
		RoutineLine routineLine = new RoutineLine();
		
		Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
		Map<Integer,String> selectVals = new TreeMap<>();
				
		for(Exercise e:exerciseCollection)
		{	
			if(e.getEquipment() != null)
				selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
			else
				selectVals.put(e.getId(),  e.getName());
		}
		
		Client client = this.clientService.findClientById(clientId);
			
		model.addAttribute("routineId", routineId);
		model.addAttribute("client", client);
		model.addAttribute("routineLine", routineLine);
		model.addAttribute("exercises", selectVals);
		
		return "trainer/routines/routinesLineCreateOrUpdate";
	}

	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/routineLine/create")
	public String processRoutineLineCreationForm(@Valid RoutineLine routineLine,BindingResult result, @ModelAttribute("exercise.id")final int exerciseId,
			@PathVariable("trainerUsername") String trainerUsername, @ModelAttribute("routineId") int routineId,
			@PathVariable("clientId") int clientId, @PathVariable("trainingId") int trainingId, final ModelMap model){
				
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isClientOfLoggedTrainer(clientId) || !isLoggedTrainer(trainerUsername))
			return "exception";
		
		if (result.hasErrors()) {
			
			Client client = this.clientService.findClientById(clientId);
			
			Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
			Map<Integer,String> selectVals = new TreeMap<>();
						
			for(Exercise e:exerciseCollection)
			{	
				if(e.getEquipment() != null)
					selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
				else
					selectVals.put(e.getId(),  e.getName());
			}
			
			model.addAttribute("routineId", routineId);
			model.addAttribute("client", client);
			model.addAttribute("routineLine", routineLine);
			
			model.addAttribute("exercises", selectVals);
			
			return "trainer/routines/routinesLineCreateOrUpdate";
		} else {
			
			Exercise exercise = this.exerciseService.findExerciseById(exerciseId);
			routineLine.setExercise(exercise);
			
			Routine routine = this.routineService.findRoutineById(routineId);
			routine.getRoutineLine().add(routineLine);			

			try
			{
				this.routineLineService.saveRoutineLine(routineLine, trainingId);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			return "redirect:/trainer/" + trainerUsername + "/clients/" + clientId + "/trainings/"
					+ trainingId + "/routines/" + routineId;
		}
	}
	
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/routineLine/{routineLineId}/update")
	public String initRoutineLineUpdateForm(@PathVariable("clientId") int clientId, @PathVariable("trainerUsername")final String trainerUsername,
			@PathVariable("routineId") int routineId, @PathVariable("routineLineId") int routineLineId,@PathVariable("trainingId")final int trainingId,final ModelMap model) {
		
		if(!routineExist(routineId) || !routineLineExist(routineLineId) || isTrainingFinished(trainingId) || !isClientOfLoggedTrainer(clientId) || !isLoggedTrainer(trainerUsername))
			return "exception";
		
		RoutineLine routineLine = this.routineLineService.findRoutineLineById(routineLineId);
		
		Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
		Map<Integer,String> selectVals = new TreeMap<>();
		
		for(Exercise e:exerciseCollection)
		{	
			if(e.getEquipment() != null)
				selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
			else
				selectVals.put(e.getId(),  e.getName());
		}
		
		Client client = this.clientService.findClientById(clientId);
			
		model.addAttribute("routineId", routineId);
		model.addAttribute("client", client);
		model.addAttribute("routineLine", routineLine);
		model.addAttribute("exercises", selectVals);
		
		return "trainer/routines/routinesLineCreateOrUpdate";
	}

	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/routineLine/{routineLineId}/update")
	public String processRoutineLineUpdateForm(@Valid RoutineLine routineLine,BindingResult result, @ModelAttribute("exercise.id")final int exerciseId,
			@PathVariable("trainerUsername") String trainerUsername, @ModelAttribute("routineId") int routineId,
			@PathVariable("clientId") int clientId, @PathVariable("trainingId") int trainingId,@PathVariable("routineLineId")final int routineLineId, final ModelMap model, RedirectAttributes redirectAttrs){
			
		if(!routineExist(routineId) || !routineLineExist(routineLineId) || isTrainingFinished(trainingId) || !isClientOfLoggedTrainer(clientId) || !isLoggedTrainer(trainerUsername))
			return "exception";
		
		if (result.hasErrors()) {
			
			routineLine.setId(this.routineLineService.findRoutineLineById(routineLineId).getId());
			
			Client client = this.clientService.findClientById(clientId);
			
			Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
			Map<Integer,String> selectVals = new TreeMap<>();
			
			for(Exercise e:exerciseCollection)
			{	
				if(e.getEquipment() != null)
					selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
				else
					selectVals.put(e.getId(),  e.getName());
			}
			
			model.addAttribute("routineId", routineId);
			model.addAttribute("client", client);
			model.addAttribute("routineLine", routineLine);
			
			model.addAttribute("exercises", selectVals);
			
			return "trainer/routines/routinesLineCreateOrUpdate";
		} else {
						
			Routine routine = this.routineService.findRoutineById(routineId);
			routineLine.setId(routineLineId);
			Exercise exercise = this.exerciseService.findExerciseById(exerciseId);
			routineLine.setExercise(exercise);
	
			try
			{
				this.routineLineService.saveRoutineLine(routineLine,trainingId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			String updateRoutineLine = " 'RoutineLine #" + routineLine.getId() + "' " + "has been updated succesfully from '" + routine.getName() + "'";
			redirectAttrs.addFlashAttribute("updateRoutineLine", updateRoutineLine);
			
			return "redirect:/trainer/" + trainerUsername + "/clients/" + clientId + "/trainings/"
					+ trainingId + "/routines/" + routineId;
		}
	}
	
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/routineLine/{routineLineId}/delete")
	public String deleteRoutineLine(@PathVariable("routineId")int routineId,@PathVariable("routineLineId")int routineLineId, @PathVariable("clientId")int clientId, @PathVariable("trainingId")int trainingId, @PathVariable("trainerUsername")String trainerUsername, Model model, RedirectAttributes redirectAttrs)
	{
		if(!routineExist(routineId) || !routineLineExist(routineLineId) || isTrainingFinished(trainingId) || !isClientOfLoggedTrainer(clientId) || !isLoggedTrainer(trainerUsername))
			return "exception";
		
		RoutineLine routineLine = this.routineLineService.findRoutineLineById(routineLineId);
		Routine routine = this.routineService.findRoutineById(routineId);
		
		try
		{
			this.routineLineService.deleteRoutineLine(routineLine,trainingId);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		String deleteRoutineLine = " 'RoutineLine #" + routineLine.getId() + "' " + "has been deleted succesfully from '" + routine.getName() + "'";
		redirectAttrs.addFlashAttribute("deleteRoutineLine", deleteRoutineLine);
		
		return "redirect:/trainer/"+ trainerUsername + "/clients/" + clientId + "/trainings/" + trainingId + "/routines/" + routineId;
	}
	
	// CLIENT
	@GetMapping("/client/{clientUsername}/trainings/{trainingId}/routines/{routineId}/routineLine/create")
	public String initRoutineLineCreateForm(@PathVariable("trainingId") int trainingId,
			@PathVariable("routineId") int routineId, @PathVariable("clientUsername")final String clientUsername, final ModelMap model) {
		
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isLoggedTrainer(clientUsername) || !isTrainingFromClient(trainingId))
			return "exception";
		
		RoutineLine routineLine = new RoutineLine();
		
		Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
		Map<Integer,String> selectVals = new TreeMap<>();
				
		for(Exercise e:exerciseCollection)
		{	
			if(e.getEquipment() != null)
				selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
			else
				selectVals.put(e.getId(),  e.getName());
		}
		
		Client client = this.clientService.findClientByUsername(clientUsername);
			
		model.addAttribute("routineId", routineId);
		model.addAttribute("client", client);
		model.addAttribute("routineLine", routineLine);
		model.addAttribute("exercises", selectVals);
		
		return "client/routines/routinesLineCreateOrUpdate";
	}
	
	@PostMapping("/client/{clientUsername}/trainings/{trainingId}/routines/{routineId}/routineLine/create")
	public String processRoutineLineCreateForm(@Valid RoutineLine routineLine, BindingResult result, @PathVariable("trainingId") int trainingId,
			@PathVariable("routineId") int routineId, @PathVariable("clientUsername")final String clientUsername, @ModelAttribute("exercise.id")final int exerciseId, final ModelMap model) {
		
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isLoggedTrainer(clientUsername) || !isTrainingFromClient(trainingId))
			return "exception";
		
		if (result.hasErrors()) {
			
			Client client = this.clientService.findClientByUsername(clientUsername);
			
			Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
			Map<Integer,String> selectVals = new TreeMap<>();
						
			for(Exercise e:exerciseCollection)
			{	
				if(e.getEquipment() != null)
					selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
				else
					selectVals.put(e.getId(),  e.getName());
			}
			
			model.addAttribute("routineId", routineId);
			model.addAttribute("client", client);
			model.addAttribute("routineLine", routineLine);
			
			model.addAttribute("exercises", selectVals);
			
			return "trainer/routines/routinesLineCreateOrUpdate";
		} else {
			
			Exercise exercise = this.exerciseService.findExerciseById(exerciseId);
			routineLine.setExercise(exercise);
			
			Routine routine = this.routineService.findRoutineById(routineId);
			routine.getRoutineLine().add(routineLine);			

			try
			{
				this.routineLineService.saveRoutineLine(routineLine, trainingId);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			return "redirect:/client/" + clientUsername + "/trainings/" + trainingId;
		}
	}
	
	@GetMapping("/client/{clientUsername}/trainings/{trainingId}/routines/{routineId}/routineLine/{routineLineId}/update")
	public String initRoutineLineUpdateForm(@PathVariable("clientUsername")final String clientUsername,
			@PathVariable("routineId") int routineId, @PathVariable("routineLineId") int routineLineId,@PathVariable("trainingId")final int trainingId,final ModelMap model) {
		
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isLoggedTrainer(clientUsername) || !isTrainingFromClient(trainingId))
			return "exception";
		
		RoutineLine routineLine = this.routineLineService.findRoutineLineById(routineLineId);
		
		Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
		Map<Integer,String> selectVals = new TreeMap<>();
		
		for(Exercise e:exerciseCollection)
		{	
			if(e.getEquipment() != null)
				selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
			else
				selectVals.put(e.getId(),  e.getName());
		}
		
		Client client = this.clientService.findClientByUsername(clientUsername);
			
		model.addAttribute("routineId", routineId);
		model.addAttribute("client", client);
		model.addAttribute("routineLine", routineLine);
		model.addAttribute("exercises", selectVals);
		
		return "client/routines/routinesLineCreateOrUpdate";
	}
	
	@PostMapping("/client/{clientUsername}/trainings/{trainingId}/routines/{routineId}/routineLine/{routineLineId}/update")
	public String processRoutineLineUpdateForm(@Valid RoutineLine routineLine,BindingResult result, @ModelAttribute("exercise.id")final int exerciseId,
			@PathVariable("clientUsername") String clientUsername, @ModelAttribute("routineId") int routineId,
			@PathVariable("trainingId") int trainingId,@PathVariable("routineLineId")final int routineLineId, final ModelMap model, RedirectAttributes redirectAttrs){
			
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isLoggedTrainer(clientUsername) || !isTrainingFromClient(trainingId))
			return "exception";
		
		if (result.hasErrors()) {
			
			routineLine.setId(this.routineLineService.findRoutineLineById(routineLineId).getId());
			
			Client client = this.clientService.findClientByUsername(clientUsername);
			
			Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
			Map<Integer,String> selectVals = new TreeMap<>();
			
			for(Exercise e:exerciseCollection)
			{	
				if(e.getEquipment() != null)
					selectVals.put(e.getId(),  e.getName()+", "+e.getEquipment().getName());
				else
					selectVals.put(e.getId(),  e.getName());
			}
			
			model.addAttribute("routineId", routineId);
			model.addAttribute("client", client);
			model.addAttribute("routineLine", routineLine);
			
			model.addAttribute("exercises", selectVals);
			
			return "trainer/routines/routinesLineCreateOrUpdate";
		} else {
			
			Routine routine = this.routineService.findRoutineById(routineId);
			routineLine.setId(routineLineId);
			Exercise exercise = this.exerciseService.findExerciseById(exerciseId);
			routineLine.setExercise(exercise);
	
			try
			{
				this.routineLineService.saveRoutineLine(routineLine,trainingId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			String updateRoutineLine = " 'RoutineLine #" + routineLine.getId() + "' " + "has been updated succesfully from '" + routine.getName() + "'";
			redirectAttrs.addFlashAttribute("updateRoutineLine", updateRoutineLine);
			
			return "redirect:/client/" + clientUsername + "/trainings/" + trainingId;
		}
	}
	
	@GetMapping("client/{clientUsername}/trainings/{trainingId}/routines/{routineId}/routineLine/{routineLineId}/delete")
	public String deleteRoutineLine(@PathVariable("routineId")int routineId,@PathVariable("routineLineId")int routineLineId, @PathVariable("trainingId")int trainingId, @PathVariable("clientUsername")String clientUsername, Model model, RedirectAttributes redirectAttrs)
	{
		if(!routineExist(routineId) || isTrainingFinished(trainingId) || !isLoggedTrainer(clientUsername) || !isTrainingFromClient(trainingId))
			return "exception";
		
		RoutineLine routineLine = this.routineLineService.findRoutineLineById(routineLineId);
		Routine routine = this.routineService.findRoutineById(routineId);
		
		try
		{
			this.routineLineService.deleteRoutineLine(routineLine,trainingId);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		String deleteRoutineLine = " 'RoutineLine #" + routineLine.getId() + "' " + "has been deleted succesfully from '" + routine.getName() + "'";
		redirectAttrs.addFlashAttribute("deleteRoutineLine", deleteRoutineLine);
		
		return "redirect:/client/" + clientUsername + "/trainings/" + trainingId;
	}
	
	
	//Security Utils Check
	
	public String getLoggedUsername()
	{
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String trainerUsername = ((UserDetails)principal).getUsername();
		
		return trainerUsername;
	}
	
	protected Boolean routineExist(final int routineId)
	{
		return this.routineService.findRoutineById(routineId) != null;
	}
	
	protected Boolean routineLineExist(final int routineLineId)
	{
		return this.routineLineService.findRoutineLineById(routineLineId) != null;
	}
	
	protected Boolean isLoggedTrainer(final String trainerUsername)
	{		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String principalUsername = ((UserDetails)principal).getUsername();
		
		return principalUsername.trim().toLowerCase().equals(trainerUsername.trim().toLowerCase());
	}
	
	protected Boolean isClientOfLoggedTrainer(final int clientId)
	{		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String trainerUsername = ((UserDetails)principal).getUsername();
		
		Trainer trainer = this.trainerService.findTrainer(trainerUsername);
		Client client = this.clientService.findClientById(clientId);
		
		return trainer.getClients().contains(client);
	}
	
	protected Boolean isTrainingFinished(final int trainingId)
	{
		Calendar now = Calendar.getInstance();
		Date actualDate = now.getTime();
				
		Training training = this.trainingService.findTrainingById(trainingId);
		
		return training.getEndDate().before(actualDate);
	}
	
	public Boolean isTrainingFromClient(final int trainingId)
	{
		Training training = this.trainingService.findTrainingById(trainingId);
		Client client = training.getClient();
	
		return getLoggedUsername().trim().toLowerCase().equals(client.getUser().getUsername().trim().toLowerCase());
	}
}
