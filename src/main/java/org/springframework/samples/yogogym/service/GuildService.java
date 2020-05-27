package org.springframework.samples.yogogym.service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Forum;
import org.springframework.samples.yogogym.model.Guild;
import org.springframework.samples.yogogym.repository.ClientRepository;
import org.springframework.samples.yogogym.repository.ForumRepository;
import org.springframework.samples.yogogym.repository.GuildRepository;
import org.springframework.samples.yogogym.service.exceptions.GuildLogoException;
import org.springframework.samples.yogogym.service.exceptions.GuildNotCreatorException;
import org.springframework.samples.yogogym.service.exceptions.GuildSameCreatorException;
import org.springframework.samples.yogogym.service.exceptions.GuildSameNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuildService {

	private GuildRepository guildRepository;
	private ClientRepository clientRepository;
	private ForumRepository forumRepository;
	
	@Autowired
	public GuildService(GuildRepository guildRepository, ClientRepository clientRepository,ForumRepository forumRepository) {
		this.guildRepository = guildRepository;
		this.clientRepository = clientRepository;
		this.forumRepository = forumRepository;
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
	public Guild findGuildByName(String guildName) throws DataAccessException {
		
		Guild res = this.guildRepository.findGuildByName(guildName);
		return res;		
	}
	 
	@Transactional
	public Collection<Client> findAllClientesByGuild(Guild guild) throws DataAccessException {
		
		Collection<Client> res = this.guildRepository.findClientsByGuild(guild);
		return res;		
	}
	
	@CacheEvict(cacheNames = "percentageGuilds", allEntries = true)
	@Transactional(rollbackFor = {GuildSameNameException.class,GuildSameCreatorException.class,GuildLogoException.class,GuildNotCreatorException.class})
	public void saveGuild(Guild guild, Client client) throws DataAccessException, GuildSameNameException, GuildSameCreatorException, GuildLogoException, GuildNotCreatorException {
		
		Collection<Guild> guilds = this.guildRepository.findAllGuilds();
		
		if(guild.getId() != null) 
		{
			guilds = guilds.stream().filter(c -> c.getId() != guild.getId()).collect(Collectors.toList());
		}
		
		if(guilds.stream().anyMatch(c->c.getName().toLowerCase().equals(guild.getName().toLowerCase()))){
			throw new GuildSameNameException();
		}
		else if(!guild.getCreator().equals(client.getUser().getUsername())) {
			throw new GuildNotCreatorException();
		}
		else if(guilds.stream().anyMatch(c->c.getCreator().equals(guild.getCreator()))) {
			throw new GuildSameCreatorException();
		}
		else if(!guild.getLogo().startsWith("https://")){
			throw new GuildLogoException();
		}
		else 
		{
			this.guildRepository.save(guild);	
			
			if(this.forumRepository.findForumByGuildId(guild.getId())==null)
			{
				Forum f = new Forum();
				f.setGuild(guild);
				f.setMessages(new ArrayList<>());
				
				this.forumRepository.save(f);				
			}
		}
	}
	
	@CacheEvict(cacheNames = {"percentageGuilds", "topPointGuild"}, allEntries = true)
	@Transactional
	public void deleteGuild(Guild guild, String clientUsername) throws DataAccessException {
		
		Collection<Client> clients = this.guildRepository.findClientsByGuild(guild);
		if(guild.getCreator().equals(clientUsername)){
		for(Client c: clients) {
			c.setGuild(null);
			this.clientRepository.save(c);
		}
		
		this.forumRepository.delete(this.forumRepository.findForumByGuildId(guild.getId()));
		
		this.guildRepository.delete(guild);
		}
	}
	
	@CacheEvict(cacheNames = {"percentageGuilds", "topPointGuild"}, allEntries = true)
	@Transactional
	public void joinGuild(String clientUsername, Guild guild) throws DataAccessException{
		
		Client client = this.clientRepository.findClientByUsername(clientUsername);
		client.setGuild(guild);
	}
	
	@CacheEvict(cacheNames = {"percentageGuilds", "topPointGuild"}, allEntries = true)
	@Transactional
	public void leaveGuild(Client client,Guild guild) throws DataAccessException{
		if(client.getGuild().equals(guild))
		client.setGuild(null);
	}

	@Transactional
	public Collection<String> findAllGuildNames() throws DataAccessException {
		Collection<String> res = this.guildRepository.findAllGuildNames();
		return res;		
	}
	
}
