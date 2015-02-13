package cworks.treefs.security;

import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;

import static cworks.treefs.TreeFsValidation.isNull;

public class TreeFsSecurity {



    /**
     * Determine go-nogo for client
     * TODO this is just a placeholder at the moment (01/21/2014)
     *
     * For now we're going to do the simple thing and that is open the treefsclients.json
     * file and see if the given client is found and authorized within this file.
     *
     * @param clientId
     * @return
     */
    public static boolean isAuthorized(String clientId) {

        TreeFsClient client = TreeFs.client(clientId);
        if(isNull(client)) {
            return false;
        }

        return client.enabled();
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
