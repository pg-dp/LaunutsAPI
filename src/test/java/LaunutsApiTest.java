import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.apache.jena.vocabulary.SKOS.notation;
import static org.apache.jena.vocabulary.SKOS.prefLabel;

public class LaunutsApiTest {

    private static String nutsLevel = "";
    private static String nutsid = " ";
    private static String nutsName = "";
    private static JSONArray coordinates = new JSONArray();
    private static String nutsidttl = " ";
    private static String nutsNamettl = "";
    private static String lauId = "";
    private static String lauName = "";
    private static JSONArray lauCoordinates = new JSONArray();
    private static String lauIdTTL = " ";
    private static String lauNameTTL = "";
    private static String lauIdInvalid = "";
    private static String nutsCoordinatesTTL = "";


    @BeforeClass
    public static void setUpJson() throws IOException, JSONException {
        String jsonUrl = "http://localhost:8080/launuts/nuts/Karlsruhe/json";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpJsonGet = new HttpGet(jsonUrl);
        CloseableHttpResponse httpResponse = httpClient.execute(httpJsonGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        JSONArray jsonArray = new JSONArray(content);
        JSONObject jsonobject = jsonArray.getJSONObject(0);
        nutsid = jsonobject.getString("nutsId");
        nutsLevel = jsonobject.getString("nutsLevel");
        nutsName = jsonobject.getString("nutsName");
        coordinates = jsonobject.getJSONArray("coordinates");

        httpClient.close();
    }

    @BeforeClass
    public static void setUpTTL() throws IOException, JSONException {

        String urlTTL = "http://localhost:8080/launuts/nuts/Karlsruhe/ttl";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpTLlGet = new HttpGet(urlTTL);
        CloseableHttpResponse httpResponseTTL = httpClient.execute(httpTLlGet);

        StringWriter writer = new StringWriter();
        IOUtils.copy(httpResponseTTL.getEntity().getContent(), writer, "UTF-8");
        String fixedString = writer.toString();
        Model defaultModel = ModelFactory.createDefaultModel();
        defaultModel.read(new StringReader(fixedString),
                urlTTL,
                "TURTLE");

        Resource resource = defaultModel.createResource("http://data.europa.eu/nuts/code/DE12");

        Statement statementNutsCode = defaultModel.getProperty(resource, notation);
        nutsidttl = statementNutsCode.getObject().toString();

        Statement statementPreflabel = defaultModel.getProperty(resource, prefLabel);
        nutsNamettl = statementPreflabel.getObject().toString();

        Property location = defaultModel.createProperty("http://purl.org/dc/terms/Location");
        StmtIterator locationItr = defaultModel.listStatements
                (resource, location, (RDFNode) null);

        if (locationItr.hasNext()) {

            Statement statementConformsTo = locationItr.nextStatement();
            RDFNode object = statementConformsTo.getObject();
            Resource objectAsResource = (Resource) object;
            String uri = "http://www.opengis.net/ont/geosparql#";
            Property polygon = defaultModel.createProperty(uri + "asWKT");
            Statement statement = defaultModel.getProperty(objectAsResource, polygon);
            RDFNode node = statement.getObject();
            if (node.isLiteral()) {
                nutsCoordinatesTTL = node.asLiteral().getString();
            }
        }

        httpClient.close();
    }

    @BeforeClass
    public static void setUpLauJson() throws IOException, JSONException {
        String jsonUrl = "http://localhost:8080/launuts/lau/de_09577125/json";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpJsonGet = new HttpGet(jsonUrl);
        CloseableHttpResponse httpResponse = httpClient.execute(httpJsonGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        JSONArray jsonArray = new JSONArray(content);

        JSONObject jsonobject = jsonArray.getJSONObject(0);
        lauId = jsonobject.getString("lauId");
        lauName = jsonobject.getString("lauName");
        lauCoordinates = jsonobject.getJSONArray("coordinates");

        httpClient.close();
    }

    @BeforeClass
    public static void setUpLauTTL() throws IOException, JSONException {

        String urlTTL = "http://localhost:8080/launuts/lau/Dittenheim/ttl";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpTLlGet = new HttpGet(urlTTL);
        CloseableHttpResponse httpResponseTTL = httpClient.execute(httpTLlGet);

        StringWriter writer = new StringWriter();
        IOUtils.copy(httpResponseTTL.getEntity().getContent(), writer, "UTF-8");
        String fixedString = writer.toString();
        Model defaultModel = ModelFactory.createDefaultModel();
        defaultModel.read(new StringReader(fixedString),
                urlTTL,
                "TURTLE");

        Resource resource = defaultModel.createResource("http://projekt-opal.de/launuts/lau/DE/09577122");

        Statement statementlauCode = defaultModel.getProperty(resource, notation);
        lauIdTTL = statementlauCode.getObject().toString();

        Statement statementPreflabel = defaultModel.getProperty(resource, prefLabel);
        lauNameTTL = statementPreflabel.getObject().toString();
        httpClient.close();
    }

    @BeforeClass
    public static void negativeTest() throws IOException, JSONException {

        String urlTTL = "http://localhost:8080/launuts/lau/Dittenim/json";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpTLlGet = new HttpGet(urlTTL);
        CloseableHttpResponse httpResponse = httpClient.execute(httpTLlGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);
        JSONArray jsonArray = new JSONArray(content);

        JSONObject jsonobject = jsonArray.getJSONObject(0);
        lauIdInvalid = jsonobject.getString("lauId");

        httpClient.close();
    }

    @Test
    public void testNutsId() throws Exception {
        Assert.assertEquals("DE12", nutsid);
    }

    @Test
    public void testLauId() throws Exception {
        Assert.assertEquals("DE_09577125", lauId);
    }

    @Test
    public void testNutsName() throws Exception {
        Assert.assertEquals("Karlsruhe", nutsName);
    }

    @Test
    public void testLauName() throws Exception {
        Assert.assertEquals("Ellingen, St", lauName);
    }

    @Test
    public void testNutsLevel() throws Exception {
        Assert.assertEquals("LEVL_2", nutsLevel);
    }

    @Test
    public void testCoordinates() throws Exception {
        String arrays = "[[[49.427, 9.604], [49.364, 9.443], [49.293, 9.049], " +
                "[49.194, 8.818], [49.058, 8.878], [49.035, 8.876], [48.867, 8.929], " +
                "[48.778, 8.804], [48.522, 8.769], [48.504, 8.756], [48.416, 8.774], " +
                "[48.378, 8.737], [48.349, 8.304], [48.603, 8.222], [48.719, 7.96], " +
                "[48.967, 8.233], [48.981, 8.261], [48.99, 8.277], [49.08, 8.34], " +
                "[49.25, 8.413], [49.283, 8.467], [49.29, 8.487], [49.312, 8.463], " +
                "[49.411, 8.497], [49.444, 8.473], [49.542, 8.423], [49.574, 8.423], " +
                "[49.583, 8.422], [49.52, 8.581], [49.471, 8.932], [49.526, 9.083], " +
                "[49.577, 9.103], [49.664, 9.411], [49.636, 9.507], [49.474, 9.519], " +
                "[49.427, 9.604]]]";

        String temp = arrays.replaceAll(" ", "");
        String coordinatesGot = coordinates.toString();
        Assert.assertEquals(temp,
                coordinatesGot);
    }

    @Test
    public void testLauCoordinates() throws Exception {
        String arrays = "[[[49.06881350000003,10.868307500000071],[49.07946209700003,10.874846536000064]," +
                "[49.08743217800003,10.873127329000056],[49.09703050000007,10.88661650000006]," +
                "[49.08418420000004,10.907111248000035],[49.09281790500006,10.919888021000077]," +
                "[49.09022463800005,10.936474906000058],[49.08447845000006,10.941273599000056]," +
                "[49.08544671300007,10.952453879000075],[49.090076002000046,10.957865948000062]," +
                "[49.08881412200003,10.975496303000057],[49.08278450000006,10.977152500000045]," +
                "[49.079923198000074,10.97816343200003],[49.07594637600005,10.98861557500004]," +
                "[49.062784571000066,10.992995144000076],[49.06030227400004,10.987197021000043]," +
                "[49.05406831400006,10.985828591000029],[49.04750100000007,10.97670850000003]," +
                "[49.04533051000004,10.96052885000006],[49.05291532000007,10.951518226000076]," +
                "[49.05679616900005,10.936995211000067],[49.06139827900006,10.93386840100004]," +
                "[49.06382272500008,10.919888021000077],[49.06140636200007,10.919888021000077]," +
                "[49.05922252100004,10.919888021000077],[49.05550200000005,10.899600500000076]," +
                "[49.05661288600004,10.882821964000073],[49.06403397200006,10.87693803600007]," +
                "[49.06881350000003,10.868307500000071]]]";

        String coordinatesGot = lauCoordinates.toString();
        Assert.assertEquals(arrays,
                coordinatesGot);
    }

    @Test
    public void testNutsNameTTL() throws Exception {
        Assert.assertEquals("Karlsruhe", nutsNamettl);
    }

    @Test
    public void testNutsIDTTL() throws Exception {
        Assert.assertEquals("DE12", nutsidttl);
    }

    @Test
    public void testLauNameTTL() throws Exception {
        Assert.assertEquals("Dittenheim", lauNameTTL);
    }

    @Test
    public void testLauIDTTL() throws Exception {
        Assert.assertEquals("09577122", lauIdTTL);
    }

    @Test
    public void testInvalidUrl() throws Exception {
        Assert.assertNotEquals("Dittenheim", lauIdInvalid);
    }

    @Test
    public void testTTLCoordinates() throws Exception {
        //Taken fron launuts.ttl
        String estimatedCoordinates = "POLYGON ((49.427 9.604, 49.364 9.443, 49.293 9.049, 49.194 8.818, " +
                "49.058 8.878, 49.035 8.876, 48.867 8.929, 48.778 8.804, 48.522 8.769, 48.504 8.756, " +
                "48.416 8.774, 48.378 8.737, 48.349 8.304, 48.603 8.222, 48.719 7.96, 48.967 8.233, " +
                "48.981 8.261, 48.99 8.277, 49.08 8.34, 49.25 8.413, 49.283 8.467, 49.29 8.487, " +
                "49.312 8.463, 49.411 8.497, 49.444 8.473, 49.542 8.423, 49.574 8.423, " +
                "49.583 8.422, 49.52 8.581, 49.471 8.932, 49.526 9.083, 49.577 9.103," +
                " 49.664 9.411, 49.636 9.507, 49.474 9.519, 49.427 9.604))";

        Assert.assertEquals(estimatedCoordinates, nutsCoordinatesTTL);
    }
}
