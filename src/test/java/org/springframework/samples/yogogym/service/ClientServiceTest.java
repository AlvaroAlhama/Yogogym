package org.springframework.samples.yogogym.service;



import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.stereotype.Service;

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ClientServiceTest {

	@Autowired
	protected ClientService clientService;
	
	@Test
	void shouldSaveClient() {
		
		Collection<Client> clients = (Collection<Client>) this.clientService.findAllClient();
		int found = clients.size();
		Client c = new Client();
		
		c.setAge(18);
		c.setEmail("aa@test.com");
		c.setFirstName("Pedro");
		c.setLastName("Perez");
		c.setHeight(179.);
		c.setWeight(100.);
		c.setFatPercentage(0.3);
		c.setNif("26547898D");
		c.setIsPublic(true);
		
		this.clientService.saveClient(c);
		
		c = this.clientService.findClientById(found + 1);
		assertThat(c.getNif()).isEqualTo("26547898D");
	}
	
	@Test
	void shouldFindClientById(){
		Client client = this.clientService.findClientById(1);
		assertThat(client.getNif()).isEqualTo("12345678B");	
		
		client = this.clientService.findClientById(50);
		assertThat(client).isNull();	
	}
	
	@Test
	void shouldFindClientByUsername(){
		Client client = this.clientService.findClientByUsername("client1");
		assertThat(client.getNif()).isEqualTo("12345678B");	
		
		client = this.clientService.findClientByUsername("client50");
		assertThat(client).isNull();
	}

	@Test
	void shouldFindClientByInscriptionId(){
		Client client = this.clientService.findClientByInscriptionId(2);
		assertThat(client.getNif()).isEqualTo("12345678B");	
		
		client = this.clientService.findClientByInscriptionId(50);
		assertThat(client).isNull();	
	}
	
	@Test
	void shouldFindClientsWithOnlySubmittedInscriptions(){
		Collection<Client> clients = this.clientService.findClientsWithOnlySubmittedInscriptions();
		assertThat(clients.size()).isEqualTo(1);	
	}
	
	@Test
	void shouldFindAllClients(){
		Collection<Client> clients = (Collection<Client>) this.clientService.findAllClient();
		assertThat(clients.size()).isEqualTo(11);
	}
	
	//Classification
	@Test
	void shouldClassificationNameDate() {
		List<String> names = this.clientService.classificationNameDate();
		assertThat(names.size()).isEqualTo(0);
	}
	
	@Test
	void shouldClassificationPointDate() {
		List<Integer> points = this.clientService.classificationPointDate();
		assertThat(points.size()).isEqualTo(0);
	}
	
	@Test
	void shouldClassificationNameAll() {
		List<String> names = this.clientService.classificationNameAll();
		assertThat(names.size()).isEqualTo(1);
	}
	
	@Test
	void shouldClassificationPointAll() {
		List<Integer> points = this.clientService.classificationPointAll();
		assertThat(points.size()).isEqualTo(1);
	}

}
