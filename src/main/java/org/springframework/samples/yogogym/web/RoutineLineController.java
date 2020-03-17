
package org.springframework.samples.yogogym.web;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Exercise;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.RoutineLine;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.ExerciseService;
import org.springframework.samples.yogogym.service.RoutineLineService;
import org.springframework.samples.yogogym.service.RoutineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RoutineLineController {

	private final RoutineService routineService;
	private final ExerciseService exerciseService;
	private final ClientService clientService;
	private final RoutineLineService routineLineService;
	
	@Autowired
	public RoutineLineController(final RoutineService routineService, final ExerciseService exerciseService,
			final ClientService clientService, final RoutineLineService routineLineService) {
		this.routineService = routineService;
		this.exerciseService = exerciseService;
		this.clientService = clientService;
		this.routineLineService = routineLineService;
	
	}

	@InitBinder("routineLine")
	public void initRoutineLineBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new RoutineLineValidator(routineLineService));
	}
	
	// TRAINER

	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/routines/{routineId}/routineLine/create")
	public String initRoutineLineCreateForm(@PathVariable("clientId") int clientId,
			@PathVariable("routineId") int routineId, final ModelMap model) {
		
		RoutineLine routineLine = new RoutineLine();
		
		Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
		Map<Integer,String> selectVals = new TreeMap<>();
		
		for(Exercise e:exerciseCollection)
		{
			selectVals.put(e.getId(), e.getName());
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
			@PathVariable("clientId") int clientId, @PathVariable("trainingId") int trainingId, final ModelMap model) {
					
		if (result.hasErrors()) {
			
			Client client = this.clientService.findClientById(clientId);
			
			Collection<Exercise> exerciseCollection = this.exerciseService.findAllExercise();
			Map<Integer,String> selectVals = new TreeMap<>();
			
			for(Exercise e:exerciseCollection)
			{
				selectVals.put(e.getId(), e.getName());
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

			this.routineService.saveRoutine(routine);

			return "redirect:/trainer/" + trainerUsername + "/clients/" + clientId + "/trainings/"
					+ trainingId + "/routines/" + routineId;
		}
	}
}
