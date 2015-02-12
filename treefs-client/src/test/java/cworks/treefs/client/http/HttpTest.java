package cworks.treefs.client.http;


import net.cworks.http.Http;
import net.cworks.json.JsonObject;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.cworks.json.Json.Json;

public class HttpTest {

    /**
     * Default HTTP connection timeout
     */
    public static final int CONNECTION_TIMEOUT = 60000;

    /**
     * Default timeout to use for requests to TreeFs
     */
    public static final int READ_TIMEOUT = 60000;

    /**
     * HttpClient used in this Unit Test
     */
    private static HttpClient client = null;

    @BeforeClass
    public static void beforeClass() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setConnectionRequestTimeout(READ_TIMEOUT).build();
        HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .setConnectionManager(connectionManager)
            .build();
    }

    @Test
    public void exampleHttpGet() throws IOException {

        String response = Http.get("http://localhost:4444/treefs/default/demo/n1_1")
             .header(new BasicHeader("Accept", "application/json"))
             .header(new BasicHeader("treefs-client", "corbofett"))
             .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
             .header(new BasicHeader("Accept-Charset", "UTF-8"))
             .use(client)
            .asString();
        System.out.println(response.toString());
    }

    @Test
    public void exampleHttpPost_CreateFolder() throws IOException {

        String body = "{" +
            "\"description\":\"A folder of random stuff\"," +
            "\"metadata\":{" +
                "\"someString\":\"Happy Happy Happy\"," +
                "\"someNumber\":100," +
                "\"someArray\":[\"apples\", \"bananas\", \"blueberries\"]," +
                "\"someObject\": {" +
                    "\"name\":\"Chuck Norris\"," +
                    "\"Occupation\":\"Bustin Heads\"" +
                "}" +
            "}" +
        "}";

        // uses internal http client, not one create in beforeClass method
        String response = Http.post("http://localhost:4444/treefs/default/unittest/httptest/testfolder")
            .body(body)
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json"))
            .header(new BasicHeader("Content-Type", "application/json"))
            .asString();

        System.out.println("post> " + response);

        // uses internal http client, not one created in beforeClass method
        response = Http.get("http://localhost:4444/treefs/default/unittest/httptest/testfolder")
            .header(new BasicHeader("Accept", "application/json"))
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .asString();

        System.out.println("get> " + response);

    }

    /**
     * This example demonstrates how to use Http.multipart to upload a File.
     * @throws IOException
     */
    @Test
    public void exampleHttpMultipart_CreateFile() throws IOException {

        String response = Http.multipart("http://localhost:4444/treefs/default/" +
            "unittest/httptest/testfolder/content")
            .upload(new File("src/test/resources/data/nacho_libre.pdf"),
                ContentType.create("application/pdf"))
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json")).asString();

        System.out.println("multipart> " + response);


    }

    /**
     * This example demonstrates how to use Http.multipart to upload a File and override
     * the default File.name give a snippet of JSON.
     *
     * treefs-server will override the original File.name (nacho_libre.pdf)
     * with the name provided in the JSON object passed as an argument to the
     * multipart request
     *
     * @throws IOException
     */
    @Test
    public void exampleHttpMultipart_CreateFile_CustomFilename() throws IOException {

        String json = "{" +
            "\"name\":\"nacho_libre_2.pdf\"," +
            "\"metadata\":{\"file_type\":\"application/pdf\"}" +
        "}";

        String response = Http.multipart("http://localhost:4444/treefs/default/" +
                "unittest/httptest/testfolder/content")
                .upload(new File("src/test/resources/data/nacho_libre.pdf"),
                        ContentType.create("application/pdf"))
                .data("file", json)
                .header(new BasicHeader("treefs-client", "corbofett"))
                .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
                .header(new BasicHeader("Accept-Charset", "UTF-8"))
                .header(new BasicHeader("Accept", "application/json")).asString();

        System.out.println("multipart> " + response);
    }

    @Test
    public void testReadLine() throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(
            "src/test/resources/data/test.txt"), Charset.forName("UTF-8"));
        for(String line : lines) {
            System.out.println(line);
        }

    }

    /**
     * This example demonstrates how to use Http to overwrite an existing File (resource)
     * in treefs-server.
     *
     * There are 2 ways to implement this without introducing domain specific logic into Http.
     *
     * 1) Perform DELETE then POST
     * 2) Perform PUT
     *
     * @throws IOException
     */
    @Test
    public void exampleHttpMultipart_OverwriteFile() throws IOException {

        String json = "{\"name\":\"nacho_libre_overwrite.pdf\"}";

        String response = Http.multipart("http://localhost:4444/treefs/default/" +
            "unittest/httptest/testfolder/content")
            .upload(new File("src/test/resources/data/nacho_libre.pdf"), ContentType.create("application/pdf"))
            .data("file", json)
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json")).asString();

        // should be some JSON in this case
        JsonObject jr = new JsonObject(response);
        //Assert.assertEquals("file", jr.getString("type"));
        Assert.assertEquals("nacho_libre_overwrite.pdf", jr.getString("name"));

        response = Http.multipart("http://localhost:4444/treefs/default/" +
            "unittest/httptest/testfolder/content")
            .upload(new File("src/test/resources/data/nacho_libre.pdf"), ContentType.create("application/pdf"))
            .data("file", json)
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json")).asString();

        // should be statusCode 400 because overwrite argument wasn't provided
        jr = new JsonObject(response);
        Assert.assertEquals(Integer.valueOf(400), jr.getObject("error").getInteger("statusCode"));

        response = Http.multipart("http://localhost:4444/treefs/default/" +
            "unittest/httptest/testfolder/content")
            .upload(new File("src/test/resources/data/nacho_libre.pdf"), ContentType.create("application/pdf"))
            .data("file", json)
            .param("overwrite", "true")
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json")).asString();

        // should be some JSON in this case because overwrite argument was true
        jr = new JsonObject(response);
        Assert.assertEquals("file", jr.getString("type"));
        Assert.assertEquals("nacho_libre_overwrite.pdf", jr.getString("name"));

        System.out.println("multipart> " + response);

    }

    /**
     * Delete examples, performing an Http.delete on a resource actually moves it to trash.
     * Then Delete on resource in trash permanently deletes it.
     *
     * TODO remove this method, make other tests self contained
     * @throws IOException
     */
    //@AfterClass
    public static void exampleHttp_Delete() throws IOException {

        String response = Http.delete("http://localhost:4444/treefs/default/unittest")
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json")).asString();

        System.out.println("exampleHttp_Delete> " + response);

        JsonObject jo = new JsonObject(response);
        Assert.assertEquals(Integer.valueOf(400), jo.getObject("error").getInteger("statusCode"));

        response = Http.delete("http://localhost:4444/treefs/default/unittest")
            .header(new BasicHeader("treefs-client", "corbofett"))
            .header(new BasicHeader("User-Agent", "treefs/1.0.0"))
            .header(new BasicHeader("Accept-Charset", "UTF-8"))
            .header(new BasicHeader("Accept", "application/json"))
            .param("forceDelete", "true").asString();

        System.out.println("exampleHttp_Delete> " + response);

        jo = new JsonObject(response);

        Assert.assertEquals(Integer.valueOf(200), jo.getObject("success").getInteger("statusCode"));

    }

    @Test
    public void siege() throws IOException, ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(13);

        for(int i = 0; i < 100; i++) {
            final int fi = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    JsonObject bodyContent = Json().object()
                        .number("siegeVal", 1000000)
                        .number("siegeId", (fi + 1))
                        .build();
                    try {
                        String response = Http.post("http://localhost:4444/siege")
                            .body(bodyContent.asString())
                            .header(new BasicHeader("Content-Type", "application/json"))
                            .header(new BasicHeader("Accept-Charset", "UTF-8"))
                            .header(new BasicHeader("Accept", "application/json")).asString();
                        System.out.println(response);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            });
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        executor.awaitTermination(5, TimeUnit.MINUTES);
        System.out.println("Finished all threads");
    }





}
