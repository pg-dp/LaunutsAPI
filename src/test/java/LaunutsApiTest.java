import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class LaunutsApiTest {

    static String nutsLevel = "";
    static String nutsid = " ";
    static String msg = "";
    static String nutsName = "";
    static JSONArray coordinates = new JSONArray();

    static String nutsLevelttl = "";
    static String nutsidttl = " ";
    static String msgttl = "";
    static String nutsNamettl = "";
    static JSONArray coordinatesttl = new JSONArray();

    @BeforeClass
    public static void setUp() throws IOException, JSONException {
        String jsonUrl = "http://localhost:8080/launuts/nuts/Karlsruhe/json";
        String jsonUrlTTL = "http://localhost:8080/launuts/nuts/Karlsruhe/ttl";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpJsonGet = new HttpGet(jsonUrl);
        HttpGet httpTLlGet = new HttpGet(jsonUrlTTL);
        CloseableHttpResponse httpResponse = httpClient.execute(httpJsonGet);
        CloseableHttpResponse httpResponseTTL = httpClient.execute(httpTLlGet);

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

        StringWriter writer = new StringWriter();
        IOUtils.copy(httpResponseTTL.getEntity().getContent(), writer, "UTF-8");
        String fixedString = writer.toString();
        Model defaultModel = ModelFactory.createDefaultModel();
        defaultModel.read(new StringReader(fixedString),
                jsonUrlTTL,
                "TURTLE");

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
        Assert.assertEquals("[[[49.427, 9.604], [49.364, 9.443], [49.293, 9.049], [49.194, 8.818], [49.058, 8.878], [49.035, 8.876], [48.867, 8.929], [48.778, 8.804], [48.522, 8.769], [48.504, 8.756], [48.416, 8.774], [48.378, 8.737], [48.349, 8.304], [48.603, 8.222], [48.719, 7.96], [48.967, 8.233], [48.981, 8.261], [48.99, 8.277], [49.08, 8.34], [49.25, 8.413], [49.283, 8.467], [49.29, 8.487], [49.312, 8.463], [49.411, 8.497], [49.444, 8.473], [49.542, 8.423], [49.574, 8.423], [49.583, 8.422], [49.52, 8.581], [49.471, 8.932], [49.526, 9.083], [49.577, 9.103], [49.664, 9.411], [49.636, 9.507], [49.474, 9.519], [49.427, 9.604]]]",
                coordinates);
    }

}