package net.cworks.treefs.server.core;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpServiceException extends Throwable {

    private static final long serialVersionUID = 1L;

    private final Number code;

    public HttpServiceException(Number code) {
        this(code, HttpResponseStatus.valueOf(code.intValue()).reasonPhrase());
    }

    public HttpServiceException(Number code, String message) {
        super(message);
        this.code = code;
    }

    public HttpServiceException(Number code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public HttpServiceException(Number code, String message, String cause) {
        super(message, new Throwable(cause));
        this.code = code;
    }

    public HttpServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public HttpServiceException(Number code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Number getErrorCode() {
        return code;
    }
}
