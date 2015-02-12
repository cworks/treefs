package cworks.treefs.provider;

import cworks.treefs.spi.TreeCopyOption;
import cworks.treefs.spi.TreeFolder;
import cworks.treefs.spi.TreePath;
import cworks.treefs.spi.StorageException;
import cworks.treefs.TreeFsClient;
import cworks.treefs.syssp.SystemStorageProvider;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class SystemStorageProviderTest {

    @Test
    public void testOpenFolder() {
        try {
            TreeFolder sFolder = _openFolder(0);
            Assert.assertEquals("n1", sFolder.name());
            // should be null because 0 depth used
            Assert.assertNull(sFolder.items());
        } catch (StorageException e) {
            Assert.fail();
        }
    }

    @Test
    public void testOpenFolderNegativeDepth() {
        try {
            TreeFolder sFolder = _openFolder(-1);
            Assert.assertEquals("n1", sFolder.name());
            // should be null because -1 depth used and that should be same as depth of 0
            Assert.assertNull(sFolder.items());
        } catch (StorageException e) {
            Assert.fail();
        }
    }

    @Test
    public void testOpenFolderDepth1() {
        try {
            TreeFolder sFolder = _openFolder(1);
            Assert.assertEquals("n1", sFolder.name());
            Assert.assertEquals(3, sFolder.items().size());
            Assert.assertEquals("n2", sFolder.items().get(0).name());
            Assert.assertEquals("n3", sFolder.items().get(1).name());
            _traverse(sFolder);
        } catch (StorageException e) {
            Assert.fail();
        }
    }

    @Test
    public void testOpenFolderDepth2() {
        try {
            TreeFolder n1 = _openFolder(2);
            Assert.assertEquals("n1", n1.name());
            // n1 should have 3 children
            Assert.assertEquals(3, n1.items().size());
            Assert.assertEquals("n2", n1.items().get(0).name());
            Assert.assertEquals("n3", n1.items().get(1).name());
            Assert.assertEquals("testfile.txt", n1.items().get(2).name());
            TreeFolder n2 = (TreeFolder)n1.items().get(0);
            // n2 should have 3 children
            Assert.assertEquals(3, n2.items().size());
            Assert.assertEquals("n4", n2.items().get(0).name());
            Assert.assertEquals("n5", n2.items().get(1).name());
            Assert.assertEquals("testfile.txt", n2.items().get(2).name());
            TreeFolder n3 = (TreeFolder)n1.items().get(1);
            // n3 should have 2 children
            Assert.assertEquals(2, n3.items().size());
            Assert.assertEquals("n6", n3.items().get(0).name());
            Assert.assertEquals("testfile.txt", n3.items().get(1).name());

            _traverse(n1);
        } catch (StorageException e) {
            Assert.fail();
        }
    }

    @Test
    public void testCopyFolder() throws IOException {
        SystemStorageProvider provider = _provider();
        Path sourceFolder = Paths.get("folderToFolderCopy/a1");
        Path targetFolder = Paths.get("folderToFolderCopy/a2");
        try {
            provider.copy(sourceFolder, targetFolder, TreeCopyOption.RECURSIVE);
            Assert.assertTrue(
                provider.exists(Paths.get("folderToFolderCopy/a2/b1")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/b2")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/b3")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/encarnacion.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/b1/ramses.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/b1/c1/religious_man.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/b2/eagles_eggs.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a2/b2/c3/corn.txt"))
            );

        } catch(StorageException ex) {
            Assert.fail();
        } finally {
            FileUtils.forceDelete(new File(_clientRoot() + "/folderToFolderCopy/a2/b1"));
            FileUtils.forceDelete(new File(_clientRoot() + "/folderToFolderCopy/a2/b2"));
            FileUtils.forceDelete(new File(_clientRoot() + "/folderToFolderCopy/a2/b3"));
            FileUtils.forceDelete(new File(_clientRoot() + "/folderToFolderCopy/a2/encarnacion.txt"));
            FileUtils.forceDelete(new File(_clientRoot() + "/folderToFolderCopy/a2/encarnacion.txt.f"));
        }
    }

    @Test
    public void testCopyFolderIntoFolder() throws IOException {
        SystemStorageProvider provider = _provider();
        Path sourceFolder = Paths.get("folderToFolderCopy/a1");
        Path targetFolder = Paths.get("folderToFolderCopy/a3");
        try {
            provider.copy(sourceFolder, targetFolder, TreeCopyOption.RECURSIVE, TreeCopyOption.INTO);
            Assert.assertTrue(
                provider.exists(Paths.get("folderToFolderCopy/a3/a1")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/encarnacion.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b1")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b1/ramses.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b1/c1")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b1/c1/religious_man.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b1/c2")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b1/c3")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b2")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b2/eagles_eggs.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b2/c1")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b2/c2")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b2/c3")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b2/c3/corn.txt")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b3")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b3/c1")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b3/c2")) &&
                provider.exists(Paths.get("folderToFolderCopy/a3/a1/b3/c3"))
            );
        } catch(StorageException ex) {
            Assert.fail();
        } finally {
            FileUtils.forceDelete(new File(_clientRoot() + "/folderToFolderCopy/a3/a1"));
        }
    }

    /**
     * Copies a file to a folder
     * source: fileToFolderCopy/a1/encarnacion.txt
     * target: n2
     * result: n2/encarnacion.txt
     *
     */
    @Test
    public void testFileToFolderCopy() throws IOException {
        SystemStorageProvider provider = _provider();
        Path sourceFolder = Paths.get("fileToFolderCopy/a1/encarnacion.txt");
        Path targetFolder = Paths.get("n2");
        try {
            provider.copy(sourceFolder, targetFolder, StandardCopyOption.REPLACE_EXISTING);
            Assert.assertTrue(provider.exists(Paths.get("n2/encarnacion.txt")));
        } catch(StorageException ex) {
            Assert.fail();
        } finally {
            FileUtils.forceDelete(new File(_clientRoot() + "/n2/encarnacion.txt"));
            FileUtils.forceDelete(new File(_clientRoot() + "/n2/encarnacion.txt.f"));
        }
    }

    /**
     * Copies a file to a file with no filename change but target path must be different from
     * source path.
     *
     * source: fileToFolderCopy/a1/encarnacion.txt
     * target: n1/n3/encarnacion.txt
     * result: n1/n3/encarnacion.txt
     * @throws IOException
     */
    @Test
    public void testFileToFileNoRename() throws IOException {
        SystemStorageProvider provider = _provider();
        Path sourceFile = Paths.get("fileToFolderCopy/a1/encarnacion.txt");
        Path targetFile = Paths.get("n1/n3/encarnacion.txt");
        try {
            provider.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            Assert.assertTrue(provider.exists(Paths.get("n1/n3/encarnacion.txt")));
        } catch(StorageException ex) {
            Assert.fail();
        } finally {
            FileUtils.forceDelete(new File(_clientRoot() + "/n1/n3/encarnacion.txt"));
            FileUtils.forceDelete(new File(_clientRoot() + "/n1/n3/encarnacion.txt.f"));
        }
    }

    TreeFolder _openFolder(int maxDepth) throws StorageException {
        SystemStorageProvider provider = _provider();
        // a value of zero is the same as calling openFolder(Paths.get("n1"))
        TreeFolder sFolder = provider.openFolder(Paths.get("n1"), maxDepth);
        return sFolder;
    }

    SystemStorageProvider _provider() {
        String root = _root();
        TreeFsClient client = new TreeFsClient("corbofett");
        SystemStorageProvider provider = SystemStorageProvider.newProvider()
            .withMount(root)
            .withBucket(client.id()).create();
        return provider;
    }

    String _root() {
        String root = System.getProperty("user.dir") + "/src/test/resources/users";
        return root;
    }

    String _clientRoot() {
        return _root() + "/corbofett";
    }

    void _traverse(TreeFolder folder) {
        if(folder.items() == null) {
            return;
        }
        List<TreePath> items = folder.items();
        for(TreePath item : items) {
            //System.out.println(item.toString());
            if(item instanceof TreeFolder) {
                _traverse((TreeFolder)item);
            }
        }

    }

}
