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
	private NutsService nut_service;
	
	@Autowired
	private LauService lau_service;
	
	private Nuts nut;
	private Lau lau;
	
	private String notification = "Please enter query in correct format."
		     + "Some correct formats are like these "
		     + "http://localhost:8080/launuts/nuts/DEA47/json, "
		     + "http://localhost:8080/launuts/nuts/DEA47/ttl, "
		     + "http://localhost:8080/launuts/nuts/paderborn/json, "
		     + "http://localhost:8080/launuts/nuts/paderborn/ttl, "
		     + "http://localhost:8080/launuts/nuts/ttl, "
		     + "http://localhost:8080/launuts/nuts/json ";
	
	@RequestMapping("/launuts/nuts/{query_string}/json")
	public List<Nuts> getNutsJson(@PathVariable String query_string) throws IOException, ParseException {
		
		//Validate input: If user trying to enter a nut id
		if(!query_string.matches(".*\\d.*")
				&& !(query_string.toLowerCase().matches("^((de)|(de\\D))((\\d{1})|(\\d{2})|(\\d{3})|(\\d{1}\\D))$"))) {
	
			nut = new Nuts(notification);	
			return Arrays.asList(nut);	
		}
		else 
			return Arrays.asList(nut_service.getNutsJson(query_string));	
			
	}
	
	@RequestMapping("/launuts/lau/{query_string}/json")
	public List<Lau> getLauJson(@PathVariable String query_string) throws IOException, ParseException {
		
		//Validate input: If user trying to enter a nut id
		if(query_string.matches(".*\\d.*") && !(query_string.toLowerCase().matches("^(de_)\\d{8}"))) {
	
			lau = new Lau(notification);	
			return Arrays.asList(lau);	
		}
		else 
			return Arrays.asList(lau_service.getLauJson(query_string));	
			
	}
	
	
	@RequestMapping("/launuts/nuts/{query_string}/ttl")
	public String getNutTurtle(@PathVariable String query_string, HttpServletResponse response) throws IOException, ParseException {
		
		//Validate input: If user trying to enter a nut id
		if(!query_string.matches(".*\\d.*")
				&& !(query_string.toLowerCase().matches("^((de)|(de\\D))((\\d{1})|(\\d{2})|(\\d{3})|(\\d{1}\\D))$"))) {
			return notification;
		}
		else { 	
		 nut_service.getNutsTurtle(query_string.toUpperCase());
		 response.setContentType("text/plain;charset=UTF-8");
		 response.setHeader("Content-Disposition", "attachment; filename=\"sample.ttl\"");
	       InputStream inputStream = new FileInputStream(new File("sample.ttl"));
           int nRead;
           while ((nRead = inputStream.read()) != -1) {
               response.getWriter().write(nRead);
           }
		}
		 return null;
	}
	
	
	@RequestMapping("/launuts/lau/{query_string}/ttl")
	public String getLauTurtle(@PathVariable String query_string, HttpServletResponse response) throws IOException, ParseException {
		
		//Validate input: If user trying to enter a nut id
		if(query_string.matches(".*\\d.*") && !(query_string.toLowerCase().matches("^(de_)\\d{8}"))) {
			return notification;
		}
		else { 	
		 String laucode = query_string.toLowerCase().replace("de_", "");
		 lau_service.getLauTurtle(laucode.toUpperCase());
		 response.setContentType("text/plain;charset=UTF-8");
		 response.setHeader("Content-Disposition", "attachment; filename=\"sample.ttl\"");
	       InputStream inputStream = new FileInputStream(new File("sample.ttl"));
           int nRead;
           while ((nRead = inputStream.read()) != -1) {
               response.getWriter().write(nRead);
           }
		}
		 return null;
	}
	
	
	@RequestMapping("/launuts/nuts/json")
	public List<JSONArray> getAllNutsJson() throws IOException, ParseException {
		return Arrays.asList(nut_service.getAllNutsJson());	
	}
	
	@RequestMapping("/launuts/lau/json")
	public List<JSONArray> getAllLauJson() throws IOException, ParseException {
		return Arrays.asList(lau_service.getAllLauJson());	
	}
	
	
	@RequestMapping("/launuts/nuts/ttl")
	public void getAllNutsTurtle(HttpServletResponse response) throws IOException, ParseException {
		nut_service.getAllNutsTurtle();	
		response.setContentType("text/plain;charset=UTF-8");
		 response.setHeader("Content-Disposition", "attachment; filename=\"sample.ttl\"");
	       InputStream inputStream = new FileInputStream(new File("sample.ttl"));
          int nRead;
          while ((nRead = inputStream.read()) != -1) {
              response.getWriter().write(nRead);
          }
	}
	
	@RequestMapping("/launuts/lau/ttl")
	public void getAllLauTurtle(HttpServletResponse response) throws IOException, ParseException {
		lau_service.getAllLauTurtle();	
		response.setContentType("text/plain;charset=UTF-8");
		 response.setHeader("Content-Disposition", "attachment; filename=\"sample.ttl\"");
	       InputStream inputStream = new FileInputStream(new File("sample.ttl"));
          int nRead;
          while ((nRead = inputStream.read()) != -1) {
              response.getWriter().write(nRead);
          }
	}
	
	
	@RequestMapping("/**")
	public List<Nuts> allOtherPaths() {
		
		nut = new Nuts(notification);	
		return Arrays.asList(nut);			
	}

}
