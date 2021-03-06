package launutsapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class LaunutsController {

	@Autowired
	private NutsService nutService;

	@Autowired
	private LauService lauService;

	private Nuts nuts;
	private Lau lau;

	private static final String NOTIFICATION = "Please enter query in correct format." + "Some correct formats are like these "
			+ "http://localhost:8080/launuts/nuts/DEA47/json, " + "http://localhost:8080/launuts/nuts/DEA47/ttl, "
			+ "http://localhost:8080/launuts/nuts/paderborn/json, "
			+ "http://localhost:8080/launuts/nuts/paderborn/ttl, " + "http://localhost:8080/launuts/nuts/ttl, "
			+ "http://localhost:8080/launuts/nuts/json ";

	@RequestMapping("/launuts/nuts/{queryString}/json")
	public List<Nuts> getNutsJson(@PathVariable String queryString) throws IOException, ParseException {

		// Validate input: If user trying to enter a nut id
		if (queryString.matches(".*\\d.*")
				&& !(queryString.toLowerCase().matches("^((de)|(de\\D))((\\d{1})|(\\d{2})|(\\d{3})|(\\d{1}\\D))$"))) {

			nuts = new Nuts(NOTIFICATION);
			return Arrays.asList(nuts);
		} else
			return Arrays.asList(nutService.getNutsJson(queryString));

	}

	@RequestMapping("/launuts/lau/{queryString}/json")
	public List<Lau> getLauJson(@PathVariable String queryString) throws IOException, ParseException {

		// Validate input: If user trying to enter a nut id
		if (queryString.matches(".*\\d.*") && !(queryString.toLowerCase().matches("^(de_)\\d{8}"))) {

			lau = new Lau(NOTIFICATION);
			return Arrays.asList(lau);
		} else
			return Arrays.asList(lauService.getLauJson(queryString));

	}

	@RequestMapping("/launuts/nuts/{queryString}/ttl")
	public String getNutsTurtle(@PathVariable String queryString, HttpServletResponse response)
			throws IOException, ParseException {

		// Validate input: If user trying to enter a nut id
		if (queryString.matches(".*\\d.*")
				&& !(queryString.toLowerCase().matches("^((de)|(de\\D))((\\d{1})|(\\d{2})|(\\d{3})|(\\d{1}\\D))$"))) {
			return NOTIFICATION;
		} else {
			return  nutService.getNutsTurtle(queryString.toUpperCase());		
		}
		
	}

	@RequestMapping("/launuts/lau/{queryString}/ttl")
	public String getLauTurtle(@PathVariable String queryString, HttpServletResponse response)
			throws IOException, ParseException {

		// Validate input: If user trying to enter a nut id
		if (queryString.matches(".*\\d.*") && !(queryString.toLowerCase().matches("^(de_)\\d{8}"))) {
			return NOTIFICATION;
		} else {
			String laucode = queryString.toLowerCase().replace("de_", "");
			return lauService.getLauTurtle(laucode.toUpperCase());
		}
		
	}

	@RequestMapping("/launuts/nuts/json")
	public List<JSONArray> getAllNutsJson() throws IOException, ParseException {
		return Arrays.asList(nutService.getAllNutsJson());
	}

	@RequestMapping("/launuts/lau/json")
	public List<JSONArray> getAllLauJson() throws IOException, ParseException {
		return Arrays.asList(lauService.getAllLauJson());
	}

	@RequestMapping("/launuts/nuts/ttl")
	public String getAllNutsTurtle(HttpServletResponse response) throws IOException, ParseException {
		return nutService.getAllNutsTurtle();	
	}

	@RequestMapping("/launuts/lau/ttl")
	public String getAllLauTurtle(HttpServletResponse response) throws IOException, ParseException {
		return lauService.getAllLauTurtle();
	}

	@RequestMapping("/**")
	public List<Nuts> allOtherPaths() {

		nuts = new Nuts(NOTIFICATION);
		return Arrays.asList(nuts);
	}

}
