package cworks.treefs.server.core;

import cworks.json.JsonArray;
import cworks.json.JsonElement;
import cworks.json.JsonObject;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.core.streams.ReadStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * HttpResponse is an implementation of Vertx's HttpServerResponse with some helper
 * methods to make it easier to deal with common tasks.
 *
 * @author comartin
 */
public class HttpResponse implements HttpServerResponse {

    /**
     * response from the original request
     */
    protected final HttpServerResponse response;

    /**
     * the vertx context
     */
    protected final Map<String, Object> context;

    /**
     * response cookies
     */
    protected Set<Cookie> cookies;

    /**
     * extra handlers
     */
    protected List<Handler<Void>> headersHandler;

    /**
     * flag that controls when header handlers get invoked
     */
    protected boolean headersHandlerTriggered;

    /*
     * list of endHandlers that gets invoked at end of response
     */
    protected List<Handler<Void>> endHandler;

    /**
     * writer filter
     */
    protected WriterFilter filter;

    /**
     * does response have a body
     */
    protected boolean hasBody;

    /**
     * Create a HttpResponse from vertx HttpServerResponse
     * @param response vertx response
     * @param context vertx context variables
     */
    public HttpResponse(HttpServerResponse response, Map<String, Object> context) {
        this.response = response;
        this.context = context;
    }

    /**
     * Sets contentType header
     * @param contentType mime type of content-header
     * @return this HttpResponse
     */
    public HttpResponse setContentType(String contentType) {
        setContentType(contentType, MimeType.getCharset("UTF-8"));
        return this;
    }

    /**
     * Sets contentType header with a specific character set encoding
     * @param contentType mime type of content-header
     * @param contentEncoding charset encoding
     * @return response instance
     */
    public HttpResponse setContentType(String contentType, String contentEncoding) {
        if (contentEncoding == null) {
            putHeader("content-type", contentType);
        } else {
            putHeader("content-type", contentType + ";charset=" + contentEncoding);
        }
        return this;
    }

    /**
     * Allow getting headers in a generified way.
     *
     * @param name The key to get
     * @param <R> The type of the return
     * @return The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R getHeader(String name) {
        return (R) headers().get(name);
    }

    /**
     * Allow getting headers in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @param <R> The type of the return
     * @return The found object
     */
    public <R> R getHeader(String name, R defaultValue) {
        if (headers().contains(name)) {
            return getHeader(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Redirect response to url
     * @param url redirect url
     */
    public void redirect(String url) {
        redirect(302, url);
    }

    /**
     * Redirect response to url with a specific httpStatus
     * @param status httpStatus to send with redirect
     * @param url redirect url
     */
    public void redirect(int status, String url) {
        setStatusCode(status);
        setStatusMessage(HttpResponseStatus.valueOf(status).reasonPhrase());
        putHeader("location", url);
        end();
    }

    public HttpResponse addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new TreeSet<>();
        }
        cookies.add(cookie);
        return this;
    }

    public void headersHandler(Handler<Void> handler) {
        if (!headersHandlerTriggered) {
            if (headersHandler == null) {
                headersHandler = new ArrayList<>();
            }
            headersHandler.add(handler);
        }
    }

    public void endHandler(Handler<Void> handler) {
        if (endHandler == null) {
            endHandler = new ArrayList<>();
        }
        endHandler.add(handler);
    }

    private void triggerHeadersHandlers() {
        if (!headersHandlerTriggered) {
            headersHandlerTriggered = true;
            // if there are handlers call them
            if (headersHandler != null) {
                for (Handler<Void> handler : headersHandler) {
                    handler.handle(null);
                }
            }
            // convert the cookies set to the right header
            if (cookies != null) {
                response.putHeader("set-cookie", ServerCookieEncoder.encode(cookies));
            }

            // if there is a filter then set the right header
            if (filter != null) {
                // verify if the filter can filter this content
                if (filter.canFilter(response.headers().get("content-type"))) {
                    response.putHeader("content-encoding", filter.encoding());
                } else {
                    // disable the filter
                    filter = null;
                }
            }
            // if there is no content delete content-type, content-encoding
            if (!hasBody) {
                response.headers().remove("content-encoding");
                response.headers().remove("content-type");
            }
        }
    }

    private void triggerEndHandlers() {
        if (endHandler != null) {
            for (Handler<Void> handler : endHandler) {
                handler.handle(null);
            }
        }
    }

    // interface implementation

    @Override
    public int getStatusCode() {
        return response.getStatusCode();
    }

    @Override
    public HttpResponse setStatusCode(int statusCode) {
        response.setStatusCode(statusCode);
        return this;
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusMessage();
    }

    @Override
    public HttpResponse setStatusMessage(String statusMessage) {
        response.setStatusMessage(statusMessage);
        return this;
    }

    @Override
    public HttpResponse setChunked(boolean chunked) {
        response.setChunked(chunked);
        return this;
    }

    @Override
    public boolean isChunked() {
        return response.isChunked();
    }

    @Override
    public MultiMap headers() {
        return response.headers();
    }

    @Override
    public HttpResponse putHeader(String name, String value) {
        response.putHeader(name, value);
        return this;
    }

    /**
     * @param name header name
     * @param value header value
     * @return response instance
     */
    @Override
    public HttpResponse putHeader(CharSequence name, CharSequence value) {
        response.putHeader(name, value);
        return this;
    }

    @Override
    public HttpResponse putHeader(String name, Iterable<String> values) {
        response.putHeader(name, values);
        return this;
    }

    /**
     * @param name header name
     * @param values header values
     * @return response instance
     */
    @Override
    public HttpResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        response.putHeader(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        return response.trailers();
    }

    @Override
    public HttpResponse putTrailer(String name, String value) {
        response.putTrailer(name, value);
        return this;
    }

    /**
     * @param name trailer name
     * @param value trailer value
     * @return response instance
     */
    @Override
    public HttpResponse putTrailer(CharSequence name, CharSequence value) {
        response.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpResponse putTrailer(String name, Iterable<String> values) {
        response.putTrailer(name, values);
        return this;
    }

    /**
     * @param name trailer name
     * @param value trailer value
     * @return response instance
     */
    @Override
    public HttpResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        response.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpResponse closeHandler(Handler<Void> handler) {
        response.closeHandler(handler);
        return this;
    }

    @Override
    public HttpResponse write(Buffer chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.write(chunk);
        } else {
            filter.write(chunk);
        }
        return this;
    }

    @Override
    public HttpResponse setWriteQueueMaxSize(int maxSize) {
        response.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return response.writeQueueFull();
    }

    @Override
    public HttpResponse drainHandler(Handler<Void> handler) {
        response.drainHandler(handler);
        return this;
    }

    @Override
    public HttpResponse write(String chunk, String enc) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.write(chunk, enc);
        } else {
            filter.write(chunk, enc);
        }
        return this;
    }

    @Override
    public HttpResponse write(String chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.write(chunk);
        } else {
            filter.write(chunk);
        }
        return this;
    }

    @Override
    public void end(String chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.end(chunk);
        } else {
            response.end(filter.end(chunk));
        }
        triggerEndHandlers();
    }

    @Override
    public void end(String chunk, String enc) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.end(chunk, enc);
        } else {
            response.end(filter.end(chunk, enc));
        }
        triggerEndHandlers();
    }

    public void end(Object data) {
        end(data.toString());
    }

    public void end(Object data, String enc) {
        end(data.toString(), enc);
    }

    @Override
    public void end(Buffer chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        response.end(filter == null ? chunk : filter.end(chunk));
        triggerEndHandlers();
    }

    public void end(ReadStream<?> stream) {
        // TODO: filter stream?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();
        Pump.createPump(stream, response).start();
        stream.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                response.end();
                triggerEndHandlers();
            }
        });
    }

    /**
     * End the response by writing JSON into body and setting application/json contentType header
     * @param json JSON element to serialize
     */
    public void end(JsonElement json) {
        if (json.isArray()) {
            JsonArray jsonArray = json.asArray();
            setContentType("application/json", "UTF-8");
            end(jsonArray.asString());
        } else if (json.isObject()) {
            JsonObject jsonObject = json.asObject();
            setContentType("application/json", "UTF-8");
            end(jsonObject.asString());
        }
    }

    @Override
    public void end() {
        triggerHeadersHandlers();
        response.end();
        triggerEndHandlers();
    }

    /**
     * Tell vertx to stream a file as specified by {@code filename} directly from disk to the
     * outgoing connection, bypassing memory altogether (where supported by the underlying OS)
     *
     * @param filename the file to send from disk to client
     * @return this HttpResponse
     */
    @Override
    public HttpResponse sendFile(String filename) {

        return sendFile(filename, null, null);
    }

    /**
     * Tell vertx to stream a file as specified by {@code filename} directly from disk to the
     * outgoing connection, bypassing memory altogether (where supported by the underlying OS).
     *
     * If for any reason the filename cannot be served then send the file pointed
     * to by notFoundFile parameter
     *
     * @param filename the file to send from disk to client
     * @param notFoundFile a file to serve in the event something goes wrong with serving filename
     * @return this HttpResponse
     */
    @Override
    public HttpResponse sendFile(String filename, String notFoundFile) {

        return sendFile(filename, notFoundFile, null);
    }

    /**
     * Tell vertx to stream a file as specified by {@code filename} directly from disk to the
     * outgoing connection, bypassing memory altogether (where supported by the underlying OS)
     *
     * resultHandler will be called when the send has completed or a failure has occurred
     *
     * @param filename the file to send from disk to client
     * @param resultHandler called when send completes or fails
     * @return this HttpResponse
     */
    @Override
    public HttpResponse sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {

        return sendFile(filename, null, resultHandler);
    }

    /**
     * Tell vertx to stream a file as specified by {@code filename} directly from disk to the
     * outgoing connection, bypassing memory altogether (where supported by the underlying OS).
     *
     * If for any reason the filename cannot be served then send the file pointed
     * to by notFoundFile parameter.
     *
     * resultHandler will be called when the send has completed or a failure has occurred
     *
     * @param filename the file to send from disk to client
     * @param notFoundFile a file to serve in the event something goes wrong with serving filename
     * @param resultHandler called when send completes or fails
     * @return this HttpResponse
     */
    @Override
    public HttpResponse sendFile(String filename,
        String notFoundFile, Handler<AsyncResult<Void>> resultHandler) {

        // TODO: filter file?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();

        if(filename != null && notFoundFile != null && resultHandler != null) {
            response.sendFile(filename, notFoundFile, resultHandler);
        } else if(filename != null && resultHandler != null) {
            response.sendFile(filename, resultHandler);
        } else if(filename != null && notFoundFile != null) {
            response.sendFile(filename, notFoundFile);
        } else if(filename != null) {
            response.sendFile(filename);
        }

        return this;
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public HttpResponse exceptionHandler(Handler<Throwable> handler) {
        response.exceptionHandler(handler);
        return this;
    }

    /**
     * WriteFilter used to filter response
     * @param filter filter the response
     */
    void setFilter(WriterFilter filter) {
        this.filter = filter;
    }

}
