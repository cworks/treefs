package net.cworks.treefs.provider;

import net.cworks.treefs.spi.StorageProvider;
import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.syssp.SystemStorageProvider;

public class StorageProviderFactory {

    /**
     * TODO this class is by far from implemented and will need some work before launching treefs!
     * TODO for example we need a way to load valid TreeFsClient(s) that have been pre-authorized
     * TODO to use this service, the config needs to include what StorageProvider the client
     * TODO is doing business with (i.e. AWS S3, Local System).
     *
     * @param client
     * @return
     */
    static StorageProvider createProvider(TreeFsClient client){
        if("corbofett".equals(client.id())) {
            // TreeFs.home is a location on the file-system from which TreeFs has read/write access
            // and is used for a myriad of things like saving TreeFs file-system data.
            StorageProvider sp = SystemStorageProvider.newProvider()
                .withMount(TreeFs.mount())
                .withBucket(client.id())
                .create();
            return sp;
        } else if("sysuser".equals(client.id())) {
            // TreeFs.home is a location on the file-system from which TreeFs has read/write access
            // and is used for a myriad of things like saving TreeFs file-system data.
            StorageProvider sp = SystemStorageProvider.newProvider()
                .withMount(TreeFs.mount())
                .withBucket(client.id())
                .create();
            return sp;
        } else {
            return null;
        }
    }
}
