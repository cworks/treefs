package cworks.treefs.server.core;

import cworks.json.JsonArray;
import cworks.json.JsonObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates an error response in JSON format is client accepts JSON otherwise
 * in plain text form.
 *
 * If fullStack is passed as true to constructor then the full stacktrace will
 * be included in the response.
 *
 * @author comartin
 */
public class ErrorHandler extends HttpService {

    /**
     * Enable / disable full stack trace reporting of exceptions
     */
    private final boolean fullStack;

    /**
     * Create this ErrorHandler to allow printing of stack traces in the response if fullStack
     * is true otherwise only the error message is used.
     * @param fullStack
     */
    public ErrorHandler(boolean fullStack) {
        this.fullStack = fullStack;
    }

    /**
     * We are using this HttpService as an ErrorHandler so we return true here
     * @return
     */
    @Override
    public boolean isErrorHandler() {
        return true;
    }

    /**
     * Handle the request by setting an error in the response
     * @param request
     * @param next
     */
    @Override
    public void handle(HttpServiceRequest request, Handler<Object> next) {
        HttpServiceResponse response = request.response();

        //
        // if we've been asked to handle an error condition yet the status code
        // on the response is less than 400 (i.e. not an error) then we fix this situation
        //
        if(response.getStatusCode() < 400) {
            response.setStatusCode(_errorCode(request.get("error")));
        }

        //
        // if the erronious request does not have an error property consistent
        // with the response then we fix that
        //
        if(request.get("error") == null) {
            request.put("error", response.getStatusCode());
        }

        String errorMessage = _message(request.get("error"));
        int statusCode = response.getStatusCode();
        response.setStatusMessage(HttpResponseStatus.valueOf(statusCode).reasonPhrase());
        List<String> stackTrace = _stackTrace(request.get("error"));
        String accept = request.getHeader("accept", "text/plain");

        //
        // if client accepts JSON then we respond with JSON,
        // otherwise respond in plain text
        //
        if (accept.contains("json")) {
            JsonObject errorObject = new JsonObject();
            errorObject.setObject("error", new JsonObject()
                .setNumber("statusCode", statusCode)
                .setString("message", errorMessage));

            if(!stackTrace.isEmpty()) {
                JsonArray stack = new JsonArray();
                for (String t : stackTrace) {
                    stack.addString(t);
                }
                errorObject.setArray("stack", stack);
            }

            response.setContentType("application/json", "UTF-8");
            response.end(errorObject.asString());
        } else {
            response.setContentType("text/plain");
            StringBuilder sb = new StringBuilder();
            sb.append("statusCode: ");
            sb.append(statusCode);
            sb.append(", message: ");
            sb.append(errorMessage);
            if(stackTrace.size() > 0) {
                sb.append(", stack: ");
            }
            for (String trace : stackTrace) {
                sb.append("\tat ");
                sb.append(trace);
                sb.append("\n");
            }
            response.end(sb.toString());
        }
    }

    /**
     * Carve out the message from the error Object.
     *
     * @param error
     * @return
     */
    private String _message(Object error) {
        if(error instanceof Throwable) {
            String message = ((Throwable) error).getMessage();
            if(message == null) {
                message = "";
            }
            if(fullStack) {
                return error.getClass().getName() + ": " + message;
            } else {
                return message;
            }
        } else if (error instanceof String) {
            return (String) error;
        } else if (error instanceof Integer) {
            return HttpResponseStatus.valueOf((Integer)error).reasonPhrase();
        } else {
            return error.toString();
        }
    }

    /**
     * Carve out a single error code from an error Object
     *
     * @param error
     * @return
     */
    private int _errorCode(Object error) {
        if (error instanceof Number) {
            return ((Number)error).intValue();
        } else if (error instanceof HttpServiceException) {
            return ((HttpServiceException)error).getErrorCode().intValue();
        } else {
            return 500;
        }
    }

    /**
     * Transform stack trace to a List so that it can be rendered
     * @param error
     * @return list of strings in stacktrace or empty list if no stacktrace exists
     */
    private List<String> _stackTrace(Object error) {
        if(fullStack && error instanceof Throwable) {
            List<String> stackTrace = new ArrayList<>();
            for (StackTraceElement t : ((Throwable) error).getStackTrace()) {
                stackTrace.add(t.toString());
            }
            return stackTrace;
        } else {
            return Collections.emptyList();
        }
    }

}

