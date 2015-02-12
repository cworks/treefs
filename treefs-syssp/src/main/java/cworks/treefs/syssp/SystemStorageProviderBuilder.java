package cworks.treefs.syssp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;

public class SystemStorageProviderBuilder {

    private String mount = null;

    private String bucket = null;

    private SystemStorageProvider provider = null;

    public SystemStorageProviderBuilder withMount(String mount) {
        this.mount = mount;
        return this;
    }

    public SystemStorageProviderBuilder withBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public SystemStorageProvider create() {

        provider = new SystemStorageProvider();

        if(!isNullOrEmpty(mount)) {
            provider._mount(mount);
        }

        if(!isNullOrEmpty(bucket)) {
            provider._bucket(bucket);
        }

        // if mount does not exist then SystemStorageProvider will not be able to
        // persist content, it does not assume to create the mount for security reasons
        // so check for the existence of the mount and log a warning if it does not exist
        // so that someone might see that it needs to be manually created
        if(validMount()) {
            // if the bucket does not exist within the mount then we go ahead and create it
            checkBucket();
        }

        return provider;
    }

    private boolean validMount() {

        String mnt = provider._mount();
        boolean exists = Files.exists(Paths.get(mnt));
        if(!exists) {
            System.out.print("**** MOUNT LOCATION DOES NOT EXIST **** ");
            System.out.print("Please create before content can be persisted to : ");
            System.out.println(mnt);
        }

        return exists;
    }

    private void checkBucket() {

        // if bucket does not exist then we try to create it
        Path path = Paths.get(provider._mount(), provider._bucket());
        if(!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException ex) {
                System.out.print("**** AUTO CREATE BUCKET FAILED **** ");
                System.out.print("Please create before content can be persisted to : ");
                System.out.println(path.toString());
                ex.printStackTrace();
            }
        }


    }

}
