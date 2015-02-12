package cworks.treefs.client.builder.folder;

import cworks.json.Json;
import cworks.treefs.client.TreeFsClient;
import cworks.treefs.client.Config;
import cworks.treefs.domain.TreeFsDeserializer;
import cworks.treefs.domain.TreeFsPath;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Folder implements FolderApi {

    private static final Logger logger = Logger.getLogger(Folder.class);

    private Config config = null;

    private boolean overwrite = false;

    private String path = null;

    private Map<String, Object> metadata = null;

    /**
     * Every listing must be done on a file-system but default the default file-system is default
     * creative ... no?
     */
    private String fs = "default";

    public Folder(String path, Config config) {
        this.path = path;
        this.config = config;
        this.overwrite = false;
        this.metadata = new HashMap<String, Object>();
    }

    @Override
    public void addMeta(String key, Object value) {
        this.metadata.put(key, value);
    }

    @Override
    public void overwrite() {
        this.overwrite = true;
    }

    @Override
    public TreeFsPath create() {

        logger.info("folder create...");

        TreeFsClient client = TreeFsClient.create(config.accountId(), config.authToken());
        TreeFsPath treefsPath = null;
        try {
            StringBuilder sb = new StringBuilder();
            if(!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);

            Map<String, Object> params = new HashMap<String, Object>();
            if(overwrite)   { params.put("overwrite",   overwrite);   }
            if(metadata.size() > 0) {
                params.put("metadata", metadata);
            }

            String url = config.protocol() + "://" + config.host() + ":" + config.port()
                    + "/treefs/" + fs + sb.toString();

            String response = client.post(url)
                .body(Json.asString(params))
                .asString();

            System.out.println(response);

            treefsPath = TreeFsDeserializer.newDeserializer().folder(response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return treefsPath;
    }
}
