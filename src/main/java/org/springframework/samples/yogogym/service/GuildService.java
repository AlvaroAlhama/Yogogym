package org.springframework.samples.yogogym.service;


import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Guild;
import org.springframework.samples.yogogym.model.Inscription;
import org.springframework.samples.yogogym.model.Enums.Status;
import org.springframework.samples.yogogym.repository.ClientRepository;
import org.springframework.samples.yogogym.repository.GuildRepository;
import org.springframework.samples.yogogym.service.exceptions.ChallengeMore3Exception;
import org.springframework.samples.yogogym.service.exceptions.ChallengeSameNameException;
import org.springframework.samples.yogogym.service.exceptions.GuildLogoException;
import org.springframework.samples.yogogym.service.exceptions.GuildNotUserException;
import org.springframework.samples.yogogym.service.exceptions.GuildSameCreatorException;
import org.springframework.samples.yogogym.service.exceptions.GuildSameNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuildService {

	private GuildRepository guildRepository;
	private ClientRepository clientRepository;
	
	@Autowired
	public GuildService(GuildRepository guildRepository, ClientRepository clientRepository) {
		this.guildRepository = guildRepository;
		this.clientRepository = clientRepository;
	}

	@Transactional
	public Collection<Guild> findAllGuild() throws DataAccessException {
		Collection<Guild> res = this.guildRepository.findAllGuilds();
		
		return res;		
	}
	
	@Transactional
	public Guild findGuildById(Integer guildId) throws DataAccessException {
		
		Guild res = this.guildRepository.findGuildById(guildId);
		return res;		
	}
	
	@Transactional
	public Collection<Client> findAllClientesByGuild(Guild guild) throws DataAccessException {
		
		Collection<Client> res = this.guildRepository.findClientsByGuild(guild);
		return res;		
	}
	
	@Transactional(rollbackFor = {GuildSameCreatorException.class, GuildSameNameException.class, GuildLogoException.class})
	public void saveGuild(Guild guild) throws DataAccessException, GuildSameCreatorException, GuildSameNameException, GuildLogoException {
		
		Collection<Guild> guilds = this.guildRepository.findAllGuilds();
		
		if(guild.getId() != null) {
			guilds = guilds.stream().filter(c -> c.getId() != guild.getId()).collect(Collectors.toList());
		}
			if(guilds.stream().anyMatch(c->c.getName().equals(guild.getName()))) {
				throw new GuildSameNameException();
				
			}else if(guilds.stream().anyMatch(c->c.getCreator().equals(guild.getCreator()))) {
				
				throw new GuildSameCreatorException();

			}else if(!guild.getLogo().startsWith("https://")){
				
				throw new GuildLogoException();
			}else {
		
				this.guildRepository.save(guild);
			}
	}
	@Transactional(rollbackFor = {GuildNotUserException.class})
	public void deleteGuild(Guild guild, String clientUsername) throws DataAccessException ,GuildNotUserException{
		
		Collection<Client> clients = this.guildRepository.findClientsByGuild(guild);
		if(!guild.getCreator().equals(clientUsername)){
			throw new GuildNotUserException();
		}else {
		for(Client c: clients) {
			c.setGuild(null);
			this.clientRepository.save(c);
		}
		
		this.guildRepository.delete(guild);
		}
	}
	
	@Transactional
	public void joinGuild(String clientUsername, Guild guild) throws DataAccessException{
		
		Client client = this.clientRepository.findClientByUsername(clientUsername);
		client.setGuild(guild);
	}
	
	@Transactional
	public void leaveGuild(String clientUsername) throws DataAccessException{
		
		Client client = this.clientRepository.findClientByUsername(clientUsername);
		client.setGuild(null);
	}
}
