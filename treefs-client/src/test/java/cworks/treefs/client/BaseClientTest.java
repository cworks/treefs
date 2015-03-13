package cworks.treefs.client;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.server.TreeFsApp;
import net.cworks.http.Http;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class BaseClientTest {
    
    protected static TreeFsApp container;
    
    @BeforeClass
    public static void setUpClientTests() {

        // start container
        try {
            JsonObject config = new JsonObject();
            config.setString("treefs.clients", "src/test/resources/treefs-server/treefsclients.json");
            container = TreeFsApp.newApp(config);
            container.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @AfterClass
    public static void tearDownClientTests() {
        
        // stop container
        if(container != null) {
            container.shutdown();
        }
    }
    
    @Test
    public void testStartUp() {

        int attempts = 0;
        JsonObject response = null;
        while(attempts < 10) {
            try {
                String json = Http.get("http://localhost:4444/_ping")
                    .header("Accept", "application/json")
                    .header("treefs-client", "corbofett")
                    .header("User-Agent", "treefs/1.0.0")
                    .header("Accept-Charset", "UTF-8").asString();
                response = Json.asObject(json);
                if(response.getInteger("status", 0) == 200) {
                    Assert.assertTrue(true);
                    return;
                }
            } catch(IOException ex) {
                if(attempts == 9) {
                    ex.printStackTrace();
                }
            }
            delay(3);
            attempts++;
        }
        
        Assert.fail("Tried to pint TreeFsApp 10 times and failed.");
    }

    protected void delay(int sec) {
        try {
            Thread.sleep(1000 * sec);
        } catch (InterruptedException ex) { /* ignore */ }
    }
    
}
