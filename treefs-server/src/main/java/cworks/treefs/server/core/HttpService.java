package cworks.treefs.server.core;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

/**
 * Abstract HttpService class that needs to be implemented when creating a new HttpService.  This class provides
 * access to the Vertx instance and by default is not marked as an error handling HttpService (i.e. is doesn't
 * deal with errors unless configured to do so).  So if you need to create a new error handler HttpService the
 * isErrorHandler method should be overridden to return true.
 *
 * @author comartin
 */
public abstract class HttpService {

    /**
     * Local Vertx instance for use within the HttpService.
     * This is useful for all the asynchronous features of Vertx
     */
    protected Vertx vertx;

    /**
     * Vertx Logger
     */
    protected Logger logger;

    /**
     * The configured mount point for this HttpService
     */
    protected String mount;

    /**
     * Initializes this HttpService.  This method is called from Zoke once a HttpService is added to the chain.
     *
     * @param vertx the local Vertx instance
     * @param logger the current Logger
     * @param mount the configured mount path
     * @return
     */
    public HttpService init(Vertx vertx, Logger logger, String mount) {
        this.vertx = vertx;
        this.logger = logger;
        this.mount = mount;

        return this;
    }

    /**
     * If there is a need to have this HttpService handle errors then this method should return true indicating
     * that this instance will...you guessed it handle errors.
     * @return
     */
    public boolean isErrorHandler() {
        return false;
    }

    /**
     * Handle a request that is inside the chain.  The next argument is a callback to inform the
     * next HttpService in the chain to handle the request.  If a NON-NULL value is set then an
     * internal error is always raised, when a null is set then the next HttpService in the chain
     * will execute.
     *
     * @param request
     * @param next
     */
    public abstract void handle(final HttpRequest request, final Handler<Object> next);

}
