package cworks.treefs;

import junit.framework.Assert;
import cworks.treefs.domain.TreeFsFactory;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.spi.StorageException;
import cworks.treefs.spi.TreeFolder;
import cworks.treefs.syssp.SystemStorageProvider;
import org.junit.Test;

import java.nio.file.Paths;

public class TreeFsStorageManagerTest {

    @Test
    public void openFolder() {
        try {
            TreeFolder sourceFolder = _openFolder(2);
            TreeFsFolder targetFolder = TreeFsFactory.folder(sourceFolder);
            // perform a traverse compare
        } catch (StorageException e) {
            Assert.fail();
        }

        Class o = new Object().getClass();
    }

    private TreeFolder _openFolder(int maxDepth) throws StorageException {
        String root = System.getProperty("user.dir") + "/src/test/resources/users";
        TreeFsClient client = new TreeFsClient("corbofett");
        SystemStorageProvider provider = SystemStorageProvider.newProvider()
            .withMount(root)
            .withBucket(client.id()).create();
        // a value of zero is the same as calling openFolder(Paths.get("n1"))
        TreeFolder sFolder = provider.openFolder(Paths.get("n1"), maxDepth);
        return sFolder;
    }

}
