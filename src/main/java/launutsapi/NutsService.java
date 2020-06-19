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

	protected static final String JSON_RESOURCE_FILE_NAME = "NUTS_Polygons.json";
	protected static final String FEATURE_NAME_TYPE = "nuts_name";
	protected static final String FEATURE_ID_TYPE = "nuts_id";
	protected static final String REGEX_FOR_PREFIX_NUTCODE_FROM_GERMANY = "(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)(DE)(\\w+)";
	protected static final String REGEX_FOR_PREFIX_NUTCODE = "(http:\\/\\/data\\.europa\\.eu\\/nuts\\/code\\/)";
	
	private static JSONParser parser = new JSONParser();
	private static Reader reader;
	
	protected static Property skos_pref_label = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
	protected static Property ogc_as_wkt = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asWKT");
	protected static Property sf_polygon = ResourceFactory.createProperty("http://www.opengis.net/ont/sf#Polygon");
	protected static Property dct_location = ResourceFactory.createProperty("http://purl.org/dc/terms/Location");

	public void setPrefixes(Model model) {

		model.setNsPrefix("nutscode", "http://data.europa.eu/nuts/code/");
		model.setNsPrefix("launuts", "http://projekt-opal.de/launuts/");
	    model.setNsPrefix("dct", "http://purl.org/dc/terms/");
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("sf", "http://www.opengis.net/ont/sf#");
		model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
		
	}
	
	public Nuts getNutsJson(String queryString) throws IOException, ParseException {

		Nuts aNuts = new Nuts();

		// JSON format response
		try {
			reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(JSON_RESOURCE_FILE_NAME)
					.getFile().toString());
			JSONArray nutsArray = (JSONArray) parser.parse(reader);
			Iterator<JSONObject> nutsIterator = nutsArray.iterator();

			while (nutsIterator.hasNext()) {

				// get next nuts object
				JSONObject nuts = nutsIterator.next();

				String nutName = nuts.get(FEATURE_NAME_TYPE).toString();
				String nutId = nuts.get(FEATURE_ID_TYPE).toString();
				String nutsLevel = nuts.get("level").toString();
				String geometryType = nuts.get("geometry_type").toString();
				ArrayList<String[]> outerRing = (ArrayList<String[]>) nuts.get("coordinates");
				ArrayList<String[]> innerRings = (ArrayList<String[]>) nuts.get("inner_rings");

				// If nut_id is in query parameter
				if (queryString.toLowerCase().matches(nutId.toLowerCase())) {
					aNuts = new Nuts(nutId, nutName, geometryType, nutsLevel, outerRing, innerRings,
							"Query was successful");
				}

				// If nut_name is in query parameter
				else if (queryString.equalsIgnoreCase(nutName)) {
					System.out.println("nut_name came in query");
					aNuts = new Nuts(nutId, nutName, geometryType, nutsLevel, outerRing, innerRings,
							"Query was successful");
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return aNuts;
	}

	public JSONArray getAllNutsJson() throws IOException, ParseException {

		reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(JSON_RESOURCE_FILE_NAME)
				.getFile().toString());
		JSONArray nutsArray = (JSONArray) parser.parse(reader);
		return nutsArray;
	}
	
	


	public void getAllNutsTurtle() {
		
		Model queryModel = ModelFactory.createDefaultModel();
		Model responseModel = ModelFactory.createDefaultModel();

		// Turtle format response
		queryModel.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = queryModel.listStatements(new SimpleSelector(null, skos_pref_label, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement statementSkosPrefLabel = iterator.nextStatement();
			Resource subject = statementSkosPrefLabel.getSubject();

			if (subject.toString().matches(REGEX_FOR_PREFIX_NUTCODE_FROM_GERMANY))
			{
				setPrefixes(responseModel);
				Resource nuts = responseModel.createResource(subject.toString());

				// Get all the properties with statements of the nuts
				StmtIterator StatementsOfAllProperties = subject.listProperties();
				while (StatementsOfAllProperties.hasNext()) {
					Statement aStatementOfaProperty = StatementsOfAllProperties.nextStatement();
					if (!aStatementOfaProperty.getObject().isAnon())
						nuts.addProperty(aStatementOfaProperty.getPredicate(), aStatementOfaProperty.getObject());
					else {
						Resource blankNode = (Resource) aStatementOfaProperty.getObject();
						nuts.addProperty(dct_location, responseModel.createResource().addProperty(RDF.type, sf_polygon)
								.addProperty(ogc_as_wkt, blankNode.getProperty(ogc_as_wkt).getObject()));
					}
				}
			}
		}
		try {
			responseModel.write(new PrintStream("sample.ttl"), "TURTLE");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void getNutsTurtle(String queryString) {
		
		Model queryModel = ModelFactory.createDefaultModel();
		Model responseModel = ModelFactory.createDefaultModel();
		
		// Turtle format response
		queryModel.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = queryModel.listStatements(new SimpleSelector(null, skos_pref_label, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement statementSkosPrefLabel = iterator.nextStatement();
			Resource subject = statementSkosPrefLabel.getSubject();
			RDFNode object = statementSkosPrefLabel.getObject();

			/**
			 * If user provided nuts label e.g. "Paderborn" in query parameter OR If use
			 * provided nuts code e.g. DEA47 in query parameter
			 */
			if ((object.toString().equalsIgnoreCase(queryString) &&
			// This regex is to filter only for nuts
					subject.toString().matches(REGEX_FOR_PREFIX_NUTCODE_FROM_GERMANY))

					// This regex is to join a nutcode with its NS e.g.
					// http://data.europa.eu/nuts/code/DEA47
					|| (subject.toString().matches(REGEX_FOR_PREFIX_NUTCODE + queryString)))

			{
				setPrefixes(responseModel);
				Resource nuts = responseModel.createResource(subject.toString());

				// Get all the properties with statements of the nuts
				StmtIterator allStatementsIterator = subject.listProperties();
				while (allStatementsIterator.hasNext()) {
					Statement st = allStatementsIterator.nextStatement();
					if (!st.getObject().isAnon())
						nuts.addProperty(st.getPredicate(), st.getObject());
					else {
						Resource blankNode = (Resource) st.getObject();
						nuts.addProperty(dct_location, responseModel.createResource().addProperty(RDF.type, sf_polygon)
								.addProperty(ogc_as_wkt, blankNode.getProperty(ogc_as_wkt).getObject()));
					}
				}
			}
		}
		try {
			responseModel.write(new PrintStream("sample.ttl"), "TURTLE");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
