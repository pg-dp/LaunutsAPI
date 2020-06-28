import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
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
    private static String msg = "";
    private static String nutsName = "";
    private static JSONArray coordinates = new JSONArray();
    private static String nutsidttl = " ";
    private static String nutsNamettl = "";

    @BeforeClass
    public static void setUpJson() throws IOException, JSONException {
        String jsonUrl = "http://localhost:8080/launuts/nuts/Karlsruhe/json";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpJsonGet = new HttpGet(jsonUrl);
        CloseableHttpResponse httpResponse = httpClient.execute(httpJsonGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonobject = jsonArray.getJSONObject(i);
            nutsid = jsonobject.getString("nutsid");
            msg = jsonobject.getString("msg");
            nutsLevel = jsonobject.getString("nutsLevel");
            nutsName = jsonobject.getString("nutsName");
            coordinates = jsonobject.getJSONArray("coordinates");
        }

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
        httpClient.close();
    }

    @Test
    public void testNutsId() throws Exception {
        Assert.assertEquals("DE12", nutsid);
    }

    @Test
    public void testNutsName() throws Exception {
        Assert.assertEquals("Karlsruhe", nutsName);
    }

    @Test
    public void testMsg() throws Exception {
        Assert.assertEquals("Query was successful", msg);
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
    public void testNutsNameTTL() throws Exception {
        Assert.assertEquals("Karlsruhe", nutsNamettl);
    }

    @Test
    public void testNutsIDTTL() throws Exception {
        Assert.assertEquals("DE12", nutsidttl);
    }
}