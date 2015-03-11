package cworks.treefs.client;

import cworks.treefs.server.VertxContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class BaseClientTest {
    
    protected static VertxContainer container;
    
    @BeforeClass
    public static void setUpClientTests() {

        // start container
        try {
            container = VertxContainer.newContainer().start();
        } catch (VertxContainer.VertxContainerException ex) {
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
    
}
