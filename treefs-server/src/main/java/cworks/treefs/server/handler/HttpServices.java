package cworks.treefs.server.handler;

import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.worker.Work;
import cworks.treefs.server.core.EventBusService;

/**
 * Static Factory class for creating HttpService(s)
 */
public class HttpServices {

    public static HttpService createPathService() {

        return new PathService();
    }

    public static HttpService readPathService() {

        return new FetchPathService();
    }

    public static HttpService updatePathService() {

        return new PlaceHolderService();
    }

    public static HttpService trashPathService() {

        return new TrashService();
    }

    public static HttpService metadataService() {

        return new MetadataService();
    }

    public static HttpService copyService() {

        return new CopyService();
    }

    public static HttpService deleteService() {

        return new DeleteService();
    }

    public static HttpService moveService() {

        return new MoveService();
    }

    public static HttpService siegeService() {

        return new SiegeService();
    }

    public static HttpService worker(Work work) {

        EventBusService workerService = new EventBusService(work);
        return workerService;
    }

    public static HttpService pingService() {

        return new PingService();
    }

    public static HttpService placeHolderService() {

        return new PlaceHolderService();
    }

    public static HttpService settingsService() {

        return new SettingsService();
    }
}
