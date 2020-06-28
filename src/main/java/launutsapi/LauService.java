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

	protected static final String JSON_RESOURCE_FILE_NAME = "LAU_Polygons.json";
	protected static final String FEATURE_NAME_TYPE = "lau_label";
	protected static final String FEATURE_ID_TYPE = "gisco_id";
	protected static final String REGEX_FOR_PREFIX_OF_LAUCODE_FROM_GERMANY = "(http:\\/\\/projekt-opal\\.de\\/launuts\\/lau\\/DE\\/)";
	
	private static JSONParser parser = new JSONParser();
	private static Reader reader;

	protected static Property skos_pref_label = ResourceFactory
			.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
	protected static Property skos_notation = ResourceFactory
			.createProperty("http://www.w3.org/2004/02/skos/core#notation");
	protected static Property ogc_as_wkt = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asWKT");
	protected static Property sf_polygon = ResourceFactory.createProperty("http://www.opengis.net/ont/sf#Polygon");
	protected static Property dct_location = ResourceFactory.createProperty("http://purl.org/dc/terms/Location");
	protected static Property dcat_centroid = ResourceFactory.createProperty("https://www.w3.org/ns/dcat#centroid");
	protected static Property sf_point = ResourceFactory.createProperty("http://www.opengis.net/ont/sf#Point");
	

	public void setPrefixes(Model model) {

		model.setNsPrefix("nutscode", "http://data.europa.eu/nuts/code/");
		model.setNsPrefix("laude", "http://projekt-opal.de/launuts/lau/DE/");
		model.setNsPrefix("launuts", "http://projekt-opal.de/launuts/");
		model.setNsPrefix("dct", "http://purl.org/dc/terms/");
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("sf", "http://www.opengis.net/ont/sf#");
		model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
		model.setNsPrefix("dcat", "https://www.w3.org/ns/dcat#");

	}

	public Lau getLauJson(String queryString) throws IOException, ParseException {

		Lau a_lau = new Lau();

		// JSON format response
		try {
			reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(JSON_RESOURCE_FILE_NAME)
					.getFile().toString());
			JSONArray lauArray = (JSONArray) parser.parse(reader);
			Iterator<JSONObject> lauIterator = lauArray.iterator();
			System.out.println(queryString);
			while (lauIterator.hasNext()) {

				// get next nuts object
				JSONObject lau = lauIterator.next();

				String lauName = lau.get(FEATURE_NAME_TYPE).toString();
				String lauId = lau.get(FEATURE_ID_TYPE).toString();
				String geometryType = lau.get("geometry_type").toString();
				ArrayList<String[]> coordinates = (ArrayList<String[]>) lau.get("coordinates");
				ArrayList<String[]> innerRings = (ArrayList<String[]>) lau.get("inner_rings");
				
				// If lau_id is in query parameter
				if (queryString.equalsIgnoreCase(lauId.toLowerCase())) {
					a_lau = new Lau(lauId, lauName, geometryType, coordinates, innerRings, "Query was successful");
				}

				// If lau_name is in query parameter
				
				else if (queryString.replaceAll("\\s+","").equalsIgnoreCase(lauName.replaceAll("\\s+",""))) {
					a_lau = new Lau(lauId, lauName, geometryType, coordinates, innerRings, "Query was successful");
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return a_lau;
	}

	
	public JSONArray getAllLauJson() throws IOException, ParseException {

		reader = new FileReader(new NutsService().getClass().getClassLoader().getResource(JSON_RESOURCE_FILE_NAME)
				.getFile().toString());
		JSONArray lauArray = (JSONArray) parser.parse(reader);
		return lauArray;
	}
	
	
	public void getLauTurtle(String queryString) {

		Model queryModel = ModelFactory.createDefaultModel();
		Model responseModel = ModelFactory.createDefaultModel();
		
		Property propertyToCheck = null;

		// Turtle format response
		queryModel.read(new LauService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());

		// Query_string is a laucode
		if (queryString.matches("\\d{8}"))
			propertyToCheck = skos_notation;

		// Query string is a name of the lau
		else
			propertyToCheck = skos_pref_label;

		StmtIterator iterator = queryModel.listStatements(new SimpleSelector(null, propertyToCheck, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement statementOfPropertyToCheck = iterator.nextStatement();
			Resource subject = statementOfPropertyToCheck.getSubject();
			RDFNode object = statementOfPropertyToCheck.getObject();

			/**
			 * If user provided nuts label e.g. "Paderborn" in query parameter OR If use
			 * provided nuts code e.g. DEA47 in query parameter
			 */
			if ((object.toString().equalsIgnoreCase(queryString) &&

			// This regex is to join a nutcode with its NS e.g.
			// http://data.europa.eu/nuts/code/DEA47
					(subject.toString().matches(REGEX_FOR_PREFIX_OF_LAUCODE_FROM_GERMANY + "\\d{8}"))))

			{
				setPrefixes(responseModel);
				Resource lau = responseModel.createResource(subject.toString());

				// Get all the properties with statements of the nuts
				StmtIterator StatementsOfAllProperties = subject.listProperties();
				while (StatementsOfAllProperties.hasNext()) {
					Statement aStatementOutOfAllStatements = StatementsOfAllProperties.nextStatement();
					if (!aStatementOutOfAllStatements.getObject().isAnon())
						lau.addProperty(aStatementOutOfAllStatements.getPredicate(),
								aStatementOutOfAllStatements.getObject());
					else {

						Resource blankNode = (Resource) aStatementOutOfAllStatements.getObject();
						if (!blankNode.hasProperty(dcat_centroid))
							lau.addProperty(dct_location,
									responseModel.createResource().addProperty(RDF.type, sf_polygon)
											.addProperty(ogc_as_wkt, blankNode.getProperty(ogc_as_wkt).getObject()));
						else {
							RDFNode centroid = blankNode.getProperty(dcat_centroid).getProperty(ogc_as_wkt).getObject();
							lau.addProperty(dct_location,
									responseModel.createResource().addProperty(RDF.type, sf_polygon)
											.addProperty(ogc_as_wkt, blankNode.getProperty(ogc_as_wkt).getObject())
											.addProperty(dcat_centroid, responseModel.createResource()
													.addProperty(RDF.type, sf_point).addProperty(sf_point, centroid)));
						}
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

	public void getAllLauTurtle() {

		Model queryModel = ModelFactory.createDefaultModel();
		Model responseModel = ModelFactory.createDefaultModel();

		// Turtle format response
		queryModel.read(new NutsService().getClass().getClassLoader().getResource("launuts.ttl").getFile().toString());
		StmtIterator iterator = queryModel.listStatements(new SimpleSelector(null, skos_notation, (RDFNode) null));
		while (iterator.hasNext()) {
			Statement skosStatement = iterator.nextStatement();
			Resource subject = skosStatement.getSubject();

			if (subject.toString().matches("http:\\/\\/projekt-opal\\.de\\/launuts\\/lau\\/DE\\/\\d{8}")) {
				setPrefixes(responseModel);
				Resource lau = responseModel.createResource(subject.toString());

				// Get all the properties with statements of the nuts
				StmtIterator StatementsOfAllProperties = subject.listProperties();
				while (StatementsOfAllProperties.hasNext()) {
					Statement aStatementOutOfAllStatements = StatementsOfAllProperties.nextStatement();
					if (!aStatementOutOfAllStatements.getObject().isAnon())
						lau.addProperty(aStatementOutOfAllStatements.getPredicate(),
								aStatementOutOfAllStatements.getObject());
					else {

						Resource blankNode = (Resource) aStatementOutOfAllStatements.getObject();
						if (!blankNode.hasProperty(dcat_centroid))
							lau.addProperty(dct_location,
									responseModel.createResource().addProperty(RDF.type, sf_polygon)
											.addProperty(ogc_as_wkt, blankNode.getProperty(ogc_as_wkt).getObject()));
						else {
							RDFNode centroid = blankNode.getProperty(dcat_centroid).getProperty(ogc_as_wkt).getObject();
							lau.addProperty(dct_location,
									responseModel.createResource().addProperty(RDF.type, sf_polygon)
											.addProperty(ogc_as_wkt, blankNode.getProperty(ogc_as_wkt).getObject())
											.addProperty(dcat_centroid, responseModel.createResource()
													.addProperty(RDF.type, sf_point).addProperty(sf_point, centroid)));
						}
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
