package cworks.treefs.server.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpModule is the top-level integration with Vertx.  The idea is a Module can encapsulate
 * both Vertx and a set of HttpService(s) that "compose" this Module.  High-level design point
 * is all HttpService(s) are mounted to same root, this allows for Module organization and for
 * poormans namespacing.
 *
 * @author comartin
 */
public class HttpModule {

    /**
     * Vert.x instance
     */
    private final Vertx vertx;

    /**
     * Internal logger
     */
    private final Logger logger;

    /**
     * Mount for this HttpModule
     */
    private final String mount;

    /**
     * Default context used by all requests
     *
     * example context data:
     * title: "HttpModule",
     * x-powered-by: true,
     * trust-proxy: true
     */
    protected final Map<String, Object> defaultContext = new HashMap<>();

    /**
     * Create a HttpModule instance.  This constructor should be called from a verticle and pass a valid Vertx instance.
     * This Vertx instance will be shared with all registered HttpService.  The reason behind this is to allow
     * HttpServices to use Vertx features such as file-system and timers.
     *
     * example use:
     *
     * public class MyVerticle extends Verticle {
     *     public void start() {
     *         final HttpModule module = new HttpModule(this);
     *     }
     * }
     *
     * @param verticle
     */
    public HttpModule(Verticle verticle) {
        this(verticle.getVertx(), verticle.getContainer().logger());
    }

    public HttpModule(Verticle verticle, String mount) {
        this(verticle.getVertx(), verticle.getContainer().logger(), mount);
    }

    /**
     * Creates a HttpModule instance.  This constructor should be called from a Verticle and pass a valid Vertx instance and
     * Logger.  This instance will be shared with all registered HttpService.  The reason behind this is to allow
     * HttpService to use Vertx features such as file-system and times.
     *
     * example use:
     * public class MyVerticle extends Verticle {
     *     public void start() {
     *         final HttpModule module = new HttpModule(getVertx(), getContainer().logger());
     *     }
     * }
     * @param vertx
     * @param logger
     */
    public HttpModule(Vertx vertx, Logger logger) {
        this(vertx, logger, "/");
    }

    /**
     * Creates a HttpModule instance.  This constructor should be called from a Verticle and pass a
     * valid Vertx instance, Logger and mount URI which will be used to mount all HttpServices to
     * this HttpModule.  This instance will be shared with all registered HttpService.  The reason
     * behind this is to allow HttpService to use Vertx features such as file-system and times.
     *
     * example use:
     * public class MyVerticle extends Verticle {
     *     public void start() {
     *         final HttpModule module =
     *          new HttpModule(getVertx(), getContainer().logger(), "myMount");
     *     }
     * }
     * @param vertx
     * @param logger
     */
    public HttpModule(Vertx vertx, Logger logger, String mount) {
        this.vertx = vertx;
        this.logger = logger;
        this.mount = mount;
        defaultContext.put("title", "HttpModule");
        defaultContext.put("x-powered-by", true);
        defaultContext.put("trust-proxy", true);
    }

    /**
     * Ordered list of mounted HttpServices in a chain
     */
    private final List<MountedHttpService> services = new ArrayList<>();

    /**
     * A special HttpService for dealing with jacked up stuff
     */
    private HttpService errorHandler;

    /**
     * Add a HttpService to this chain.  If the HttpService is an error handler then it is handled differently
     * and if multiple error handlers are used then only the last one is kept.
     *
     * If the route does not match to a HttpService then the HttpService is skipped.
     *
     * @param route
     * @param httpServices
     * @return
     */
    public HttpModule use(String route, HttpService... httpServices) {
        for(HttpService m : httpServices) {
            /*
             * When the type of HttpService is error handler then the route is ignored and
             * the HttpService is extracted from the execution chain into a special placeholder
             * for error handling
             */
            if(m.isErrorHandler()) {
                errorHandler = m;
            } else {
                services.add(new MountedHttpService(route, m));
            }

            // initialize the HttpService with the current Vert.x and Logger
            m.init(vertx, logger, route);
        }
        // for method chaining
        return this;
    }

    /**
     * Add a HttpService to the top of the chain with a prefix route of '/'
     * @param httpServices
     * @return
     */
    public HttpModule use(HttpService... httpServices) {
        return use(mount, httpServices);
    }

    /**
     * Add a vertx Handler to a route.  This behavior is similar to the HttpService with the exception this
     * will be a terminal point in the chain.  If HttpServices are added after the Handler then they will
     * not be executed.
     *
     * @param route
     * @param handler
     * @return
     */
    public HttpModule use(String route, final Handler<HttpRequest> handler) {
        services.add(new MountedHttpService(route, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        }));
        // for method chaining
        return this;
    }

    /**
     * Add a Vertx Handler to the top of the chain with a prefix route of '/'
     * @param handler
     * @return
     */
    public HttpModule use(Handler<HttpRequest> handler) {
        return use(mount, handler);
    }

    /**
     * Module setter that can be used to share global properties with requests.  Properties will be
     * available as request.get("myProperty").
     *
     * @param key
     * @param value Any non-null value, nulls are not saved and if null value is passed then the property is removed
     * @return
     */
    public HttpModule set(String key, Object value) {
        if(value == null) {
            defaultContext.remove(key);
        } else {
            defaultContext.put(key, value);
        }
        // for method chaining
        return this;
    }

    /**
     * Starts this Module listenting on a given port bound to all available interfaces
     * @param port
     * @return
     */
    public HttpModule listen(int port) {
        return listen(port, "0.0.0.0", null);
    }

    /**
     * Starts this Module listenting on a given port bound to all available interfaces.  The handler argument
     * is for asynchronous event handlers of the listen operation.
     *
     * @param port
     * @param handler
     * @return
     */
    public HttpModule listen(int port, Handler<Boolean> handler) {
        return listen(port, "0.0.0.0", handler);
    }

    /**
     * Starts this Module listenting on a given port bound to given address
     * @param port
     * @param address
     * @return
     */
    public HttpModule listen(int port, String address) {
        return listen(port, address, null);
    }

    /**
     * Starts this Module listening on a given port bound to a given address.  The handler argument
     * is for asynchronous event handlers of the listen operation.
     *
     * @param port
     * @param address
     * @param handler
     * @return
     */
    public HttpModule listen(int port, String address, final Handler<Boolean> handler) {
        HttpServer server = vertx.createHttpServer();

        listen(server);
        if(handler != null) {
            server.listen(port, address, new Handler<AsyncResult<HttpServer>>() {
                @Override
                public void handle(AsyncResult<HttpServer> listen) {
                    handler.handle(listen.succeeded());
                }
            });
        } else {
            server.listen(port, address);
        }
        // for method chaining
        return this;
    }

    /**
     * Starts this Module listening on an already created Server instance
     * @param server
     * @return
     */
    public HttpModule listen(final HttpServer server) {
        // is this server HTTPS?
        final boolean secure = server.isSSL();

        // http request handler that deals with the http request-response cycle
        server.requestHandler(req -> {
            // decorate http request with httpService stuff
            final HttpRequest request = wrapRequest(req, secure);
            // add x-powered-by header is enabled
            Boolean poweredBy = request.get("x-powered-by");
            if (poweredBy != null && poweredBy) {
                request.response().putHeader("x-powered-by", "TreeFs");
            }

            Handler<HttpService> chainHandler = new Handler<HttpService>() {
                int currentService = -1;
                @Override
                public void handle(HttpService service) {
                    if(!(service instanceof ErrorHandler)) {
                        currentService++;
                        if (currentService < services.size()) {
                            MountedHttpService mounted = services.get(currentService);
                            if(request.path().startsWith(mounted.mount())) {
                                try {
                                    mounted.handle(request, this);
                                } catch(Throwable ex) {
                                    handle(new ErrorHandler(ex));
                                }
                            } else {
                                // the HttpService was not mounted on this uri, so skip to the next entry
                                handle(null);
                            }
                        } else {
                            // reached the end and no handler was able to answer the request
                            HttpServerResponse response = request.response();
                            response.setStatusCode(404);
                            response.setStatusMessage(HttpResponseStatus.valueOf(404).reasonPhrase());
                            if (errorHandler != null) {
                                errorHandler.handle(request, null);
                            } else {
                                response.end(HttpResponseStatus.valueOf(404).reasonPhrase());
                            }
                        }
                    } else {
                        // else we got an error
                        // TODO look at prior implementation
                    }
                }
            };
            
            // start chain
            chainHandler.handle(null);

        });

        return this;
    }

    /**
     * Internal method that wraps a Vertx request with a HttpRequest and pairs a
     * HttpResponse with a HttpRequest
     * @param request
     * @param secure - set to true if using SSL
     * @return
     */
    private HttpRequest wrapRequest(HttpServerRequest request, boolean secure) {
        // the context map is shared with all HttpServices
        final Map<String, Object> context = new Context(defaultContext);
        HttpResponse response = new HttpResponse(request.response(), context);
        return new HttpRequest(request, response, secure, context);
    }
}
