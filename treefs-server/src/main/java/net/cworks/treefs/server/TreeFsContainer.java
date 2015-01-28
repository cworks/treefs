package net.cworks.treefs.server;

import net.cworks.treefs.TreeFsValidation;
import org.apache.log4j.Logger;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeFsContainer {

    /**
     * Logger
     */
    public static final Logger logger = Logger.getLogger(TreeFsContainer.class);

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
    private TreeFsContainer(JsonObject config) {

        this.config = config;
    }

    /**
     * Create a new VertxContainer instance
     * @param config
     * @return
     */
    public static TreeFsContainer newContainer(JsonObject config) {

        TreeFsContainer container = new TreeFsContainer(config);
        return container;
    }

    /**
     * Boot an embedded vertx container
     * @return
     */
    public TreeFsContainer start() throws TreeFsContainer.VertxContainerException {

        final String moduleName = config.getString("moduleName");
        if(TreeFsValidation.isNullOrEmpty(moduleName)) {
            throw new IllegalArgumentException("moduleName is required to boot VertxContainer");
        }

        final Integer port = config.getInteger("port", 4444);
        final String host = config.getString("host", "localhost");

        JsonArray arr = config.getArray("classpath");
        List<File> classpathFiles = new ArrayList<File>();

        Iterator it = arr.iterator();
        while(it.hasNext()) {
            String entry = (String)it.next();
            classpathFiles.add(new File(entry));
        }
        if(classpathFiles.size() < 1) {
            File f = new File(TreeFsContainer.class
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

        // need a Runnable
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                logger.info("starting VertxContainer...");

                pm.deployModuleFromClasspath(moduleName,
                    config,
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

                logger.info("stopping VertxContainer...");
                pm.stop();
            }
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
     * main entry point for the embedded Vertx container, typically used in unit tests
     * @param args
     */
    public static void main(String[] args) {

        JsonObject config = new JsonObject();
        config.putString("moduleName", "treefs-server~treefs-server~SNAPSHOT");
        config.putNumber("port", 4444);
        config.putString("host", "localhost");
        JsonArray classpath = new JsonArray(new String[]{
            "build/mods/treefs-server~treefs-server~SNAPSHOT",
            "build/mods/treefs-server~treefs-server~SNAPSHOT/lib/*"
        });
        config.putArray("classpath", classpath);

        // starts a container
        try {
            TreeFsContainer container = TreeFsContainer.newContainer(config).start();

            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            container.shutdown();
        } catch (VertxContainerException ex) {
            ex.printStackTrace();
        }
    }

}
