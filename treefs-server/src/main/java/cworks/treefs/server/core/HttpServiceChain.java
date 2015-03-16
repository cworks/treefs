package cworks.treefs.server.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.ArrayList;
import java.util.List;

import static cworks.treefs.common.ObjectUtils.isNull;

public class HttpServiceChain implements Handler<HttpRequest> {

    /**
     * Ordered list of mounted HttpServices in a chain
     */
    private final List<MountedHttpService> services;

    /**
     * Dedicated ErrorHandler for this chain 
     */
    private final HttpService errorHandler;

    /**
     * Request to run down this chain
     */
    private HttpRequest request;
    
    /**
     * Current HttpService being used
     */
    private int currentService = -1;
    
    public HttpServiceChain(List<MountedHttpService> services, HttpService errorHandler) {
        this.services = services;
        this.errorHandler = errorHandler;
    }
    
    @Override
    public void handle(HttpRequest request) {
        boolean found = false;
        for(MountedHttpService mounted : services) {
            if(request.path().startsWith(mounted.mount())) {
                found = true;
                HttpService service = mounted.httpService();
                try {
                    // handle is called on event-loop
                    service.handle(request, new Handler<HttpService>() {
                        @Override
                        public void handle(HttpService event) {
                            
                        }
                    });
                } catch(Throwable ex) {
                    handle(ex);
                }
            }
        }

        if(!found) {
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

    }

    public void handle(Throwable throwable) {


    }
    
}
