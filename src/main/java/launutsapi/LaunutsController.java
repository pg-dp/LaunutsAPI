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
	
	private Nuts nut;
	
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
		if(query_string.matches(".*\\d.*") && !(query_string.toLowerCase().matches("^((de)|(de\\D))((\\d{1})|(\\d{2})|(\\d{3}))$"))) {
	
			nut = new Nuts(notification);	
			return Arrays.asList(nut);	
		}
		else 
			return Arrays.asList(nut_service.getNutsJson(query_string));	
			
	}
	
	@RequestMapping("/launuts/nuts/{query_string}/ttl")
	public String getNutTurtle(@PathVariable String query_string, HttpServletResponse response) throws IOException, ParseException {
		
		//Validate input: If user trying to enter a nut id
		if(query_string.matches(".*\\d.*") && !(query_string.toLowerCase().matches("^((de)|(de\\D))((\\d{1})|(\\d{2})|(\\d{3}))$"))) {
			return notification;
		}
		else 	
		 nut_service.getNutsTurtle(query_string.toUpperCase());
		 response.setContentType("text/plain;charset=UTF-8");
		 response.setHeader("Content-Disposition", "attachment; filename=\"sample.ttl\"");
	       InputStream inputStream = new FileInputStream(new File("sample.ttl"));
           int nRead;
           while ((nRead = inputStream.read()) != -1) {
               response.getWriter().write(nRead);
           }
		 return null;
	}
	
	@RequestMapping("/launuts/nuts/json")
	public List<JSONArray> getAllNutsJson() throws IOException, ParseException {
		return Arrays.asList(nut_service.getAllNutsJson());	
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
	
	
	@RequestMapping("/**")
	public List<Nuts> allOtherPaths() {
		
		nut = new Nuts(notification);	
		return Arrays.asList(nut);			
	}

}
