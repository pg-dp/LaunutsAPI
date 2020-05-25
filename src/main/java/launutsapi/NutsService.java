package launutsapi;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

@Service
public class NutsService {

	protected static String json_resource_file_name = "NUT_Polygons.json";
	protected static String feature_name_type = "nut_name";
	protected static String feature_id_type = "nut_id";

	/**
	 * How to test this service: In the query_string pass name of a city or state in
	 * string format or as an ID e.g "Paderborn" or "DEA47". In the query_format,
	 * pass "json" or "ttl" or "Turtle". Case is not a problem here.
	 */

	public Nuts getNutJson(String query_string) throws IOException, ParseException {

		Nuts a_nut = null;

		// JSON format response

		JSONParser parser = new JSONParser();
		Reader reader;
		Iterator<JSONObject> nutsIterator;

		try {
			reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(json_resource_file_name)
					.getFile().toString());
			JSONArray nuts_array = (JSONArray) parser.parse(reader);
			nutsIterator = nuts_array.iterator();

			while (nutsIterator.hasNext()) {

				// get next nut object
				JSONObject nut = nutsIterator.next();

				String nut_name = nut.get(feature_name_type).toString();
				String nut_id = nut.get(feature_id_type).toString();
				String nut_level = nut.get("level").toString();
				String geometry_type = nut.get("geometry_type").toString();
				ArrayList<String[]> outer_ring = (ArrayList<String[]>) nut.get("outer_ring");
				ArrayList<String[]> inner_rings = (ArrayList<String[]>) nut.get("inner_rings");

				// If nut_id is in query parameter
				if (query_string.toLowerCase().matches(nut_id.toLowerCase())) {
					System.out.println("nuts_id came in query");
					a_nut = new Nuts(nut_id, nut_name, geometry_type,nut_level, outer_ring, inner_rings,"Query was successful");
				}

				// If nut_name is in query parameter
				else if (query_string.toLowerCase().equals(nut_name.toLowerCase())) {
					System.out.println("nut_name came in query");
					a_nut = new Nuts(nut_id, nut_name, geometry_type, nut_level, outer_ring, inner_rings, "Query was successful");
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return a_nut;
	}

	public void getNutTurtle(String query_string) {
		
		//String query_string = "DEA47";
		System.out.println(query_string);

		// Turtle format response
		Model query_model = ModelFactory.createDefaultModel();
		Model response_model = ModelFactory.createDefaultModel();

//		ByteArrayOutputStream alternate_string_output_handle = new ByteArrayOutputStream();
//		PrintStream alternate_print_stream = new PrintStream(alternate_string_output_handle);
//		System.setOut(alternate_print_stream);

		// Set namespace prefixes for response model
		response_model.setNsPrefix("nutscode", "http://data.europa.eu/nuts/code/");
		response_model.setNsPrefix("launuts", "http://projekt-opal.de/launuts/");
		response_model.setNsPrefix("dct", "http://purl.org/dc/terms/");
		response_model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		response_model.setNsPrefix("sf", "http://www.opengis.net/ont/sf#");
		response_model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");

		// set some properties to look for
		Property skos_pref_label = query_model.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");

		query_model.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = query_model.listStatements(new SimpleSelector(null, skos_pref_label, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement my_st = iterator.nextStatement();
			Resource subject = my_st.getSubject();
			RDFNode predicate = my_st.getPredicate();
			RDFNode object = my_st.getObject();

			/**
			 * If user provided nut label e.g. "Paderborn" in query parameter OR If use
			 * provided nut code e.g. DEA47 in query parameter
			 */
			if ((object.toString().toLowerCase().equals(query_string.toLowerCase())
					&& subject.toString().matches("(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)(DE)(\\w+)"))
					// This regex is to filter only for nuts

					|| (subject.toString().matches("(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)" + query_string)))
			// This regex is to join a nutcode with its NS e.g.
			// http://data.europa.eu/nuts/code/DEA47
			{

				Resource nut = response_model.createResource(subject.toString());

				// Get all the properties with statements of the nut
				StmtIterator st_iterator = subject.listProperties();
				while (st_iterator.hasNext()) {
					Statement st = st_iterator.nextStatement();
					nut.addProperty(st.getPredicate(), st.getObject());
				}
			}
		}
		try {
			response_model.write(new PrintStream("sample.ttl"), "TURTLE");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
