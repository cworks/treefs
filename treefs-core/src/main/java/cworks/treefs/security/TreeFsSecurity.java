package cworks.treefs.security;

import cworks.treefs.TreeFsClient;

public class TreeFsSecurity {

    /**
     * Determine go-nogo for client
     * TODO this is just a placeholder at the moment (01/21/2014)
     * @param client
     * @return
     */
    public static boolean isAuthorized(final TreeFsClient client) {

        if("corbofett".equalsIgnoreCase(client.id())) {
            return true;
        } else if("treefspoc".equalsIgnoreCase(client.id())) {
            return true;
        } else if("sysuser".equalsIgnoreCase(client.id())) {
            return true;
        } else if("treefss3user".equalsIgnoreCase(client.id())) {
            return true;
        }
        return false;
    }

    /**
     * Determine if client has a file-system with the fsid, if not do not create and return false
     * TODO this is a placeholder at the moment (01/21/2014)
     * @param client
     * @param fsid
     * @return
     */
    public static boolean hasFileSystem(TreeFsClient client, String fsid) {
        return true;
    }
}
