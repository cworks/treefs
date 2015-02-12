package cworks.treefs.server.handler;

import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.worker.Work;
import cworks.treefs.server.core.EventBusService;

/**
 * Static Factory class for creating HttpService(s)
 */
public class HttpServices {

    public static HttpService createPathService() {

        return new CreatePathHttpService();
    }

    public static HttpService readPathService() {

        return new FetchPathHttpService();
    }

    public static HttpService updatePathService() {

        return null;
    }

    public static HttpService trashPathService() {

        return new TrashHandler();
    }

    public static HttpService metadataService() {

        return new MetadataHandler();
    }

    public static HttpService copyService() {

        return new CopyHandler();
    }

    public static HttpService deleteService() {

        return new DeleteHandler();
    }

    public static HttpService moveService() {

        return new MoveService();
    }

    public static HttpService siegeService() {

        return new SiegeHttpService();
    }

    public static HttpService worker(Work work) {

        EventBusService workerService = new EventBusService(work);
        return workerService;
    }

    public static HttpService pingService() {

        return new PingService();
    }
}
