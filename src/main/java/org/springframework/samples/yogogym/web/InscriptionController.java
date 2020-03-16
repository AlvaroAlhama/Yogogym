package org.springframework.samples.yogogym.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.yogogym.model.Challenge;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Inscription;
import org.springframework.samples.yogogym.model.Enums.Status;
import org.springframework.samples.yogogym.service.ChallengeService;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.InscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InscriptionController {

	
	private ChallengeService challengeService;
	private InscriptionService inscriptionService;
	private ClientService clientService;
	
	@Autowired
	public InscriptionController(ChallengeService challengeService, InscriptionService inscriptionService, ClientService clientService) {
		this.challengeService = challengeService;
		this.inscriptionService = inscriptionService;
		this.clientService = clientService;
	}
		
	
	// ADMIN:
	
	@PostMapping("/admin/challenges/submitted/{challengeId}/inscription/{inscriptionId}/evaluate")
	public String evaluateChallengeAdmin(@PathVariable("challengeId") int challengeId, @PathVariable("inscriptionId") int inscriptionId,
			                             Inscription inscription, Model model) {

		Challenge challenge = this.challengeService.findChallengeById(challengeId);

		inscription.setChallenge(challenge);
		inscription.setId(inscriptionId);
		
		this.inscriptionService.saveInscription(inscription);
		
		return "redirect:/admin/challenges/submitted";
	}
	
	// CLIENT:
	
	@GetMapping("/client/{clientUsername}/challenges/{challengeId}/inscription/create")
	public String createInscriptionByChallengeId(@PathVariable("clientUsername") String clientUsername, @PathVariable("challengeId") int challengeId, Model model) {	  
		
		Challenge challenge = this.challengeService.findChallengeById(challengeId);
		Client client = this.clientService.findClientByClientUsername(clientUsername);
	   	Inscription inscription = new Inscription();
	   	
	   	inscription.setChallenge(challenge);
	   	inscription.setStatus(Status.PARTICIPATING);
	   	this.inscriptionService.saveInscription(inscription);
	   	
	   	client.addInscription(inscription);
	   	this.clientService.saveClient(client);
	   	
	   	
	    return "redirect:/client/"+clientUsername+"/challenges";
	}
	
	@PostMapping("/client/{clientUsername}/challenges/{challengeId}/inscription/{inscriptionId}/submit")
	public String submitInscription(@PathVariable("clientUsername") String clientUsername, @PathVariable("challengeId") int challengeId, 
									@PathVariable("inscriptionId") int inscriptionId, Inscription inscription, Model model) {	  
		
		Challenge challenge = this.challengeService.findChallengeById(challengeId);
		inscription.setChallenge(challenge);
		inscription.setId(inscriptionId);
		inscription.setStatus(Status.SUBMITTED);
		
	   	this.inscriptionService.saveInscription(inscription);
	   	
	    return "redirect:/client/"+clientUsername+"/challenges/mine/"+challengeId;
	}
	
}
