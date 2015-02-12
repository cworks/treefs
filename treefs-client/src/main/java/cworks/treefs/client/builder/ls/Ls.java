package cworks.treefs.client.builder.ls;

import cworks.treefs.client.TreeFsClient;
import cworks.treefs.client.Config;
import cworks.treefs.domain.TreeFsDeserializer;
import cworks.treefs.domain.TreeFsFile;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.domain.TreeFsPath;
import cworks.treefs.domain.TreeFsPathVisitor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ls implements LsApi {

    /**
     * logger
     */
    private static final Logger logger = Logger.getLogger(Ls.class);

    /**
     * depth parameter that controls how deep to perform list operation
     */
    private int depth = 0;

    /**
     * Only list files no folders
     */
    private boolean filesOnly = false;

    /**
     * Only list folders no files
     */
    private boolean foldersOnly = false;

    /**
     * If true then do a recursive listing of the default depth (controlled on server)
     */
    private boolean recursive = false;

    /**
     * Every listing must be done on a file-system but default the default file-system is default
     * creative ... no?
     */
    private String fs = "default";

    /**
     * List of glob patterns used to filter the listing
     */
    private List<String> globs = null;

    /**
     * Path to start the listing from
     */
    private String path = null;

    /**
     * Config instance contains treefs-server goody-goods.
     */
    private Config config = null;

    /**
     * Listing requires a path to list from
     * @param path
     * @param config
     */
    Ls(String path, Config config) {
        this.path = path;
        this.globs = new ArrayList<String>();
        this.config = config;
    }

    @Override
    public void depth(int n) {
        // arbitrarily checking upper limit of 500 on client side, even though server has a limit
        if(n > 0 && n < 500) {
            depth = n;
        }
    }

    @Override
    public void filesOnly() {
        filesOnly = true;
    }

    @Override
    public void foldersOnly() {
        foldersOnly = true;
    }

    @Override
    public void fromFs(String fs) {
        if(fs != null && fs.trim().length() > 0) {
            this.fs = fs;
        }
    }

    @Override
    public void glob(String pattern) {
        // only allow 32 glob patterns
        if(globs.size() < 32) {
            globs.add(pattern);
        }
    }

    @Override
    public void recursive() {
        recursive = true;
    }

    @Override
    public TreeFsPath fetch() {
        logger.info("ls fetch...");

        TreeFsClient client = TreeFsClient.create(config.accountId(), config.authToken());
        TreeFsPath treefsPath = null;
        try {
            StringBuilder sb = new StringBuilder();
            if(!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);

            Map<String, Object> params = new HashMap<String, Object>();
            if(depth > 0)   { params.put("depth",       depth);       }
            if(filesOnly)   { params.put("filesOnly",   filesOnly);   }
            if(foldersOnly) { params.put("foldersOnly", foldersOnly); }
            if(recursive)   { params.put("recursive",   recursive);   }
            if(globs.size() > 0) {
                String filters = "";
                for(String glob : globs) {
                    filters = filters + glob + "|";
                }
                filters = filters.substring(0, filters.length()-1);
                params.put("filter", filters);
            }

            String url = config.protocol() + "://" + config.host() + ":" + config.port()
                    + "/treefs/" + fs + sb.toString();

            String response = client.get(url)
                .params(params)
                .asString();

            System.out.println(response);

            treefsPath = TreeFsDeserializer.newDeserializer().folder(response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return treefsPath;
    }

    /**
     * Return a flattened list
     * @return
     */
    @Override
    public List<TreeFsPath> fetchList() {
        final List<TreeFsPath> list = new ArrayList<TreeFsPath>();

        TreeFsPath path = fetch();

        // if this path is a file then add to list then return
        if(path instanceof TreeFsFile) {
            list.add(path);
            return list;
        }

        // if this path is a folder then add to list then return
        if(path instanceof TreeFsFolder) {
            TreeFsFolder f = (TreeFsFolder)path;
            f.walk(new TreeFsPathVisitor() {
                @Override
                public void visit(TreeFsPath path) {
                    if(filesOnly && !foldersOnly) {
                        if(path instanceof TreeFsFile) {
                            list.add(path);
                        }
                    } else if(foldersOnly && !filesOnly) {
                        if(path instanceof TreeFsFolder) {
                            list.add(path);
                        }
                    } else {
                        // otherwise add both files and folders
                        list.add(path);
                    }
                }
            });

        }

        return list;
    }
}
