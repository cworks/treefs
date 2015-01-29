/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Baked with love by corbett
 * Project: treefs
 * Package: net.cworks.treefs.api
 * Class: TreeFsApi
 * Created: 1/29/15 9:06 AM
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package net.cworks.treefs.api;

import net.cworks.treefs.server.core.HttpModule;
import net.cworks.treefs.server.core.HttpRouter;
import net.cworks.treefs.server.handler.HttpServices;

public class HttpApi {
    
    private HttpApi() {
        
    }
    
    public static HttpApi create(String rootUri) {

        HttpModule module = new HttpModule(null, rootUri);

        // sub-resource need to come before actual resources so matching works...need to fix this
        module.use(new HttpRouter().get("/:fs/.*/#meta$", HttpServices.metadataService()));
        module.use(new HttpRouter().delete("/:fs/.*/#trash$", HttpServices.trashPathService()));
        module.use(new HttpRouter().post("/:fs/.*/#copy$", HttpServices.copyService()));
        module.use(new HttpRouter().put("/:fs/.*/#move$", HttpServices.moveService()));

        // path resources & operations
        module.use(new HttpRouter().post("/:fs/.*", HttpServices.createPathService()));
        module.use(new HttpRouter().put("/:fs/.*"));
        module.use(new HttpRouter().patch("/:fs/.*"));
        module.use(new HttpRouter().delete("/:fs/.*"));
        module.use(new HttpRouter().get("/:fs/.*"));

        //module.use(new HttpRouter().get("/:fs/.*", HttpServices.readPathService()));
        //module.use(new HttpRouter().delete("/:fs/.*", HttpServices.deleteService()));

        // TODO clean this up
        return new HttpApi();
    }
}
