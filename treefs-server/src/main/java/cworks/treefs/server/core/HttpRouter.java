package cworks.treefs.server.core;

import net.cworks.http.Http;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Route a request by path or regex.  All HTTP Verbs are available:
 * POST, GET, PUT, DELETE, OPTIONS, HEAD, TRACE, CONNECT, PATCH
 */
public class HttpRouter extends HttpService {

    private final List<PatternBinding> getBindings = new ArrayList<>();
    private final List<PatternBinding> putBindings = new ArrayList<>();
    private final List<PatternBinding> postBindings = new ArrayList<>();
    private final List<PatternBinding> deleteBindings = new ArrayList<>();
    private final List<PatternBinding> optionsBindings = new ArrayList<>();
    private final List<PatternBinding> headBindings = new ArrayList<>();
    private final List<PatternBinding> traceBindings = new ArrayList<>();
    private final List<PatternBinding> connectBindings = new ArrayList<>();
    private final List<PatternBinding> patchBindings = new ArrayList<>();

    private final Map<String, HttpService> paramProcessors = new HashMap<>();

    // @example
    //      new HttpRouter() {{
    //        get("/hello", new Handler<HttpRequest>() {
    //          public void handle(HttpRequest request) {
    //            request.response().end("Hello World!");
    //          }
    //        });
    //      }}
    public HttpRouter() {

    }

    @Override
    public HttpService init(Vertx vertx, Logger logger, String mount) {
        super.init(vertx, logger, mount);
        // since this call can happen after the bindings are in place we need to update all bindings to have a reference
        // to the vertx object
        for (PatternBinding binding : getBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : putBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : postBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : deleteBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : optionsBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : headBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : traceBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : connectBindings) {
            binding.service.init(vertx, logger, mount);
        }

        for (PatternBinding binding : patchBindings) {
            binding.service.init(vertx, logger, mount);
        }

        return this;
    }

    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {

        switch (request.method()) {
            case "GET":
                route(request, next, getBindings);
                break;
            case "PUT":
                route(request, next, putBindings);
                break;
            case "POST":
                route(request, next, postBindings);
                break;
            case "DELETE":
                route(request, next, deleteBindings);
                break;
            case "OPTIONS":
                route(request, next, optionsBindings);
                break;
            case "HEAD":
                route(request, next, headBindings);
                break;
            case "TRACE":
                route(request, next, traceBindings);
                break;
            case "PATCH":
                route(request, next, patchBindings);
                break;
            case "CONNECT":
                route(request, next, connectBindings);
                break;
        }
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter get(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, getBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter get(String pattern, final Handler<HttpRequest> handler) {
        return get(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter put(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, putBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter put(String pattern, final Handler<HttpRequest> handler) {
        return put(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter post(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, postBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter post(String pattern, final Handler<HttpRequest> handler) {
        return post(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter delete(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, deleteBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter delete(String pattern, final Handler<HttpRequest> handler) {
        return delete(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter options(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, optionsBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter options(String pattern, final Handler<HttpRequest> handler) {
        return options(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter head(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, headBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter head(String pattern, final Handler<HttpRequest> handler) {
        return head(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter trace(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, traceBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter trace(String pattern, final Handler<HttpRequest> handler) {
        return trace(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter connect(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, connectBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter connect(String pattern, final Handler<HttpRequest> handler) {
        return connect(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handlers The HttpService to call
     */
    public HttpRouter patch(String pattern, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addPattern(pattern, handler, patchBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter patch(String pattern, final Handler<HttpRequest> handler) {
        return patch(pattern, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter all(String pattern, HttpService... handler) {
        get(pattern, handler);
        put(pattern, handler);
        post(pattern, handler);
        delete(pattern, handler);
        options(pattern, handler);
        head(pattern, handler);
        trace(pattern, handler);
        connect(pattern, handler);
        patch(pattern, handler);
        return this;
    }

    /**
     * Specify a HttpService that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The HttpService to call
     */
    public HttpRouter all(String pattern, final Handler<HttpRequest> handler) {
        get(pattern, handler);
        put(pattern, handler);
        post(pattern, handler);
        delete(pattern, handler);
        options(pattern, handler);
        head(pattern, handler);
        trace(pattern, handler);
        connect(pattern, handler);
        patch(pattern, handler);
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP GET
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter get(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, getBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP GET
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter get(Pattern regex, final Handler<HttpRequest> handler) {
        return get(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PUT
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter put(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, putBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PUT
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter put(Pattern regex, final Handler<HttpRequest> handler) {
        return put(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP POST
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter post(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, postBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP POST
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter post(Pattern regex, final Handler<HttpRequest> handler) {
        return post(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP DELETE
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter delete(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, deleteBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP DELETE
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter delete(Pattern regex, final Handler<HttpRequest> handler) {
        return delete(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP OPTIONS
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter options(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, optionsBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP OPTIONS
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter options(Pattern regex, final Handler<HttpRequest> handler) {
        return options(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP HEAD
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter head(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, headBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP HEAD
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter head(Pattern regex, final Handler<HttpRequest> handler) {
        return head(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP TRACE
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter trace(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, traceBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP TRACE
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter trace(Pattern regex, final Handler<HttpRequest> handler) {
        return trace(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP CONNECT
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter connect(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, connectBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP CONNECT
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter connect(Pattern regex, final Handler<HttpRequest> handler) {
        return connect(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PATCH
     * @param regex A regular expression
     * @param handlers The HttpService to call
     */
    public HttpRouter patch(Pattern regex, HttpService... handlers) {
        for (HttpService handler : handlers) {
            addRegEx(regex, handler, patchBindings);
        }
        return this;
    }

    /**
     * Specify a HttpService that will be called for a matching HTTP PATCH
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter patch(Pattern regex, final Handler<HttpRequest> handler) {
        return patch(regex, new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a HttpService that will be called for all HTTP methods
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter all(Pattern regex, HttpService... handler) {
        get(regex, handler);
        put(regex, handler);
        post(regex, handler);
        delete(regex, handler);
        options(regex, handler);
        head(regex, handler);
        trace(regex, handler);
        connect(regex, handler);
        patch(regex, handler);
        return this;
    }

    /**
     * Specify a HttpService that will be called for all HTTP methods
     * @param regex A regular expression
     * @param handler The HttpService to call
     */
    public HttpRouter all(Pattern regex, final Handler<HttpRequest> handler) {
        get(regex, handler);
        put(regex, handler);
        post(regex, handler);
        delete(regex, handler);
        options(regex, handler);
        head(regex, handler);
        trace(regex, handler);
        connect(regex, handler);
        patch(regex, handler);
        return this;
    }

    public HttpRouter param(final String paramName, final HttpService handler) {
        // also pass the vertx object to the routes
        handler.init(vertx, logger, mount);
        paramProcessors.put(paramName, handler);
        return this;
    }

    public HttpRouter param(final String paramName, final Pattern regex) {
        return param(paramName, new HttpService() {
            @Override
            public void handle(final HttpRequest request, final Handler<HttpService> next) {
                if (!regex.matcher(request.params().get(paramName)).matches()) {
                    // Bad Request
                    next.handle(new ErrorHandler(400));
                    return;
                }

                next.handle(null);
            }
        });
    }

    /**
     * Search for any :<token name> tokens in the String and replace with named capture groups
     * @param input
     * @param handler
     * @param bindings
     */
    private void addPattern(String input, HttpService handler, List<PatternBinding> bindings) {
        // We need to search for any :<token name> tokens in the String and replace them with named capture groups
        Matcher m =  Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(input);
        StringBuffer sb = new StringBuffer();
        Set<String> groups = new HashSet<>();
        while (m.find()) {
            String group = m.group().substring(1);
            if (groups.contains(group)) {
                throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
            }
            m.appendReplacement(sb, "(?<$1>[^\\/]+)");
            groups.add(group);
        }
        m.appendTail(sb);
        // ignore tailing slash if not part of the input, not really REST but common on other frameworks
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append("\\/?$");
        }
        String regex = sb.toString();
        PatternBinding binding = new PatternBinding(Pattern.compile(regex), groups, handler);
        // also pass the vertx object to the routes
        handler.init(vertx, logger, mount);
        bindings.add(binding);
    }

    List<PatternBinding> allGetBindings() {
        return this.getBindings;
    }

    private void addRegEx(Pattern regex, HttpService handler, List<PatternBinding> bindings) {
        PatternBinding binding = new PatternBinding(regex, null, handler);
        // also pass the vertx object to the routes
        handler.init(vertx, logger, mount);
        bindings.add(binding);
    }

    private void route(final HttpRequest request, final Handler<HttpService> next, final List<PatternBinding> bindings) {
        for (final PatternBinding binding: bindings) {
            // TODO: use normalized path?
            final Matcher m = binding.pattern.matcher(request.path());
            if (m.matches()) {
                final MultiMap params = request.params();

                if (binding.paramNames != null) {
                    // Named params
                    // TODO need to look at paramMidddleware handler
                    new AsyncIterator<String>(binding.paramNames) {
                        @Override
                        public void handle(String param) {
                            if (hasNext()) {
                                params.add(param, m.group(param));
                                HttpService paramMiddleware = paramProcessors.get(param);
                                if (paramMiddleware != null) {
                                    paramMiddleware.handle(request, new Handler<HttpService>() {
                                        @Override
                                        public void handle(HttpService err) {
                                            if (err == null) {
                                                next();
                                            } else {
                                                next.handle(err);
                                            }
                                        }
                                    });
                                } else {
                                    next();
                                }
                            } else {
                                binding.service.handle(request, next);
                            }
                        }
                    };
                } else {
                    // Un-named params
                    for (int i = 0; i < m.groupCount(); i++) {
                        params.add("param" + i, m.group(i + 1));
                    }
                    binding.service.handle(request, next);
                }
                return;
            }
        }

        next.handle(null);
    }

    static class PatternBinding {
        final Pattern pattern;
        final HttpService service;
        final Set<String> paramNames;

        private PatternBinding(Pattern pattern, Set<String> paramNames, HttpService service) {
            this.pattern = pattern;
            this.paramNames = paramNames;
            this.service = service;
        }
    }

    private static HttpService wrap(final Object o, final Method m, final boolean simple, final String[] consumes, final String[] produces) {
        return new HttpService() {
            @Override
            public void handle(HttpRequest request, Handler<HttpService> next) {
                try {
                    // we only know how to process certain media types
                    if (consumes != null) {
                        boolean canConsume = false;
                        for (String c : consumes) {
                            if (request.is(c)) {
                                canConsume = true;
                                break;
                            }
                        }

                        if (!canConsume) {
                            // 415 Unsupported Media Type (we don't know how to handle this media)
                            next.handle(new ErrorHandler(415));
                            return;
                        }
                    }

                    // the object was marked with a specific content type
                    if (produces != null) {
                        String bestContentType = request.accepts(produces);

                        // the client does not know how to handle our content type, return 406
                        if (bestContentType == null) {
                            next.handle(new ErrorHandler(406));
                            return;
                        }

                        // mark the response with the correct content type (which allows service to know it later on)
                        request.response().setContentType(bestContentType);
                    }

                    if (simple) {
                        m.invoke(o, request);
                    } else {
                        m.invoke(o, request, next);
                    }
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    next.handle(new ErrorHandler(ex));
                }
            }
        };
    }

}
