package cworks.treefs.server;

import cworks.json.JsonArray;
import cworks.json.JsonObject;
import cworks.treefs.TreeFsValidation;
import org.apache.log4j.Logger;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VertxContainer {

    /**
     * Logger
     */
    public static final Logger logger = Logger.getLogger(VertxContainer.class);

    /**
     * StartException, thrown when something wacky happens on startup
     */
    public class VertxContainerException extends Exception {
        public VertxContainerException() {
            super();
        }
        public VertxContainerException(String message) {
            super(message);
        }
        public VertxContainerException(String message, Throwable cause) {
            super(message, cause);
        }
        public VertxContainerException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * All configuration for this container
     */
    private JsonObject config = null;

    /**
     * flag used to control whether this thread is running or not
     */
    private volatile boolean running = true;

    /**
     * private constructor, use boot to create
     * @param config
     */
    private VertxContainer(JsonObject config) {

        this.config = config;
    }

    /**
     * Create a container with the default (development) settings
     * @return
     */
    public static VertxContainer newContainer() {
        return newContainer("localhost", 4444);
    }

    /**
     * Create a container and bind to the given host and port
     * @param host
     * @param port
     * @return
     */
    public static VertxContainer newContainer(String host, int port) {
        JsonObject config = new JsonObject();
        config.setString("moduleName", "treefs-server~treefs-server~SNAPSHOT");
        config.setNumber("port", port);
        config.setString("host", host);
        config.setString("treefs.clients", "build/resources/main/treefsclients.json");
        JsonArray classpath = new JsonArray(new String[]{
                "build/mods/treefs-server~treefs-server~SNAPSHOT",
                "build/mods/treefs-server~treefs-server~SNAPSHOT/lib/*"
        });
        config.setArray("classpath", classpath);
        // configure individual verticles
        JsonArray verticles = new JsonArray();
        verticles.addObject(new JsonObject()
                .setString("verticle", "TreeFsServer")
                .setString("type", "standard")
                .setNumber("instances", 1));
        verticles.addObject(new JsonObject()
                .setString("verticle", "cworks.treefs.server.handler.SiegeApp")
                .setString("type", "worker")
                .setNumber("instances", 7));
        verticles.addObject(new JsonObject()
                .setString("verticle", "cworks.treefs.server.worker.Worker")
                .setString("type", "worker")
                .setNumber("instances", 20));
        config.setArray("verticles", verticles);
        config.setBoolean("settings.response.pretty", true);
        
        return newContainer(config);
    }
    
    /**
     * Create a new VertxContainer instance
     * @param config
     * @return
     */
    public static VertxContainer newContainer(JsonObject config) {
        VertxContainer container = new VertxContainer(config);
        return container;
    }
    


    /**
     * Boot an embedded vertx container
     * @return
     */
    public VertxContainer start() throws VertxContainer.VertxContainerException {

        final String moduleName = config.getString("moduleName");
        if(TreeFsValidation.isNullOrEmpty(moduleName)) {
            throw new IllegalArgumentException("moduleName is required to boot VertxContainer");
        }

        JsonArray arr = config.getArray("classpath");
        List<File> classpathFiles = new ArrayList<File>();

        Iterator it = arr.iterator();
        while(it.hasNext()) {
            String entry = (String)it.next();
            classpathFiles.add(new File(entry));
        }
        if(classpathFiles.size() < 1) {
            File f = new File(VertxContainer.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation().getPath());
            classpathFiles.add(f);
        }

        final Integer instances = config.getInteger("instances", 1);

        final List<URL> urls = new ArrayList<URL>(classpathFiles.size());
        for(File f : classpathFiles) {
            try {
                urls.add(f.toURI().toURL());
            } catch (MalformedURLException ex) {
                throw new VertxContainerException("malformed classpath entry", ex);
            }
        }

        final Handler<AsyncResult<String>> doneHandler = new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if(event.succeeded()) {
                    logger.info("VertxContainer has booted: " + event.result());
                } else if(event.failed()) {
                    logger.error("VertxContainer has crashed and burned: " + event.result());
                }
            }
        };

        final PlatformManager pm = PlatformLocator.factory.createPlatformManager();
        Runnable runnable = () -> {

            logger.info("Starting VertxContainer...");

            pm.deployModuleFromClasspath(moduleName,
                new org.vertx.java.core.json.JsonObject(config.toMap()),
                instances,
                urls.toArray(new URL[urls.size()]),
                doneHandler);

            while(running) {
                try {
                    Thread.sleep((long) 1000 * 60 * 60);
                } catch (InterruptedException e) {
                    running = false;
                }
            }

            logger.info("Stopping VertxContainer...");
            pm.stop();
        };

        Thread thread = new Thread(runnable);
        thread.start();

        return this;

    }

    /**
     * shutdown this container
     */
    public void shutdown() {
        running = false;
    }

    /**
     * main entry point for the embedded Vertx container
     * @param args
     */
    public static void main(String[] args) {
        
        // starts a container
        try {
            String host = getHost(args);
            int port = getPort(args);
            
            final VertxContainer container = VertxContainer.newContainer(host, port).start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if(container != null) {
                        container.shutdown();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * TODO fix me I'm hard coded 
     * @param args
     * @return
     */
    private static int getPort(String[] args) {
        return 4444;
    }

    /**
     * TODO fix me I'm hard coded
     * @param args
     * @return
     */
    private static String getHost(String[] args) {
        return "localhost";
    }

}
