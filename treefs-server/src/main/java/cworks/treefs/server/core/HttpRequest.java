package cworks.treefs.server.core;

import cworks.json.JsonArray;
import cworks.json.JsonObject;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpVersion;
import org.vertx.java.core.net.NetSocket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * HttpRequest is an implementation of Vertx's HttpRequest with some helper methods
 * to make it easier to deal with common tasks.
 *
 * @author comartin
 */
public class HttpRequest implements HttpServerRequest {

    private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
        float getQuality(String s) {
            if (s == null) {
                return 0;
            }

            String[] params = s.split(" *; *");
            for (int i = 1; i < params.length; i++) {
                String[] q = params[1].split(" *= *");
                if ("q".equals(q[0])) {
                    return Float.parseFloat(q[1]);
                }
            }
            return 1;
        }
        @Override
        public int compare(String o1, String o2) {
            float f1 = getQuality(o1);
            float f2 = getQuality(o2);
            if (f1 < f2) {
                return 1;
            }
            if (f1 > f2) {
                return -1;
            }
            return 0;
        }
    };

    // the original request
    protected final HttpServerRequest request;
    // the wrapped response
    protected final HttpResponse response;
    // the request context
    protected final Map<String, Object> context;
    // is this request secure
    protected final boolean secure;
    // we can override the setMethod
    protected String method;
    // -1 means no limit
    private long bodyLengthLimit = -1;
    // the body is protected so extensions can access the raw object instead of casted versions.
    protected Object body;
    // list of files that might be set in file-upload operation
    protected Map<String, FileUpload> files;
    // control flags
    private boolean expectMultiPartCalled = false;

    public HttpRequest(HttpServerRequest request,
                       HttpResponse response, boolean secure, Map<String, Object> context) {
        this.context = context;
        this.request = request;
        this.method = request.method();
        this.response = response;
        this.secure = secure;
    }
    
    public HttpRequest(HttpRequest request) {
        this(request.request, request.response, request.secure, request.context);
    }

    /**
     * Getting properties from the context in a general way
     * @param name of property
     * @return the property
     */
    public <R> R get(String name) {
        return (R) context.get(name);
    }

    /**
     * Get property from context in general way and provide defaultValue if key does not exist.
     * @param name of property
     * @param defaultValue
     * @return the property
     */
    public <R> R get(String name, R defaultValue) {
        if (context.containsKey(name)) {
            return get(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Put a value into the context
     * @param name of property
     * @param value of property to set
     * @return previous value if set
     */
    @SuppressWarnings("unchecked")
    public <R> R put(String name, R value) {
        return (R) context.put(name, value);
    }

    /**
     * Get header
     * @param name of header
     */
    public String getHeader(String name) {
        return headers().get(name);
    }

    /**
     * Get all headers for a header name
     * @param name of header
     */
    public List<String> getAllHeaders(String name) {
        return headers().getAll(name);
    }

    /**
     * Get header and return defaultValue if the header does not exist.
     * @param name of header
     * @param defaultValue
     */
    public String getHeader(String name, String defaultValue) {
        if (headers().contains(name)) {
            return getHeader(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * package-private method of setting HTTP method
     * @param newMethod, GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
     */
    void setMethod(String newMethod) {
        this.method = newMethod.toUpperCase();
    }

    /**
     * package-private method for setting body length limit
     * @param limit acceptable length in bytes
     */
    void setBodyLengthLimit(long limit) {
        bodyLengthLimit = limit;
    }

    /**
     * Maximum length of the body, -1 for unlimited
     */
    public long bodyLengthLimit() {
        return bodyLengthLimit;
    }

    /**
     * Return true if this request has a body by evaluating if the content-length or
     * transfer-encoding headers are set.
     * @return true if this request has a body
     */
    public boolean hasBody() {
        MultiMap headers = headers();
        return headers.contains("transfer-encoding") || headers.contains("content-length");
    }

    /**
     * Return the content-length of this request or -1 if header isn't present.
     */
    public long contentLength() {
        String contentLengthHeader = headers().get("content-length");
        if (contentLengthHeader != null) {
            return Long.parseLong(contentLengthHeader);
        } else {
            return -1;
        }
    }

    /**
     * The request body as a JsonObject or JsonArray
     * @return body
     */
    public <V> V body() {
        if (body != null) {
            if (body instanceof Map) {
                return (V) new JsonObject((Map) body);
            }
            if (body instanceof List) {
                return (V) new JsonArray((List) body);
            }
        }

        return (V) body;
    }

    /**
     * package-private setter for body
     */
    void setBody(Object body) {
        this.body = body;
    }

    /**
     * The uploaded files
     */
    public Map<String, FileUpload> files() {
        return files;
    }

    /**
     * Get one of the uploaded files
     * @param name of file
     */
    public FileUpload getFile(String name) {
        if (files == null) {
            return null;
        }

        return files.get(name);
    }

    /**
     * package-private setter for Map of FileUpload instances
     */
    void setFiles(Map<String, FileUpload> files) {
        this.files = files;
    }

    /**
     * Is this request using SSL?
     * @return true if SSL, false if not
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Split the parts of a Mime type, for example "application/json"
     * @return parts of Mime type
     */
    private static String[] splitMime(String mime) {
        int space = mime.indexOf(';');

        if (space != -1) {
            mime = mime.substring(0, space);
        }

        String[] parts = mime.split("/");

        if (parts.length < 2) {
            return new String[] {
                parts[0],
                "*"
            };
        }

        return parts;
    }

    /**
     * Check if this request accepts a given type and return type match when true otherwise null,
     * in which case the response should be 406 "Not Acceptable".  The type value must be a single
     * mime-type such as "application/json".
     *
     * @return the mime-type if accepted otherwise null
     */
    public String accepts(String... types) {
        String accept = getHeader("accept");
        // accept anything when accept is not present
        if (accept == null) {
            return types[0];
        }

        // parse
        String[] acceptTypes = accept.split(" *, *");
        // sort on quality
        Arrays.sort(acceptTypes, ACCEPT_X_COMPARATOR);

        for (String senderAccept : acceptTypes) {
            String[] sAccept = splitMime(senderAccept);
            for (String appAccept : types) {
                String[] aAccept = splitMime(appAccept);
                if ((sAccept[0].equals(aAccept[0]) ||
                    "*".equals(sAccept[0]) ||
                    "*".equals(aAccept[0])) &&
                        (sAccept[1].equals(aAccept[1]) ||
                         "*".equals(sAccept[1]) || "*".equals(aAccept[1]))) {
                    return senderAccept;
                }
            }
        }

        return null;
    }

    /**
     * Check if the request contains the "Content-Type" header and that it contains the given
     * mime-type.
     * For example: Content-Type: application/json
     * request.is("json")
     * request.is("application/json")
     * request.is("application/*") returns true
     * request.is("xml") returns false
     * @param  type content-type
     * @return true if content-type is of type given
     */
    public boolean is(String type) {
        String ct = getHeader("Content-Type");
        if (ct == null) {
            return false;
        }
        // get the content type only (exclude charset)
        ct = ct.split(";")[0];

        // if we received an incomplete CT
        if (type.indexOf('/') == -1) {
            // when the content is incomplete we assume */type, e.g.:
            // json -> */json
            type = "*/" + type;
        }

        // process wildcards
        if (type.contains("*")) {
            String[] parts = type.split("/");
            String[] ctParts = ct.split("/");
            if ("*".equals(parts[0]) && parts[1].equals(ctParts[1])) {
                return true;
            }

            if ("*".equals(parts[1]) && parts[0].equals(ctParts[0])) {
                return true;
            }

            return false;
        }

        return ct.contains(type);
    }

    /**
     * Return IP address of client.  If trust-proxy is true (default) then first look for
     * X-Forward-For-Header
     * @return
     */
    public String ip() {
        Boolean trustProxy = (Boolean) context.get("trust-proxy");
        if (trustProxy != null && trustProxy) {
            String xForwardFor = getHeader("x-forward-for");
            if (xForwardFor != null) {
                String[] ips = xForwardFor.split(" *, *");
                if (ips.length > 0) {
                    return ips[0];
                }
            }
        }

        return request.remoteAddress().getHostName();
    }

    /**
     * Get parameters
     * @param name
     * @return
     */
    public String getParameter(String name) {
        return params().get(name);
    }

    /**
     * Get parameters and return defaultValue if parameter does not exist
     * @param name
     * @param defaultValue
     * @return
     */
    public String getParameter(String name, String defaultValue) {
        String value = getParameter(name);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Get all parameters for a given name
     * @param name
     * @return
     */
    public List<String> getParameterList(String name) {
        return params().getAll(name);
    }

    /**
     * Get form attributes given a form element name
     * @param name
     * @return
     */
    public String getFormParameter(String name) {
        return request.formAttributes().get(name);
    }

    /**
     * Get form attribute given a form element name, return defaultValue if attribute does not exist
     * @param name
     * @param defaultValue
     * @return
     */
    public String getFormParameter(String name, String defaultValue) {
        String value = request.formAttributes().get(name);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Get form attributes list for a given name
     * @param name
     * @return
     */
    public List<String> getFormParameterList(String name) {
        return request.formAttributes().getAll(name);
    }

    /**
     * Return the real vertx request
     * @return
     */
    public HttpServerRequest vertxHttpServerRequest() {
        return request;
    }

    /**
     * Return the HTTP version
     * @return
     */
    @Override
    public HttpVersion version() {
        return request.version();
    }

    /**
     * Return the HTTP method
     * @return
     */
    @Override
    public String method() {
        if (method != null) {
            return method;
        }
        return request.method();
    }

    /**
     * Return the full URI
     * @return
     */
    @Override
    public String uri() {
        return request.uri();
    }

    /**
     * Return the path part of the URI
     * @return
     */
    @Override
    public String path() {
        return request.path();
    }

    /**
     * Return the query part of the URI
     * @return
     */
    @Override
    public String query() {
        return request.query();
    }

    /**
     * Return the response instance for this request
     * @return
     */
    @Override
    public HttpResponse response() {
        return response;
    }

    /**
     * A map of all headers in the request, If the request contains multiple headers with the same
     * key, the values will be concatenated together into a single header with the same key value,
     * with each value separated by a comma, as specified
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">here</a>.
     * The headers will be automatically lower-cased when they reach the server
     */
    @Override
    public MultiMap headers() {
        return request.headers();
    }

    /**
     * @return Returns a map of all the parameters in the request
     */
    @Override
    public MultiMap params() {
        return request.params();
    }

    /**
     * @return Return the remote (client side) address of the request
     */
    @Override
    public InetSocketAddress remoteAddress() {
        return request.remoteAddress();
    }

    /**
     * @return Return the local (server side) address of the server that handles the request
     */
    @Override
    public InetSocketAddress localAddress() {
        return request.localAddress();
    }

    /**
     * @return an array of the peer certificates.  Returns null if connection is not SSL.
     * @throws javax.net.ssl.SSLPeerUnverifiedException SSL peer's identity has not been verified.
     */
    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return request.peerCertificateChain();
    }

    /**
     * Get the absolute URI corresponding to the the HTTP request
     * @return the URI
     */
    @Override
    public URI absoluteURI() {
        return request.absoluteURI();
    }

    /**
     * Convenience method for receiving the entire request body in one piece. This saves the user
     * having to manually set a data and end handler and append the chunks of the body until the
     * whole body received.
     *
     * WARNING: Don't use this if your request body is large - you could potentially run out of RAM.
     *
     * @param bodyHandler This handler will be called after all the body has been received
     */
    @Override
    public HttpRequest bodyHandler(Handler<Buffer> bodyHandler) {
        request.bodyHandler(bodyHandler);
        return this;
    }

    /**
     * Get a net socket for the underlying connection of this request. USE THIS WITH CAUTION!
     * Writing to the socket directly if you don't know what you're doing can easily break the
     * HTTP protocol
     * @return the net socket
     */
    @Override
    public NetSocket netSocket() {
        return request.netSocket();
    }

    /**
     * Call this with true if you are expecting a multi-part form to be submitted in the request
     * This must be called before the body of the request has been received.
     * @param expect
     */
    @Override
    public HttpRequest expectMultiPart(boolean expect) {
        // if we expect
        if (expect) {
            // then only call it once
            if (!expectMultiPartCalled) {
                expectMultiPartCalled = true;
                request.expectMultiPart(expect);
            }
        } else {
            // if we don't expect reset even if we were called before
            expectMultiPartCalled = false;
            request.expectMultiPart(expect);
        }
        return this;
    }

    /**
     * Set the upload handler. The handler will get notified once a new file upload was received
     * and so allow to get notified by the upload in progress.
     */
    @Override
    public HttpRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        request.uploadHandler(uploadHandler);
        return this;
    }

    /**
     * Returns a map of all form attributes which was found in the request. Be aware that this
     * message should only get called after the endHandler was notified as the map will be filled
     * on-the-fly.
     *
     * {@link #expectMultiPart(boolean)} must be called first before trying to get
     * the formAttributes
     */
    @Override
    public MultiMap formAttributes() {
        return request.formAttributes();
    }

    /**
     * Set a data handler. As data is read, the handler will be called with the data.
     */
    @Override
    public HttpRequest dataHandler(Handler<Buffer> handler) {
        request.dataHandler(handler);
        return this;
    }

    /**
     * Pause the {@code ReadSupport}. While it's paused, no data will be sent to the
     * {@code dataHandler}
     */
    @Override
    public HttpServerRequest pause() {
        request.pause();
        return this;
    }

    /**
     * Resume reading. If the {@code ReadSupport} has been paused, reading will recommence on it.
     */
    @Override
    public HttpRequest resume() {
        request.resume();
        return this;
    }

    /**
     * Set an end handler. Once the stream has ended, and there is no more data to be read,
     * this handler will be called.
     */
    @Override
    public HttpRequest endHandler(Handler<Void> endHandler) {
        request.endHandler(endHandler);
        return this;
    }

    /**
     * Set a handler to deal with exceptions that occur while processing this request
     * @param handler
     * @return
     */
    @Override
    public HttpRequest exceptionHandler(Handler<Throwable> handler) {
        request.exceptionHandler(handler);
        return this;
    }

    @Override
    public String toString() {
        return this.request.method() + " " + this.request.path() + " " + this.request.query();
    }
}