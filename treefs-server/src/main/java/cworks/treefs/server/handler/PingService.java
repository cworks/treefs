package cworks.treefs.server.handler;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.common.dt.ISO8601DateParser;
import cworks.treefs.server.json.JsonService;
import cworks.treefs.server.json.JsonRequest;

import java.util.Date;

public class PingService extends JsonService {
    
    @Override
    public void handle(final JsonRequest request) {

        String clientId = request.getParameter("clientId", "anonymous");

        JsonObject pingBody = Json.object()
            .string("hello", clientId)
            .string("app", "treefs")
            .string("version", "1.0")
            .string("time", ISO8601DateParser.toString(new Date()))
            .build();
        
        request.response(pingBody);
    }

}
