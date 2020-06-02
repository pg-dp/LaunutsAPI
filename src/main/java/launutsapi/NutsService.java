package launutsapi;

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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

@Service
public class NutsService {

	protected static String json_resource_file_name = "NUTS_Polygons.json";
	protected static String feature_name_type = "nuts_name";
	protected static String feature_id_type = "nuts_id";
	private static JSONParser parser = new JSONParser();
	private static Reader reader;
	
	protected static Property skos_pref_label = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
	protected static Property ogc_as_wkt = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asWKT");
	protected static Property sf_polygon = ResourceFactory.createProperty("http://www.opengis.net/ont/sf#Polygon");
	protected static Property dct_location = ResourceFactory.createProperty("http://purl.org/dc/terms/Location");
	protected static String regex_for_prefix_nutcode_from_germany = "(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)(DE)(\\w+)";
	protected static String regex_for_prefix_nutcode = "(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)";


	public void setPrefixes(Model model) {

		model.setNsPrefix("nutscode", "http://data.europa.eu/nuts/code/");
		model.setNsPrefix("launuts", "http://projekt-opal.de/launuts/");
	    model.setNsPrefix("dct", "http://purl.org/dc/terms/");
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("sf", "http://www.opengis.net/ont/sf#");
		model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
		
	}
	
	public Nuts getNutsJson(String query_string) throws IOException, ParseException {

		Nuts a_nut = null;

		// JSON format response
		try {
			reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(json_resource_file_name)
					.getFile().toString());
			JSONArray nuts_array = (JSONArray) parser.parse(reader);
			Iterator<JSONObject> nutsIterator = nuts_array.iterator();

			while (nutsIterator.hasNext()) {

				// get next nuts object
				JSONObject nuts = nutsIterator.next();

				String nut_name = nuts.get(feature_name_type).toString();
				String nut_id = nuts.get(feature_id_type).toString();
				String nut_level = nuts.get("level").toString();
				String geometry_type = nuts.get("geometry_type").toString();
				ArrayList<String[]> outer_ring = (ArrayList<String[]>) nuts.get("coordinates");
				ArrayList<String[]> inner_rings = (ArrayList<String[]>) nuts.get("inner_rings");

				// If nut_id is in query parameter
				if (query_string.toLowerCase().matches(nut_id.toLowerCase())) {
					a_nut = new Nuts(nut_id, nut_name, geometry_type, nut_level, outer_ring, inner_rings,
							"Query was successful");
				}

				// If nut_name is in query parameter
				else if (query_string.equalsIgnoreCase(nut_name)) {
					System.out.println("nut_name came in query");
					a_nut = new Nuts(nut_id, nut_name, geometry_type, nut_level, outer_ring, inner_rings,
							"Query was successful");
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return a_nut;
	}

	public JSONArray getAllNutsJson() throws IOException, ParseException {

		reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(json_resource_file_name)
				.getFile().toString());
		JSONArray nuts_array = (JSONArray) parser.parse(reader);
		return nuts_array;
	}
	
	


	public void getAllNutsTurtle() {
		
		Model query_model = ModelFactory.createDefaultModel();
		Model response_model = ModelFactory.createDefaultModel();
		setPrefixes(response_model);

		// Turtle format response
		query_model.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = query_model.listStatements(new SimpleSelector(null, skos_pref_label, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement my_st = iterator.nextStatement();
			Resource subject = my_st.getSubject();

			if (subject.toString().matches(regex_for_prefix_nutcode_from_germany))
			{
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

	public void getNutsTurtle(String query_string) {
		
		Model query_model = ModelFactory.createDefaultModel();
		Model response_model = ModelFactory.createDefaultModel();
		setPrefixes(response_model);
		
		// Turtle format response
		query_model.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = query_model.listStatements(new SimpleSelector(null, skos_pref_label, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement my_st = iterator.nextStatement();
			Resource subject = my_st.getSubject();
			RDFNode object = my_st.getObject();

			/**
			 * If user provided nuts label e.g. "Paderborn" in query parameter OR If use
			 * provided nuts code e.g. DEA47 in query parameter
			 */
			if ((object.toString().equalsIgnoreCase(query_string) &&
			// This regex is to filter only for nuts
					subject.toString().matches(regex_for_prefix_nutcode_from_germany))

					// This regex is to join a nutcode with its NS e.g.
					// http://data.europa.eu/nuts/code/DEA47
					|| (subject.toString().matches(regex_for_prefix_nutcode + query_string)))

			{

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
