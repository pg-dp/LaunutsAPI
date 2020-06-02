package launutsapi;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

@Service
public class LauService {

	protected static String json_resource_file_name = "LAU_Polygons.json";
	protected static String feature_name_type = "lau_label";
	protected static String feature_id_type = "gisco_id";
	private static JSONParser parser = new JSONParser();
	private static Reader reader;

	protected static Property skos_pref_label = ResourceFactory
			.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
	protected static Property skos_notation = ResourceFactory
			.createProperty("http://www.w3.org/2004/02/skos/core#notation");
	protected static Property ogc_as_wkt = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asWKT");
	protected static Property sf_polygon = ResourceFactory.createProperty("http://www.opengis.net/ont/sf#Polygon");
	protected static Property dct_location = ResourceFactory.createProperty("http://purl.org/dc/terms/Location");
	protected static String regex_for_prefix_of_laucode_from_germany = "(http:\\/\\/projekt-opal\\.de\\/launuts\\/lau\\/DE\\/)";

	public void setPrefixes(Model model) {

		model.setNsPrefix("laude", "http://projekt-opal.de/launuts/lau/DE/");
		model.setNsPrefix("launuts", "http://projekt-opal.de/launuts/");
		model.setNsPrefix("dct", "http://purl.org/dc/terms/");
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("sf", "http://www.opengis.net/ont/sf#");
		model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");

	}

	public Lau getLauJson(String query_string) throws IOException, ParseException {

		Lau a_lau = null;

		// JSON format response
		try {
			reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(json_resource_file_name)
					.getFile().toString());
			JSONArray lau_array = (JSONArray) parser.parse(reader);
			Iterator<JSONObject> lauIterator = lau_array.iterator();
			System.out.println(query_string);
			while (lauIterator.hasNext()) {

				// get next nuts object
				JSONObject lau = lauIterator.next();

				String lau_name = lau.get(feature_name_type).toString();
				String lau_id = lau.get(feature_id_type).toString();
				String geometry_type = lau.get("geometry_type").toString();
				ArrayList<String[]> coordinates = (ArrayList<String[]>) lau.get("coordinates");
				ArrayList<String[]> inner_rings = (ArrayList<String[]>) lau.get("inner_rings");
				
				// If nut_id is in query parameter
				if (query_string.toLowerCase().matches(lau_id.toLowerCase())) {
					a_lau = new Lau(lau_id, lau_name, geometry_type, coordinates, inner_rings, "Query was successful");
				}

				// If nut_name is in query parameter
				
				else if (query_string.equalsIgnoreCase(lau_name) || query_string.toLowerCase().matches("^("+query_string.toLowerCase()+")")) {
					a_lau = new Lau(lau_id, lau_name, geometry_type, coordinates, inner_rings, "Query was successful");
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return a_lau;
	}

	
	public JSONArray getAllLauJson() throws IOException, ParseException {

		reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(json_resource_file_name)
				.getFile().toString());
		JSONArray lau_array = (JSONArray) parser.parse(reader);
		return lau_array;
	}
	
	
	public void getLauTurtle(String query_string) {

		Model query_model = ModelFactory.createDefaultModel();
		Model response_model = ModelFactory.createDefaultModel();
		setPrefixes(response_model);
		Property property_to_check = null;

		// Turtle format response
		query_model.read(new LauService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());

		// Query_string is a laucode
		if (query_string.matches("\\d{8}"))
			property_to_check = skos_notation;

		// Query string is a name of the lau
		else
			property_to_check = skos_pref_label;

		StmtIterator iterator = query_model.listStatements(new SimpleSelector(null, property_to_check, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement my_st = iterator.nextStatement();
			Resource subject = my_st.getSubject();
			RDFNode object = my_st.getObject();

			/**
			 * If user provided nuts label e.g. "Paderborn" in query parameter OR If use
			 * provided nuts code e.g. DEA47 in query parameter
			 */
			if ((object.toString().equalsIgnoreCase(query_string) &&

			// This regex is to join a nutcode with its NS e.g.
			// http://data.europa.eu/nuts/code/DEA47
					(subject.toString().matches(regex_for_prefix_of_laucode_from_germany + "\\d{8}"))))

			{

				Resource lau = response_model.createResource(subject.toString());

				// Get all the properties with statements of the nuts
				StmtIterator st_iterator = subject.listProperties();
				while (st_iterator.hasNext()) {
					Statement st = st_iterator.nextStatement();
					if (!st.getObject().isAnon())
						lau.addProperty(st.getPredicate(), st.getObject());
					else {
						Resource blank_node = (Resource) st.getObject();
						lau.addProperty(dct_location, response_model.createResource().addProperty(RDF.type, sf_polygon)
								.addProperty(ogc_as_wkt, blank_node.getProperty(ogc_as_wkt).getObject()));
					}
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

	public void getAllLauTurtle() {

		Model query_model = ModelFactory.createDefaultModel();
		Model response_model = ModelFactory.createDefaultModel();
		setPrefixes(response_model);

		// Turtle format response
		query_model.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = query_model.listStatements(new SimpleSelector(null, skos_notation, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement my_st = iterator.nextStatement();
			Resource subject = my_st.getSubject();

			if (subject.toString().matches("http:\\/\\/projekt-opal\\.de\\/launuts\\/lau\\/DE\\/\\d{8}")) {
				Resource nuts = response_model.createResource(subject.toString());

				// Get all the properties with statements of the nuts
				StmtIterator st_iterator = subject.listProperties();
				while (st_iterator.hasNext()) {
					Statement st = st_iterator.nextStatement();
					if (!st.getObject().isAnon())
						nuts.addProperty(st.getPredicate(), st.getObject());
					else {
						Resource blank_node = (Resource) st.getObject();
						nuts.addProperty(dct_location, response_model.createResource().addProperty(RDF.type, sf_polygon)
								.addProperty(ogc_as_wkt, blank_node.getProperty(ogc_as_wkt).getObject()));
					}
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
