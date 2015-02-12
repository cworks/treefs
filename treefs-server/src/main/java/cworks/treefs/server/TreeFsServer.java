package cworks.treefs.server;

import cworks.treefs.TreeFs;
import cworks.treefs.server.core.BodyParser;
import cworks.treefs.server.core.ErrorHandler;
import cworks.treefs.server.core.HeaderParser;
import cworks.treefs.server.core.HttpModule;
import cworks.treefs.server.core.HttpRouter;
import cworks.treefs.server.handler.FileSystemHandler;
import cworks.treefs.server.handler.HttpServices;
import cworks.treefs.server.handler.UriHandler;
import org.apache.log4j.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * TreeFs API operations
 *
 * Path Operations
 *
 * Create:   POST   /corbett/projects/house/bathroom (body contains path properties)
 * Update:   PUT    /corbett/projects/house/bathroom (body contains path properties)
 * Partial:  PATCH  /corbett/projects/house/bathroom (body contains partial properties)
 * Remove:   DELETE /corbett/projects/house/bathroom
 * Retrieve: GET    /corbett/projects/house/bathroom
 *
 * These actions don't really map into the simple HTTP verb CRUD operations (POST, GET, PUT, DELETE)
 * but are required to make the API useful.  REST purest folks only design URI(s) that are Nouns
 * for example POST /this/should/be/a/person/place/or/thing, and never mixed with Verbs because
 * the standard HTTP Verbs (POST, GET, PUT and DELETE) should be the only operations you need.  Well
 * no.  Unfortunately real-life situations call for more actions.  In TreeFs some of those actions are
 * copy, move, meta and trash.  Each an action on a Noun.
 *
 * Consider the Copy verb.  Using POST /a/path to copy a path conflicts with POST /a/path operation
 * which creates a path.  You see there is no way to distinguish what your intent is.  The design
 * choice in TreeFs is to append certain Verbs onto the end of the Noun URI.  Each appended Verb
 * is looked upon as a sub-resource and can be managed as such.
 *
 * User sub-resource URIs
 *
 * Copy:     POST   /corbett/projects/house/bathroom/#copy (body contains copy properties)
 * Move:     PUT    /corbett/projects/house/bathroom/#move (body contains move properties)
 * Meta:     GET    /corbett/projects/house/bathroom/#meta
 * Trash:    PUT    /corbett/projects/house/bathroom/#trash
 *
 * Leaf Operations
 *
 * Create:   POST   /corbett/projects/house/bathroom/plans.pdf (body contains leaf metadata)
 * Update:   PUT    /corbett/projects/house/bathroom/plans.pdf (body contains leaf properties)
 * Partial:  PATCH  /corbett/projects/house/bathroom/plans.pdf (body contains leaf properties)
 * Remove:   DELETE /corbett/projects/house/bathroom/plans.pdf
 * Retrieve: GET    /corbett/projects/house/bathroom/plans.pdf
 *
 * Copy:     POST   /corbett/projects/house/bathroom/plans.pdf/#copy (body contains copy properties)
 * Move:     PUT    /corbett/projects/house/bathroom/plans.pdf/#move (body contains move properties)
 * Meta:     GET    /corbett/projects/house/bathroom/plans.pdf/#meta
 * Trash:    PUT    /corbett/projects/house/bathroom/plans.pdf/#trash
 *
 */
public class TreeFsServer extends Verticle {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(TreeFsServer.class);

    /**
     * Default host this verticle is associated with
     */
    private static final String  DEFAULT_HOST = "localhost";

    /**
     * Default port this verticle binds to
     */
    private static final Integer DEFAULT_PORT = 4444;

    /**
     * Context root of TreeFs
     */
    public static final String TREEFS_ROOT = "/";

    /**
     * Startup TreeFsServer verticle which will handle IO to TreeFs
     * If treefs.home cannot be found this verticle will stop the container.
     */
    public void start() {

        // perform initialization and log boot info before anything else
        try {
            initTreeFs(container.config());
            logBoot(container.config(), container.env());
        } catch (IOException ex) {
            String msg = "Exception initializing TreeFsServer Verticle.";
            logger.error(msg, ex);
            throw new VertxException(msg, ex);
        }

        // initTreeFs above initializes TreeFs data
        String home = TreeFs.home();
        if(home == null) {
            String msg = "treefs.home NOT FOUND, please set in ENV, System Property or Vertx Config";
            logger.error(msg);
            throw new VertxException(msg);
        } else {
            logger.info("Setting System property treefs.home=" + home);
            System.setProperty("treefs.home", home);
        }

        String  host = container.config().getString("host", DEFAULT_HOST);
        Integer port = container.config().getInteger("port", DEFAULT_PORT);

        HttpModule module = new HttpModule(this, TREEFS_ROOT);

        module.use(new ErrorHandler(false))
            .use(new UriHandler())
            .use(new HeaderParser())
            .use(new FileSystemHandler())
            .use(new BodyParser(TreeFs.uploadDir()));

        // sub-resource need to come before actual resources so matching works...need to fix this
        module.use(new HttpRouter().get("/:fs/.*/meta$",
            HttpServices.metadataService()));
        module.use(new HttpRouter().delete("/:fs/.*/trash$",
            HttpServices.trashPathService()));
        module.use(new HttpRouter().post("/:fs/.*/cp$",
            HttpServices.copyService()));
        module.use(new HttpRouter().put("/:fs/.*/mv$",
            HttpServices.moveService()));

        // main resources
        module.use(new HttpRouter().post("/:fs/.*",
            HttpServices.createPathService()));
        module.use(new HttpRouter().get("/:fs/.*",
            HttpServices.readPathService()));
        // module.use(new HttpRouter().put("/:fs/.*",
        //    HttpServices.updatePathService()));
        module.use(new HttpRouter().delete("/:fs/.*",
            HttpServices.deleteService()));

        // siege service is a simple load testing hook
        module.use("/siege",
            new BodyParser(TreeFs.uploadDir()),
            new HttpRouter().post("/.*"),
            HttpServices.siegeService());

        // ping hook for assisting load balancers
        module.use("/ping",
            new HttpRouter().get("/.*"),
            HttpServices.pingService());

        // be slow to speak and quick to listen
        module.listen(port, host, new Handler<Boolean>() {
            @Override
            public void handle(Boolean status) {
                String message = "";
                if (status) {
                    message = "TreeFsServer is taking root!!!";
                    logger.info(message);
                } else {
                    message = "TreeFsServer has been cut down.";
                    logger.error(message);
                }
            }
        });
    }

    private void initTreeFs(JsonObject jsonConfig) throws IOException {

        Map config = jsonConfig.toMap();
        Map env    = container.env();

        TreeFs.mergeIn(config, env);

        String homeDir     = TreeFs.home();
        String mountDir    = TreeFs.mount();
        String downloadDir = TreeFs.downloadDir();
        String uploadDir   = TreeFs.uploadDir();

        createIfNotExist(homeDir);
        createIfNotExist(mountDir);
        createIfNotExist(downloadDir);
        createIfNotExist(uploadDir);
    }

    private void logBoot(JsonObject config, Map env) {

        JsonObject je = new JsonObject(env);
        JsonObject jc = new JsonObject(config.toMap());
        JsonObject json = new JsonObject();

        json.putObject("env",    je);
        json.putObject("config", jc);

        json.putString("treefs.home",      TreeFs.home());
        json.putString("treefs.mount",     TreeFs.mount());
        json.putString("treefs.downloads", TreeFs.downloadDir());
        json.putString("treefs.uploads",   TreeFs.uploadDir());

        logger.info("*** TREEFS properties ***");
        logger.info(json.encodePrettily());
    }

    private void createIfNotExist(String dir) throws IOException {
        Path path = Paths.get(dir);
        if(!Files.exists(path)) {
            Files.createDirectory(path);
            logger.info("created TreeFs directory: " + path.toString());
        }
    }

}
