package org.springframework.samples.yogogym.web;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;

import org.openqa.selenium.remote.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.yogogym.model.Client;
import org.springframework.samples.yogogym.model.Item;
import org.springframework.samples.yogogym.model.Playlist;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.RoutineLine;
import org.springframework.samples.yogogym.model.TokenMapper;
import org.springframework.samples.yogogym.model.Tracks;
import org.springframework.samples.yogogym.model.Training;
import org.springframework.samples.yogogym.model.Enums.Intensity;
import org.springframework.samples.yogogym.service.TrainingService;
import org.springframework.samples.yogogym.service.exceptions.ChallengeMore3Exception;
import org.springframework.samples.yogogym.service.exceptions.ResourceNotFoundException;
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2LoginSpec;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

@Controller
public class PlaylistController {

	private final TrainingService trainingService;
	private final String SPOTIFY_BASE64CODE = "OTU2YjhhZTNlNGIyNDZiNmE4MmM0YTJjNWNlNmU0YWM6ZDkyMGEyOGFjOTU4NDU5ZWIzNTRmODIyMjZkYmFmMTY=";
	private static final String URL_SPOTIFY_TOKEN = "https://accounts.spotify.com/api/token";

	@Autowired
	public PlaylistController(TrainingService trainingService) {
		this.trainingService = trainingService;
	}
	
	@Autowired
	private HttpSession httpSession;
	

	@GetMapping("/redirect")
	public String getRedirect(Model model) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails)principal).getUsername();
		
		Integer id = (Integer)httpSession.getAttribute("train");
		return "redirect:client/"+username+"/trainings/"+id+"/playlist";
	}

	@GetMapping("/callback/")
	public String goToPlaylist(@PathParam("code") String code,Model model, RestTemplate restTemplate) throws IOException {
		
		return "redirect:/redirect?code=" + code;
	}

	@GetMapping("/client/{clientUsername}/trainings/{trainingId}/playlist")
	public String getPlaylistOfTraining(RestTemplate restTemplate, Model model,
			@PathVariable("trainingId") int trainingId) throws ResourceNotFoundException {

		String idPlaylist = getPlaylistIdFromTrainingIntensity(trainingId);
		String clientID = "956b8ae3e4b246b6a82c4a2c5ce6e4ac";
		Playlist playlist = new Playlist();

		try {
		
			HttpEntity<String> entity = setUpHeaders();
		
			
			ResponseEntity<Playlist> response = restTemplate.exchange(
					"https://api.spotify.com/v1/playlists/" + idPlaylist, HttpMethod.GET, entity, Playlist.class);

			playlist.setName(response.getBody().getName());
			String[] href = response.getBody().getUri().split(":");
			String uri = "https://open.spotify.com/playlist/"+href[2];
			
			
			playlist.setUri(uri);
			Tracks tracks = response.getBody().getTracks();
			for(Item i : tracks.getItems()) {
				String[] tra = i.getTrack().getUri().split(":");
				i.getTrack().setUri("https://open.spotify.com/track/"+tra[2]);
			}
			playlist.setTracks(tracks);
			model.addAttribute("playlist", playlist);
			model.addAttribute("apiFunctional", true);

		} catch (Exception e) {

			if (idPlaylist == "") {
				playlist.setName("This training doesn't have exercises yet");
			} else {

				playlist.setName("Just because our API fails doesnt mean you can fail too");
				model.addAttribute("playlist", playlist);
				model.addAttribute("apiFunctional", false);

			}
		}
		return "client/trainings/playlists/playlist";
	}

	private HttpEntity<String> setUpHeaders() {

		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		headers.add("Authorization", "Bearer " + getToken().getAccessToken());
		return new HttpEntity<String>("parameters", headers);
	}

	public String getPlaylistIdFromTrainingIntensity(Integer trainingId) {
		List<String> res = new ArrayList<>();
		List<String> playlistsVeryIntense = new ArrayList<>();
		String veryIntenseA[] = new String[] { "37i9dQZF1DWUgX5cUT0GbU", "37i9dQZF1DX76Wlfdnj7AP" };
		playlistsVeryIntense.addAll(Arrays.asList((veryIntenseA)));
		List<String> playlistsIntense = new ArrayList<>();
		String intenseA[] = new String[] { "37i9dQZF1DX2apWzyECwyZ", "37i9dQZF1DWYp5sAHdz27Y" };
		playlistsIntense.addAll(Arrays.asList((intenseA)));
		List<String> playlistsModerated = new ArrayList<>();
		String moderatedA[] = new String[] { "37i9dQZF1DX7QOv5kjbU68", "37i9dQZF1DX70RN3TfWWJh" };
		playlistsModerated.addAll(Arrays.asList(moderatedA));
		List<String> playlistsLow = new ArrayList<>();
		String lowA[] = new String[] { "37i9dQZF1DX7hgjNHEiO3v", "37i9dQZF1DX7R7Bjxm48PR", "37i9dQZF1DXcTpoGQmyr2B" };
		playlistsLow.addAll(Arrays.asList((lowA)));
		Training t = this.trainingService.findTrainingById(trainingId);
		Collection<Routine> routines = t.getRoutines();
		Integer low = 0;
		Integer moderated = 0;
		Integer intense = 0;
		Integer veryIntense = 0;
		for (Routine r : routines) {
			Collection<RoutineLine> routineLine = r.getRoutineLine();
			for (RoutineLine rl : routineLine) {
				if (rl.getExercise().getIntensity().equals(Intensity.LOW))
					low++;
				if (rl.getExercise().getIntensity().equals(Intensity.MODERATED))
					moderated++;
				if (rl.getExercise().getIntensity().equals(Intensity.INTENSE))
					intense++;
				if (rl.getExercise().getIntensity().equals(Intensity.VERY_INTENSE))
					veryIntense++;
			}
		}
		if (low == 0 && moderated == 0 && intense == 0 && veryIntense == 0) {
			return "";
		}

		if ((low > moderated) && (low > moderated) && (low > moderated))
			res = playlistsLow;
		if ((moderated >= low) && (moderated >= intense) && (moderated >= veryIntense))
			res = playlistsModerated;
		if ((intense > low) && (intense > moderated) && (intense > veryIntense))
			res = playlistsIntense;
		if ((veryIntense > low) && (veryIntense > intense) && (veryIntense > moderated))
			res = playlistsVeryIntense;

		Random ram = new Random();
		return res.get(ram.nextInt(res.size() - 1));

	}

	private TokenMapper getToken() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpHeaders.add("Authorization", "Basic " + SPOTIFY_BASE64CODE);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "client_credentials");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<TokenMapper> response = restTemplate.exchange(URL_SPOTIFY_TOKEN, HttpMethod.POST, request,
				TokenMapper.class);

		return response.getBody();
	}

}