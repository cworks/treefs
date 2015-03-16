package cworks.treefs.server.core;

import org.vertx.java.core.Handler;

/**
 * This class needs to be kept package-private due to core being the only one
 * needing this class.
 * 
 * This class wraps an HttpService and the mount point that its attached on
 */
class MountedHttpService extends HttpService {
    
    /**
     * URL mound point 
     */
    private final String mount; // url attach point

    /**
     * Http MicroService
     */
    private final HttpService httpService;

    /**
     * Create a new MountedHttpService instance
     * @param mount the mount path
     * @param httpService the HttpService to use on the path
     */
    MountedHttpService(String mount, HttpService httpService) {
        this.mount = mount;
        this.httpService = httpService;
    }
    
    String mount() {
        return this.mount;
    }
    
    HttpService httpService() {
        return this.httpService;
    }

    @Override
    public void handle(final HttpRequest request, final Handler<HttpService> next) {

        // 1) (Yes) handle request and write response
        // 2) (Yes) handle request and delegate write response
        // 3) (Not Allowed) delegate request and write response
        // 4) (Allowed?) delegate request and delegate write response

        request.response().exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                System.out.println("response.exceptionHandler called: " + event.getMessage());
                next.handle(new ErrorHandler(event));
            }
        });
        
        request.response().closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                System.out.println("response.closeHandler called: " + request.id());
                next.handle(null);
            }
        });
        
        request.response().endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                System.out.println("response.endHandler called: " + request.id());
                next.handle(null);
            }
        });
        
        this.httpService.handle(request, next);

    }
    
}
