package org.springframework.samples.yogogym.web;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Guild;
import org.springframework.samples.yogogym.model.Inscription;
import org.springframework.samples.yogogym.model.Enums.Status;
import org.springframework.samples.yogogym.service.ClientService;
import org.springframework.samples.yogogym.service.ForumService;
import org.springframework.samples.yogogym.service.GuildService;
import org.springframework.samples.yogogym.service.exceptions.GuildLogoException;
import org.springframework.samples.yogogym.service.exceptions.GuildNotCreatorException;
import org.springframework.samples.yogogym.service.exceptions.GuildSameCreatorException;
import org.springframework.samples.yogogym.service.exceptions.GuildSameNameException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GuildController {
	

	private final ClientService clientService;
	private final GuildService guildService;
	private final ForumService forumService;
	
	@Autowired
	public GuildController(final ClientService clientService, final GuildService guildService,final ForumService forumService) {
		this.clientService = clientService;
		this.guildService = guildService;
		this.forumService = forumService;
	}
	
	
	@GetMapping("/client/{clientUsername}/guilds")
	public String ClienGuildList(@PathVariable("clientUsername") String clientUsername, Model model) {
		
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Client client = this.clientService.findClientByUsername(clientUsername);
		Collection<Guild> allGuilds = this.guildService.findAllGuild();
		
		model.addAttribute("client", client);
		model.addAttribute("allGuilds",allGuilds);
		return "client/guilds/guildsList";
	}
	
	@GetMapping("/client/{clientUsername}/guilds/{guildId}")
	public String ClientGuildDetails(@PathVariable("clientUsername") String clientUsername,
			 @PathVariable("guildId") int guildId, Model model) {
		
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Guild guild = this.guildService.findGuildById(guildId);
		Collection<Client> clients = this.guildService.findAllClientesByGuild(guild);
		Client client = this.clientService.findClientByUsername(clientUsername);
		Integer points = 0;
		for(Client c : clients) {
			Collection<Inscription> ins = c.getInscriptions();
			for(Inscription i : ins) {
				if(i.getStatus().equals(Status.SUBMITTED)) {
					points+=i.getChallenge().getPoints();
				}
			}
		}
		int forumId = this.forumService.findForumIdByGuildId(guildId);
		
		model.addAttribute("client",client);
		model.addAttribute("clients",clients.size());
		model.addAttribute("points",points);
		model.addAttribute("guild", guild);
		model.addAttribute("forumId",forumId);

		return "client/guilds/guildsDetails";
	}
	
	@GetMapping("client/{clientUsername}/guilds/create")
	public String initGuildCreateForm(@PathVariable("clientUsername") String clientUsername,
			final ModelMap model) {
		
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Guild guild = new Guild();
		Client client = this.clientService.findClientByUsername(clientUsername);
		guild.setCreator(clientUsername);
		model.addAttribute("guild", guild);
		model.addAttribute("client", client);
			
		return "client/guilds/guildsCreateOrUpdate";
		
	}
	
	@PostMapping("client/{clientUsername}/guilds/create")
	public String processGuildCreationForm(@Valid Guild guild,BindingResult result,
			@PathVariable("clientUsername") String clientUsername, final ModelMap model) {

		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Client client = this.clientService.findClientByUsername(clientUsername);
	
		if (result.hasErrors()) {
			
			model.addAttribute("client", client);
			model.addAttribute("guild", guild);
			
			return "client/guilds/guildsCreateOrUpdate";
		} else {
			
			try {
			client.setGuild(guild);
			this.guildService.saveGuild(guild,client);
			}catch(Exception ex){
				if(ex instanceof GuildSameNameException) {
					result.rejectValue("name", "required: ", "There is already a guild with that name");
				}else if (ex instanceof GuildSameCreatorException) {
					result.rejectValue("creator", "required: ", "There is already a guild created by this creator");
				}else if(ex instanceof GuildLogoException) {
					result.rejectValue("logo", "required: ", "The link must start with https://");
				}else if(ex instanceof GuildNotCreatorException) {
					result.rejectValue("creator", "required: ", "The creator has to be the same as the user logged");
				}
				return "client/guilds/guildsCreateOrUpdate";
			}
			return "redirect:/client/" + clientUsername + "/guilds";
		}
	}
	
	
	
	@GetMapping("client/{clientUsername}/guilds/{guildId}/edit")
	public String initGuildEditForm(@PathVariable("clientUsername") String clientUsername,@PathVariable("guildId") int guildId,
			final ModelMap model) {
		
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
	
		Guild guild = this.guildService.findGuildById(guildId);
		if(!guild.getCreator().equals(clientUsername)) 
			return "exception";
		Client client = this.clientService.findClientByUsername(clientUsername);
		guild.setCreator(clientUsername);
		model.addAttribute("guild", guild);
		model.addAttribute("client", client);
			
		return "client/guilds/guildsCreateOrUpdate";
		
	}
	
	@PostMapping("client/{clientUsername}/guilds/{guildId}/edit")
	public String processGuildEditForm(@PathVariable("clientUsername") String clientUsername,
			@PathVariable("guildId") int guildId, @Valid Guild guild,  BindingResult result,ModelMap model) {
		
			if(!isLoggedPrincipal(clientUsername))
				return "exception";
		
			Client client = this.clientService.findClientByUsername(clientUsername);
			if(result.hasErrors()) {
				model.put("guild", guild);
				
				model.addAttribute("client", client);
				
				return "client/guilds/guildsCreateOrUpdate";
			}else {
				try {
					
				guild.setId(guildId);
				this.guildService.saveGuild(guild,client);
				
				}catch(Exception ex){
					if(ex instanceof GuildSameNameException) {
						result.rejectValue("name", "required: ", "There is already a guild with that name");	
					}else if(ex instanceof GuildLogoException) {
						result.rejectValue("logo", "required: ", "The link must start with https://");
					}else if(ex instanceof GuildNotCreatorException) {
						result.rejectValue("creator", "required: ", "You can't change the creator");
					}
					return "client/guilds/guildsCreateOrUpdate";
				}
				return "redirect:/client/" + clientUsername + "/guilds/"+guildId;
			}
	}
	
	@GetMapping("client/{clientUsername}/guilds/{guildId}/delete")
	public String deleteGuild(@PathVariable("clientUsername") String clientUsername,@PathVariable("guildId") int guildId, RedirectAttributes redirectAttrs, Model model) {
		
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Guild guild = this.guildService.findGuildById(guildId);
		redirectAttrs.addFlashAttribute("deleteMessage", "The Guild was deleted successfully");
		if(!guild.getCreator().equals(clientUsername))
			return "exception";
			
		this.guildService.deleteGuild(guild,clientUsername);
		return "redirect:/client/{clientUsername}/guilds";
	}
	
	@GetMapping("client/{clientUsername}/guilds/{guildId}/join")
	public String joinGuild(@PathVariable("clientUsername") String clientUsername, @PathVariable("guildId")int guildId, Model model){
		
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Guild guild = this.guildService.findGuildById(guildId);
		
		this.guildService.joinGuild(clientUsername, guild);
		
		return "redirect:/client/{clientUsername}/guilds";
	}
	
	@GetMapping("client/{clientUsername}/guilds/{guildId}/leave")
	public String leaveGuild(@PathVariable("clientUsername") String clientUsername, @PathVariable("guildId")int guildId, Model model){
	
		if(!isLoggedPrincipal(clientUsername))
			return "exception";
		
		Client client = this.clientService.findClientByUsername(clientUsername);
		Guild guild = this.guildService.findGuildById(guildId);
		if(!client.getGuild().equals(guild))
			return "exception";
		
		this.guildService.leaveGuild(client,guild);
		return "redirect:/client/{clientUsername}/guilds";
	}
	
	Boolean checkGuildNotSameName(final Guild guild, Collection<Guild> guilds){
		Boolean res = true;		
			
		//Para Sprint 2 cambiar a un método de service y realziar respectivas pruebas
		Error:
		for(Guild g: guilds)
		{
			if(g.getId() != guild.getId())
			{
				if(g.getName().trim().toLowerCase().equals(guild.getName().trim().toLowerCase()))
				{	
					res = false;
					break Error;
				}
			}
		}

		return res;
	}
	
private boolean isLoggedPrincipal(String Username) {
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String principalUsername;
		if (principal instanceof UserDetails) {
			principalUsername = ((UserDetails) principal).getUsername();
		} else {
			principalUsername = principal.toString();
		}
		
		return principalUsername.trim().toLowerCase().equals(Username.trim().toLowerCase());
	}
}
