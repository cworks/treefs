package cworks.treefs;

import cworks.json.Json;
import cworks.json.JsonArray;
import cworks.json.JsonObject;
import cworks.treefs.provider.TreeFsStorageManager;

import java.io.File;
import java.util.Map;

import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;

/**
 * Top level class in TreeFs used to obtain configuration data and global settings
 * such as the home() directory for TreeFs.
 *
 * Configuration is returned using the following precedence.
 * 1. env first then...
 * 2. properties second, these are -D options, then...
 * 3. config third, this is external configuration passed into TreeFs, then...
 * 4. defined default, a defined constant that will be used if 1-3 above fail to make.
 */
public final class TreeFs {

    private static final TreeFsContext context = TreeFsContext.context();

    public static void mergeIn(Map config, Map env) {
        context.config(config);
        context.env(env);
    }

    /**
     * return treefs home
     * env        overrides properties
     * properties override  config
     * config     overrides defaults
     * defaults   end of the line
     *
     */
    public static String home() {

        String treefsHome = stringValue("treefs.home");
        if(isNullOrEmpty(treefsHome)) {
            // default home location
            treefsHome = System.getProperty("user.dir");
        }

        return treefsHome;
    }

    /**
     * Return the data mount under home()
     * @return
     */
    public static String mount() {

        String mount = stringValue("treefs.mount");
        if(isNullOrEmpty(mount)) {
            // default home location
            mount = home() + File.separator + "treefs-data";
        } else {
            mount = home() + File.separator + mount;
        }

        return mount;
    }

    /**
     * upload dir for TreeFs under home()
     * @return
     */
    public static String uploadDir() {

        String uploads = stringValue("treefs.uploads");
        if(isNullOrEmpty(uploads)) {
            // default home location
            uploads = home() + File.separator + "uploads";
        } else {
            uploads = home() + File.separator + uploads;
        }

        System.out.println("uploadDir=" + uploads);
        return uploads;
    }

    /**
     * download dir for TreeFs under home()
     * @return
     */
    public static String downloadDir() {

        String downloads = stringValue("treefs.downloads");
        if(isNullOrEmpty(downloads)) {
            // default home location
            downloads = home() + File.separator + "downloads";
        } else {
            downloads = home() + File.separator + downloads;
        }

        System.out.println("downloadDir=" + downloads);
        return downloads;
    }

    /**
     * Return a string property according to the rules of attempting to obtain
     * from environment first, the System.properties, and then configuration.
     * @param key
     * @return
     */
    private static String stringValue(String key) {
        String value = context.envString(toEnvvar(key));
        if(!isNullOrEmpty(value)) {
            return value;
        }

        value = System.getProperty(key);
        if(!isNullOrEmpty(value)) {
            return value;
        }

        value = context.configString(key);
        if(!isNullOrEmpty(value)) {
            return value;
        }

        return null;
    }

    /**
     * Convert the input path to a path that TreeFs requires
     * @param path
     * @return
     */
    public static String convertPath(String path) {
        String clean = path.replaceAll("[^a-zA-Z0-9.-\\/]", "_");
        String convert = clean.replace("\\", "/");
        return convert;
    }

    /**
     * Convert the input path to a path of the unix variety
     * @param path
     * @return
     */
    public static String unixPath(String path) {
        String convert = path.replace("\\", "/");
        return convert;
    }

    /**
     * Create a new TreeFsStorageManager for the given client to handle folder and file ops
     * @param client
     * @return
     */
    public static TreeFsStorageManager storageManager(final TreeFsClient client) {

        TreeFsStorageManager manager = TreeFsStorageManager.create(client);
        if(manager == null) {
            throw new TreeFsException("No StorageManager configured for client: " + client.toString());
        }

        return manager;
    }

    /**
     * Create a TreeFsClient if the given clientId is valid.
     * @param clientId
     * @return
     */
    public static TreeFsClient client(String clientId) {

        if(clientId == null) {
            throw new IllegalArgumentException("clientId cannot be null");
        }

        JsonObject config = Json.asObject(new File(stringValue("treefs.clients")));
        JsonArray clients = config.getArray("clients");
        for(Object o : clients) {
            JsonObject client = (JsonObject)o;
            if(clientId.equals(client.getString("id"))) {
                return new TreeFsClient(
                    client.getString("id"),
                    client.getBoolean("enabled"));
            }
        }

        return null;
    }

    public static String clientHeader() {
        return "treefs-client";
    }

    private static String toEnvvar(String key) {
        String envVar = key.toUpperCase();
        return envVar.replace('.', '_');
    }
}
