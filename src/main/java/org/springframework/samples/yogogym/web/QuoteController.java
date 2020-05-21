package org.springframework.samples.yogogym.web;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.yogogym.model.Quote;
import org.springframework.samples.yogogym.service.QuoteService;
import org.springframework.samples.yogogym.service.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class QuoteController {

	private final 	QuoteService quoteService;


	@Autowired
	public QuoteController(QuoteService quoteService) {
		this.quoteService = quoteService;
	}
	
	@GetMapping("/mainMenu/quotes")
	public String getQuote(RestTemplate restTemplate, Model model) throws ResourceNotFoundException {

		Quote quote = new Quote();
		
		try {

			HttpEntity<String> entity = setUpHeaders();
			ResponseEntity<Quote> response = restTemplate.exchange("https://api.kanye.rest/", HttpMethod.GET, entity, Quote.class);
			quote.setQuote(response.getBody().getQuote());			
			model.addAttribute("quote", quote);
			model.addAttribute("apiFunctional", true);

			
		} catch (Exception e) {
			
			quote.setQuote("Just because our API fails doesnt mean you can fail too");
			model.addAttribute("quote", quote);
			model.addAttribute("apiFunctional", false);

		}


		return "mainMenu/quotes/quote";
	 }

	 private HttpEntity<String> setUpHeaders()
	 {
		 HttpHeaders headers = new HttpHeaders();
		 headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		 headers.add("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		 return new HttpEntity<String>("parameters", headers);
	 }
	 
}