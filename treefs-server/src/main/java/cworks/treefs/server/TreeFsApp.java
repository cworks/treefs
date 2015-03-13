package cworks.treefs.server;

import cworks.json.Json;
import cworks.json.JsonArray;
import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsValidation;
import net.cworks.http.Http;
import org.apache.log4j.Logger;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static cworks.treefs.common.ObjectUtils.isNull;
import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;

public class TreeFsApp {

    /**
     * Logger
     */
    public static final Logger logger = Logger.getLogger(TreeFsApp.class);

    /**
     * StartException, thrown when something wacky happens on startup
     */
    public class TreeFsAppException extends Exception {
        public TreeFsAppException() {
            super();
        }
        public TreeFsAppException(String message) {
            super(message);
        }
        public TreeFsAppException(String message, Throwable cause) {
            super(message, cause);
        }
        public TreeFsAppException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Start Thread for this container
     */
    private Thread startThread = null;
    
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
    private TreeFsApp(JsonObject config) {
        this.config = config;
    }

    /**
     * Create a new app with default (development) settings
     * @return
     */
    public static TreeFsApp newApp() {
        return newApp("localhost", 4444, TreeFs.home());
    }

    /**
     * Create a new app for the given home location
     * @param home
     * @return
     */
    public static TreeFsApp newApp(String home) {
        return newApp("localhost", 4444, home);
    }

    /**
     * Create a new app and bind to the given host and port
     * @param host
     * @param port
     * @return
     */
    public static TreeFsApp newApp(String host, int port, String home) {

        JsonObject config = defaultConfig();
        config.setString("host", host);
        config.setNumber("port", port);
        config.setString("treefs.home", home);
        
        return newApp(config);
    }
    
    /**
     * Create a new TreeFs app instance
     * @param config
     * @return
     */
    public static TreeFsApp newApp(JsonObject config) {
        
        JsonObject defaultConfig = defaultConfig();
        defaultConfig.merge(config);
        TreeFsApp container = new TreeFsApp(defaultConfig);
        
        return container;
    }

    /**
     * Boot an embedded vertx container
     * @return
     */
    public TreeFsApp start() throws TreeFsApp.TreeFsAppException {

        final String moduleName = config.getString("moduleName");
        if(TreeFsValidation.isNullOrEmpty(moduleName)) {
            throw new IllegalArgumentException("moduleName is required to boot TreeFsApp");
        }

        JsonArray arr = config.getArray("classpath");
        List<File> classpathFiles = new ArrayList<File>();

        Iterator it = arr.iterator();
        while(it.hasNext()) {
            String entry = (String)it.next();
            classpathFiles.add(new File(entry));
        }
        if(classpathFiles.size() < 1) {
            File f = new File(TreeFsApp.class
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
                throw new TreeFsAppException("malformed classpath entry", ex);
            }
        }

        final Handler<AsyncResult<String>> doneHandler = new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if(event.succeeded()) {
                    logger.info("TreeFsApp has booted: " + event.result());
                } else if(event.failed()) {
                    logger.error("TreeFsApp has crashed and burned: " + event.result());
                }
            }
        };

        final PlatformManager pm = PlatformLocator.factory.createPlatformManager();
        Runnable runnable = () -> {

            logger.info("Starting TreeFsApp...");

            pm.deployModuleFromClasspath(moduleName,
                new org.vertx.java.core.json.JsonObject(config.toMap()),
                instances,
                urls.toArray(new URL[urls.size()]),
                doneHandler);

            // keep this thread running until 'running' is false
            while(running) {
                delay(5);
            }

            logger.info("Stopping TreeFsApp...");
            pm.stop();
        };
        
        // save start thread reference and start thread
        this.startThread = new Thread(runnable);
        this.startThread.start();
        
        // test startup
        testStartUp();

        return this;
    }

    public void testStartUp() throws TreeFsAppException {
        testStartUp(10);
    }
    
    public void testStartUp(int attempts) throws TreeFsAppException {
        if(attempts < 1 || attempts > 100) {
            attempts = 10;
        }
        int i = 0;
        while(i < attempts) {
            try {
                String json = Http.get("http://" + this.config.getString("host")
                    + ":" + this.config.getInteger("port")
                    + "/_ping").header("treefs-client", "corbofett").asString();
                JsonObject response = Json.asObject(json);
                if(response.getInteger("status", 0) == 200) {
                    return;
                }
            } catch(IOException ex) {
                if(i == (attempts-1)) {
                    logger.error("Error conducting ping on startup.", ex);
                }
            }
            delay(3);
            i++;
        }

        throw new TreeFsAppException("START-UP: Tried to ping TreeFsApp 10 times and failed");
    }

    protected void delay(int sec) {
        try {
            Thread.sleep(1000 * sec);
        } catch (InterruptedException ex) { /* ignore */ }
    }

    /**
     * shutdown this container
     */
    public void shutdown() {
        running = false;
    }

    /**
     * Starter config that can be overridden
     * @return
     */
    private static JsonObject defaultConfig() {
        JsonObject config = new JsonObject();
        config.setString("moduleName", "cworks~treefs~1.0");
        config.setNumber("port", 4444);
        config.setString("host", "localhost");
        config.setString("treefs.clients", "build/resources/main/treefsclients.json");
        JsonArray classpath = new JsonArray(new String[]{
                "build/mods/cworks~treefs~1.0",
                "build/mods/cworks~treefs~1.0/lib/*"
        });
        config.setArray("classpath", classpath);
        // configure individual verticles
        JsonArray verticles = new JsonArray();
        verticles.addObject(new JsonObject()
                .setString("verticle", "cworks.treefs.server.TreeFsServer")
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
        return config;
    }

    /**
     * main entry point for the embedded Vertx container
     * @param args
     */
    public static void main(String[] args) {
        try {
            String home = getTreeFsHome(args);
            String host = getHost(args);
            int port = getPort(args);
            
            final TreeFsApp container = TreeFsApp.newApp(host, port, home).start();
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
     * Get port from command line or return default port
     * @param args
     * @return
     */
    private static int getPort(String[] args) {
        String port = getArgument(args, "--port", "4444");
        return Integer.valueOf(port);
    }

    /**
     * Get host from command line or return default localhost
     * @param args
     * @return
     */
    private static String getHost(String[] args) {
        return getArgument(args, "--host", "localhost");
    }

    /**
     * Get TreeFs home location from command line or return default home
     * @param args
     * @return
     */
    private static String getTreeFsHome(String[] args) {
        return getArgument(args, "--treefs.home", TreeFs.home());
    }
    
    private static String getArgument(String[] args, String name) {
        return getArgument(args, name, null);
    }
    
    private static String getArgument(String[] args, String name, String defaultValue) {
        if(isNull(args) || isNullOrEmpty(name)) {
            return defaultValue;
        }
        String argument = null;
        for(int i = 0; i < args.length; i++) {
            if(name.equalsIgnoreCase(args[i])) {
                if(args.length > (i+1)) {
                    argument = args[i+1];
                }
            }
        }
        if(isNullOrEmpty(argument)) {
            return defaultValue;
        }
        return argument;
    }
}
