package launutsapi;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class NutService {

	public static void main(String[] args) throws IOException, ParseException {

		/**
		 * How to test this service:
		 * In the query_string pass name of a city or state in string format or 
		 * as an ID e.g "Paderborn" or "DEA47". In the query_format, pass "json"
		 * or "ttl" or "Turtle". Case is not a problem here.
		 */
		
		String query_string = "DEA47";
		String query_format = "ttl";

		//JSON format response
		if (query_format.toLowerCase().equals("json")) {
			JSONParser parser = new JSONParser();
			Reader reader;
			Iterator<JSONObject> nutsIterator;

			try {
				reader = new FileReader(new NutService().getClass().getClassLoader().getResource("NUT_Polygons.json")
						.getFile().toString());
				JSONArray nuts_array = (JSONArray) parser.parse(reader);
				nutsIterator = nuts_array.iterator();

				while (nutsIterator.hasNext()) {

					// get next nut object
					JSONObject nut = nutsIterator.next();

					String nut_name = nut.get("NUTS_NAME").toString();
					String nut_id = nut.get("NUTS_ID").toString();
					String nut_level = nut.get("Level").toString();
					ArrayList<String[]> outer_ring = (ArrayList<String[]>) nut.get("Outer_ring");
					ArrayList<String[]> inner_ring = (ArrayList<String[]>) nut.get("Inner_ring");

					// If nut_id is in query parameter
					if (query_string.toLowerCase().matches(nut_id.toLowerCase())) {
						System.out.println("nuts_id came in query");
						DataContainer container = new DataContainer(nut_id, nut_name, nut_level, outer_ring,
								inner_ring);
						ObjectMapper mapper = new ObjectMapper();
						try {
							String json = mapper.writeValueAsString(container);
							System.out.println("ResultingJSONstring = " + json);
							// System.out.println(json);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}

					// If nut_name is in query parameter
					else if (query_string.toLowerCase().equals(nut_name.toLowerCase())) {
						System.out.println("nut_name came in query");
						DataContainer container = new DataContainer(nut_id, nut_name, nut_level, outer_ring,
								inner_ring);

						ObjectMapper mapper = new ObjectMapper();
						try {
							String json = mapper.writeValueAsString(container);
							System.out.println("ResultingJSONstring = " + json);
							// System.out.println(json);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		//Turtle format response
		else if(query_format.toLowerCase().equals("ttl") || query_format.toLowerCase().equals("turtle")) 
		{
			Model query_model = ModelFactory.createDefaultModel();
			Model response_model = ModelFactory.createDefaultModel();
			
			//Set namespace prefixes for response model
			response_model.setNsPrefix("nutscode", "http://data.europa.eu/nuts/code/");
			response_model.setNsPrefix("launuts", "http://projekt-opal.de/launuts/");
			response_model.setNsPrefix("dct", "http://purl.org/dc/terms/");
			response_model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
			response_model.setNsPrefix("sf", "http://www.opengis.net/ont/sf#");
			response_model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
			
			//set some properties to look for
			Property skos_pref_label = query_model.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
			
			query_model.read(new NutService().getClass().getClassLoader().getResource("launuts.ttl")
					.getFile().toString());
			StmtIterator iterator = query_model.listStatements(new SimpleSelector(null, skos_pref_label,(RDFNode) null));
			while(iterator.hasNext()) {
				Statement my_st = iterator.nextStatement();
				Resource subject = my_st.getSubject();
				RDFNode predicate = my_st.getPredicate();
				RDFNode object = my_st.getObject();
				
				/**
				 * If user provided nut label e.g. "Paderborn" in query parameter
				 * OR
				 * If use provided nut code e.g. DEA47 in query parameter
				 */
				if((object.toString().toLowerCase().equals(query_string) && subject.toString()
						.matches("(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)(DE)(\\w+)")) 
						//This regex is to filter only for nuts
						
				|| (subject.toString().matches("(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)"+query_string)))
					//This regex is to join a nutcode with its NS e.g. http://data.europa.eu/nuts/code/DEA47
				{
					
					Resource nut = response_model.createResource(subject.toString());
					
					//Get all the properties with statements of the nut
					StmtIterator st_iterator = subject.listProperties();
					while(st_iterator.hasNext()) {
						Statement st = st_iterator.nextStatement();
						nut.addProperty(st.getPredicate(),st.getObject() );
					}
				}
			}
			response_model.write(System.out,"TURTLE");
		}
	}

}
