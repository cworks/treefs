package net.cworks.treefs.server;

import org.apache.log4j.Logger;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Iterator;

import static net.cworks.treefs.common.ObjectUtils.isNull;
import static net.cworks.treefs.common.ObjectUtils.isNullOrEmpty;

/**
 * MainVerticle boots TreeFsServer and misc verticles specificed in treefsconfig.json
 *
 * @author comartin
 */
public class MainVerticle extends Verticle {

    /**
     * Logger for this verticle
     */
    private static final Logger logger = Logger.getLogger(MainVerticle.class);

    /**
     * Entry-point for TreeFs-Server and TreeFs API
     */
    private static final String TREEFS_SERVER = TreeFsServer.class.getName();

    /**
     * Main Verticle for TreeFs.  All server initialization should take place here.
     */
    public void start() {

        // Reads configuration from treefsconfig.json
        final JsonObject config = container.config();
        logger.info("treefs config: " + config.encodePrettily());
        JsonObject treefsSrvConfig = null;

        // perform some arbitrary Verticle deployments if configuration exists
        JsonArray verticles = config.getArray("verticles");
        if(!isNull(verticles) && verticles.size() > 0) {
            Iterator it = verticles.iterator();
            while (it.hasNext()) {
                JsonObject verticleConfig = (JsonObject) it.next();
                String clazzName = verticleConfig.getString("verticle");
                // all verticles must have a clazzName
                if (isNullOrEmpty(clazzName)) {
                    logger.warn("configuration contains a Verticle without a clazzName #craycray.");
                    continue;
                }
                // we always deploy TREEFS_VERTICLE last
                if (TREEFS_SERVER.equalsIgnoreCase(clazzName)) {
                    treefsSrvConfig = verticleConfig;
                    continue;
                }

                Integer instances = verticleConfig.getInteger("instances", 1);
                String verticleType = verticleConfig.getString("type", "standard");
                if ("worker".equalsIgnoreCase(verticleType)) {
                    deployWorkerVerticle(clazzName, config, instances);
                } else if ("standard".equalsIgnoreCase(verticleType)) {
                    deployStandardVerticle(clazzName, config, instances);
                } else {
                    // don't know what was entered, so deploy a standard verticle
                    logger.warn("Verticle type of " + verticleType + " specified for verticle: "
                        + clazzName + ".  Not familiar with that type so I can't deploy #craycray");
                }
            }
        }

        // does important init stuff for TREEFS_SERVER on successful deployment
        Handler deployHandler = new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> deployResult) {
                if(deployResult.succeeded()) {
                    logger.info(deployResult.result());
                } else if(deployResult.failed()) {
                    logger.info(deployResult.result());
                    container.undeployVerticle(TREEFS_SERVER);
                }
            }
        };

        // We always deploy TREEFS_SERVER but if custom config is given then use that
        if(!isNull(treefsSrvConfig)) {
            String verticleType = treefsSrvConfig.getString("type", "standard");
            Integer instances = treefsSrvConfig.getInteger("instances", 1);
            if("worker".equalsIgnoreCase(verticleType)) {
                deployWorkerVerticle(TREEFS_SERVER, config, instances, deployHandler);
            } else {
                deployStandardVerticle(TREEFS_SERVER, config, instances, deployHandler);
            }
        } else {
            // No custom config was given for TREEFS_SERVER so deploy with default settings
            deployStandardVerticle(TREEFS_SERVER, config, 1, deployHandler);
        }
    }

    /**
     * Deploys instance number of clazzName Verticles into the Vertx.IO container
     * @param clazzName Verticle to deploy
     * @param config Configuration for Verticle
     * @param instances Number of instances to create
     * @param handler deployment status handler
     */
    void deployStandardVerticle(String clazzName, JsonObject config, Integer instances,
        Handler<AsyncResult<String>> handler) {

        container.deployVerticle(clazzName, config, instances, handler);
    }

    /**
     * Deploys instance number of clazzName Verticles into the Vertx.IO container
     * @param clazzName Verticle to deploy
     * @param config Configuration for Verticle
     * @param instances Number of instances to create
     */
    void deployStandardVerticle(String clazzName, JsonObject config, Integer instances) {
        deployStandardVerticle(clazzName, config, instances,
            new AsyncResultHandler<String>() {
                @Override
                public void handle(AsyncResult<String> deployResult) {
                    if (deployResult.succeeded()) {
                        logger.info(deployResult.result());
                    } else {
                        logger.error(deployResult.result());
                    }
                }
            }
        );
    }

    /**
     * Deploys instances number of clazzName Worker Verticles into the Vertx.IO container
     * @param clazzName Verticle to deploy as a Worker
     * @param config Configuration for Verticle
     * @param instances Number of instances to create
     * @param handler deployment status handler
     */
    void deployWorkerVerticle(String clazzName, JsonObject config, Integer instances,
        Handler<AsyncResult<String>> handler) {
        container.deployWorkerVerticle(clazzName, config, instances, false, handler);
    }

    /**
     * Deploys instances number of clazzName Worker Verticles into the Vertx.IO container
     * @param clazzName Verticle to deploy as a Worker
     * @param config Configuration for Verticle
     * @param instances Number of instances to create
     */
    void deployWorkerVerticle(String clazzName, JsonObject config, Integer instances) {
        deployWorkerVerticle(clazzName, config, instances,
            new AsyncResultHandler<String>() {
                @Override
                public void handle(AsyncResult<String> deployResult) {
                    if (deployResult.succeeded()) {
                        logger.info(deployResult.result());
                    } else {
                        logger.error(deployResult.result());
                    }
                }
        });
    }
}
