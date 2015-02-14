package cworks.treefs.server.core;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpException extends Throwable {

    private static final long serialVersionUID = 1L;

    private final Number code;

    public HttpException(Number code) {
        this(code, HttpResponseStatus.valueOf(code.intValue()).reasonPhrase());
    }

    public HttpException(Number code, String message) {
        super(message);
        this.code = code;
    }

    public HttpException(Number code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public HttpException(Number code, String message, String cause) {
        super(message, new Throwable(cause));
        this.code = code;
    }

    public HttpException(String message) {
        super(message);
        this.code = 500;
    }

    public HttpException(Number code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Number getErrorCode() {
        return code;
    }
}
