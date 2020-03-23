package org.springframework.samples.yogogym.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Diet;
import org.springframework.samples.yogogym.model.Trainer;
import org.springframework.samples.yogogym.model.Training;
import org.springframework.samples.yogogym.model.Enums.DietType;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.DietService;
import org.springframework.samples.yogogym.service.TrainerService;
import org.springframework.samples.yogogym.service.TrainingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
public class DietController {

	private final ClientService clientService;
	private final TrainerService trainerService;
	private final TrainingService trainingService;
	private final DietService dietService;

	@Autowired
	public DietController(final ClientService clientService, final TrainerService trainerService,
			final DietService dietService,final TrainingService trainingService) {
		this.clientService = clientService;
		this.trainerService = trainerService;
		this.dietService = dietService;
		this.trainingService = trainingService;

	}

	// TRAINER

	// LIST
	@GetMapping("/trainer/{trainerUsername}/diets")
	public String ClienDietList(@PathVariable("trainerUsername") String trainerUsername, Model model) {
		Trainer trainer = this.trainerService.findTrainer(trainerUsername);
		model.addAttribute("trainer", trainer);

		return "trainer/diets/dietsList";
	}

	// GET
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/diets/{dietId}")
	public String ClientDietDetails(@PathVariable("trainerUsername") String trainerUsername,
			@PathVariable("clientId") int clientId, @PathVariable("dietId") int dietId, @PathVariable("trainingId") int trainingId, Model model) {
		Client client = this.clientService.findClientById(clientId);
		Diet diet = this.dietService.findDietById(dietId);
		Training training = this.trainingService.findTrainingById(trainingId);
		
		model.addAttribute("client", client);
		model.addAttribute("diet", diet);
		model.addAttribute("training", training);

		return "trainer/diets/dietsDetails";
	}

	// POST
	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/diets/create")
	public String initDietCreateForm(@PathVariable("clientId") int clientId,
			@PathVariable("trainingId") int trainingId, Model model) {

		Diet diet = new Diet();
		Client client = this.clientService.findClientById(clientId);
		Training training = this.trainingService.findTrainingById(trainingId);
		List<DietType> dietTypes = Arrays.asList(DietType.values());

		model.addAttribute("diet", diet);
		model.addAttribute("client", client);
		model.addAttribute("dietTypes", dietTypes);
		model.addAttribute("training", training);

		return "trainer/diets/dietsCreateOrUpdate";
	}

	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/diets/create")
	public String processDietCreateForm(@Valid Diet diet, BindingResult result, @PathVariable("clientId") int clientId,
			@PathVariable("trainerUsername") String trainerUsername, 
			@PathVariable("trainingId") int trainingId, Model model) {

		Client client = this.clientService.findClientById(clientId);
		Trainer trainer = this.trainerService.findTrainer(trainerUsername);
		Training training = this.trainingService.findTrainingById(trainingId);

		Double HarrisBenedict = (10 * client.getWeight()) + (6.25 * client.getHeight()) - (5 * client.getAge()) + 5;
		Double KatchMcArdle = 21.6 * (client.getWeight() - client.getWeight() * client.getFatPercentage() / 100) + 370;
		Double TMB = (HarrisBenedict + KatchMcArdle) / 2;
		Double caloriasMantemiento = TMB * 1.65;
		Double proteins;
		Double carbs;

		switch (diet.getType()) {
			case VOLUME:
				caloriasMantemiento = caloriasMantemiento * 1.125;
				proteins = 2.2 * client.getWeight();
				carbs = 5 * client.getWeight();
				break;
			case DEFINITION:
				caloriasMantemiento = caloriasMantemiento * 0.85;
				proteins = 2.2 * client.getWeight();
				carbs = 3 * client.getWeight();
				break;
			case EXTREME_DEFINITION:
				caloriasMantemiento = client.getWeight() * 22;
				proteins = 2.2 * client.getWeight();
				carbs =  client.getWeight();
				break;
			default:
				proteins = 2 * client.getWeight();
				carbs = 4* client.getWeight();
		}
		
		Double fat = (caloriasMantemiento - (proteins + carbs) * 4) / 9;

		diet.setKcal(caloriasMantemiento.intValue());
		diet.setProtein(proteins.intValue());
		diet.setCarb(carbs.intValue());
		diet.setFat(fat.intValue());

		if (result.hasErrors()) {
			return "trainer/diets/dietsCreateOrUpdate";
		} else {
			// client.getDiets().add(diet);
			// this.clientService.saveClient(client);
			
			training.setDiet(diet);
			this.trainingService.saveTraining(training);

			this.dietService.saveDiet(diet);

			return "redirect:/trainer/" + trainer.getUser().getUsername() + "/diets";
		}
	}

	@GetMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/diets/{dietId}/edit")
	public String initTrainingUpdateForm(@PathVariable("clientId") int clientId,
			@PathVariable("trainingId") int trainingId, Model model) {

		Diet diet = new Diet();
		Client client = this.clientService.findClientById(clientId);
		Training training = this.trainingService.findTrainingById(trainingId);
		List<DietType> dietTypes = Arrays.asList(DietType.values());

		
		model.addAttribute("diet", diet);
		model.addAttribute("client", client);
		model.addAttribute("dietTypes", dietTypes);
		model.addAttribute("training", training);

		return "trainer/diets/dietsCreateOrUpdate";
	}
	
	@PostMapping("/trainer/{trainerUsername}/clients/{clientId}/trainings/{trainingId}/diets/{dietId}/edit")
	public String processTrainingUpdateForm(@Valid Diet diet, BindingResult result, @PathVariable("clientId") int clientId,
			@PathVariable("trainerUsername") String trainerUsername, @PathVariable("trainingId") int trainingId,  Model model) {
		
		// Client client = this.clientService.findClientById(clientId);
		Trainer trainer = this.trainerService.findTrainer(trainerUsername);
		Training training = this.trainingService.findTrainingById(trainingId);

		if (result.hasErrors()) {
			return "trainer/diets/dietsCreateOrUpdate";
		} 
		else {
			// client.getDiets().add(diet);
			// this.clientService.saveClient(client);
			
			training.setDiet(diet);
			this.trainingService.saveTraining(training);

			this.dietService.saveDiet(diet);

			return "redirect:/trainer/" + trainer.getUser().getUsername() + "/clients/" + clientId +
			 "/trainings/" + trainingId + "/diets/" + diet.getId();
		}
	}
}