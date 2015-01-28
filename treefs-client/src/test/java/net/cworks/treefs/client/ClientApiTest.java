package net.cworks.treefs.client;

import net.cworks.treefs.server.TreeFsContainer;
import net.cworks.treefs.domain.TreeFsFile;
import net.cworks.treefs.domain.TreeFsFolder;
import net.cworks.treefs.domain.TreeFsPath;
import net.cworks.treefs.domain.TreeFsPathVisitor;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Unit tests that show how a client would use TreeFs
 */
public class ClientApiTest {

    /**
     * VertxContainer used in this unit test.  Note: treefs-client has a testCompile
     * dependency on treefs-server because of this which is fine but in no way should
     * treefs-client have a compile time dependency on treefs-server.
     */
    static TreeFsContainer container = null;

    static Config createConfig() {
        Config config = new Config() {
            @Override
            public String protocol() { return "http"; }
            @Override
            public int port() { return 1234; }
            @Override
            public String fileSystem() { return "myFileSystem"; }
            @Override
            public String host() { return "www.theresnoplacelikehome.com"; }
            @Override
            public String accountId() { return "corbofett"; }
            @Override
            public String authToken() { return "onefishtwofishredfishbluefish"; }
        };
        return config;
    }

// =================================================================================================
// CREATE TREEFS Context Tests/Examples
// =================================================================================================
    @Test
    public void createContextExamples() {

        // create TreeFs context with default ("http://127.0.0.1:1973/treefs") config
        Context treefs = TreeFs.create();
        Assert.assertEquals("localhost", treefs.config().host());

        // create TreeFs context with custom config
        Config config = createConfig();
        treefs = TreeFs.create(config);
        Assert.assertEquals("www.theresnoplacelikehome.com", treefs.config().host());
        Assert.assertEquals("myFileSystem", treefs.config().fileSystem());
        Assert.assertEquals("http", treefs.config().protocol());
        Assert.assertEquals(1234, treefs.config().port());

        // create TreeFs instance, overriding the host
        treefs = TreeFs.create("api.com/treefs");
        Assert.assertEquals("api.com/treefs", treefs.config().host());
        Assert.assertEquals("default", treefs.config().fileSystem());
        Assert.assertEquals("http", treefs.config().protocol());
        Assert.assertEquals(4444, treefs.config().port());

        // create TreeFs instance, overriding the host,
        // since http:// is added it will override protocol as well
        treefs = TreeFs.create("http://treefs.io");
        Assert.assertEquals("treefs.io/treefs", treefs.config().host());
        Assert.assertEquals("default", treefs.config().fileSystem());
        Assert.assertEquals("http", treefs.config().protocol());
        Assert.assertEquals(4444, treefs.config().port());

        // create TreeFs instance, overriding the host,
        // since https:// is added it will override protocol as well
        treefs = TreeFs.create("https://treefs.io");
        Assert.assertEquals("treefs.io/treefs", treefs.config().host());
        Assert.assertEquals("default", treefs.config().fileSystem());
        Assert.assertEquals("https", treefs.config().protocol());
        Assert.assertEquals(4444, treefs.config().port());

        // create TreeFs instance with an invalid port
        try {
            treefs = TreeFs.create("http://foo.com/treefs", 1023);
            Assert.fail("TreeFs instance created with invalid port, should of failed.");
        } catch(IllegalArgumentException ex) {
            // should throw because port was lower than 1024
            ex.printStackTrace();
        }

        // create TreeFs instance, overriding the host and port
        // since http:// is added it will override protocol as well
        treefs = TreeFs.create("http://happyhappyfunplace.com/treefs", 9876);
        Assert.assertEquals("happyhappyfunplace.com/treefs", treefs.config().host());
        Assert.assertEquals("default", treefs.config().fileSystem());
        Assert.assertEquals("http", treefs.config().protocol());
        Assert.assertEquals(9876, treefs.config().port());

    }

// =================================================================================================
// LS Api Examples
// =================================================================================================

    /**
     * Simple testing of basic list operation.
     * @prerequisite demo params has to be setup
     *
     */
    //@Test
    public void lsPath() {
        Context treefs = TreeFs.create();

        // list all paths (files and folder) in a given folder
        TreeFsPath p1 = treefs.ls("demo/n1_1").fetch();
        if(!(p1 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p1 should be an instanceof TreeFsFolder...#epicfail!!!");
            return;
        }

        TreeFsFolder f1 = (TreeFsFolder)p1;
        Assert.assertEquals("n1_1", f1.name());
        Assert.assertEquals("demo/n1_1", f1.path());
        Assert.assertEquals("folder", f1.type());
        Assert.assertEquals(f1.paths().size(), 5);
        Assert.assertEquals("n2_1", f1.paths().get(0).name());
        Assert.assertEquals("n2_2", f1.paths().get(1).name());
        Assert.assertEquals("nacho_eggs.png", f1.paths().get(2).name());
        Assert.assertEquals("nacho_fight.pptx", f1.paths().get(3).name());
        Assert.assertEquals("nacho_libre.pdf", f1.paths().get(4).name());
    }

    /**
     * Simple testing of basic list operation from a specific file-system
     */
    //@Test
    public void lsFromFs() {
        Context treefs = TreeFs.create();

        // list all paths (files and folder) in a given folder from a specific file-system
        TreeFsPath p1 = treefs.ls("demo/n1_2")
            .fromFs("ar")
            .fetch();
        if(!(p1 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p1 should be an instanceof TreeFsFolder...#u.g.l.y!!!");
        }
        TreeFsFolder f1 = (TreeFsFolder)p1;
        Assert.assertEquals("n1_2", f1.name());
        Assert.assertEquals("demo/n1_2", f1.path());
        Assert.assertEquals("folder", f1.type());
        Assert.assertEquals(f1.paths().size(), 2);
        Assert.assertEquals("n2_1", f1.paths().get(0).name());
        Assert.assertEquals("n2_2", f1.paths().get(1).name());
    }

    /**
     * Test list operation when only folders are requested
     */
    //@Test
    public void lsOnlyFolders() {
        Context treefs = TreeFs.create();

        // list all folders in demo
        TreeFsPath p1 = treefs.ls("demo")
            .foldersOnly()
            .fetch();

        if(!(p1 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p1 should be an instanceof TreeFsFolder...#thatdogdonthunt");
        }

        TreeFsFolder f1 = (TreeFsFolder)p1;
        Assert.assertEquals("demo", f1.name());
        Assert.assertEquals("demo", f1.path());
        Assert.assertEquals("folder", f1.type());

        for(TreeFsPath p : f1.paths()) {
            if(!(p instanceof TreeFsFolder)) {
                Assert.fail("TreeFsPath p should be an instanceof TreeFsFolder...#folderonlybra");
            }
        }

        // these are the folders under demo, setup script will create them
        Assert.assertEquals(3, f1.paths().size());
        Assert.assertEquals("n1_1", f1.paths().get(0).name());
        Assert.assertEquals("n1_2", f1.paths().get(1).name());
        Assert.assertEquals("n1_3", f1.paths().get(2).name());
    }

    /**
     * Test list operation with several tests using the depth and recursive setting.
     */
    //@Test
    public void lsDepth() {
        Context treefs = TreeFs.create();

        // list content of all paths recursively (up to default max depth, set to protect server side)
        TreeFsPath p1 = treefs.ls("depthtest")
            .recursive()
            .fetch();

        if(!(p1 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p1 should be an instanceof TreeFsFolder...#dork");
        }

        TreeFsFolder f1 = (TreeFsFolder)p1;
        Assert.assertEquals("depthtest", f1.name());
        Assert.assertEquals("depthtest", f1.path());
        Assert.assertEquals("folder", f1.type());

        for(TreeFsPath p : f1.paths()) {
            if(!(p instanceof TreeFsFolder)) {
                Assert.fail("TreeFsPath p should be an instanceof TreeFsFolder...#chuckisnothappywithu");
            }
        }

        // walk the path tree and count
        final int[] count = {0,0, 0};
        f1.walk(new TreeFsPathVisitor() {
            public void visit(TreeFsPath path) {
                count[0]++;
                // check the name of a few paths
                switch (count[0]) {
                    case 25:  Assert.assertEquals("gtufxxil", path.name()); break;
                    case 50:  Assert.assertEquals("fvnlquup", path.name()); break;
                    case 75:  Assert.assertEquals("cmalqopc", path.name()); break;
                    case 100: Assert.assertEquals("wqdozhpv", path.name()); break;
                }
            }
        });

        Assert.assertEquals(100, count[0]);

        TreeFsPath p2 = treefs.ls("depthtest")
            .recursive()
            .depth(10)
            .fetch();
        if(!(p2 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p2 should be an instanceof TreeFsFolder...#doubledork");
        }
        TreeFsFolder f2 = (TreeFsFolder)p2;
        f2.walk(new TreeFsPathVisitor() {
            @Override
            public void visit(TreeFsPath path) {
                count[1]++;
                switch (count[1]) {
                    case  1:  Assert.assertEquals("flabcdou", path.name()); break;
                    case  2:  Assert.assertEquals("tvkujtcf", path.name()); break;
                    case  3:  Assert.assertEquals("cbtcsweg", path.name()); break;
                    case  4:  Assert.assertEquals("dioqzvrq", path.name()); break;
                    case  5:  Assert.assertEquals("tljwufob", path.name()); break;
                    case  6:  Assert.assertEquals("ephowjrx", path.name()); break;
                    case  7:  Assert.assertEquals("fjweuxtj", path.name()); break;
                    case  8:  Assert.assertEquals("xinptlml", path.name()); break;
                    case  9:  Assert.assertEquals("mqreoysu", path.name()); break;
                    case 10:  Assert.assertEquals("gnauqgnm", path.name()); break;
                }
            }
        });
        Assert.assertEquals(10, count[1]);

        TreeFsPath p3 = treefs.ls("depthtest/flabcdou/tvkujtcf/cbtcsweg/dioqzvrq/tljwufob/" +
            "ephowjrx/fjweuxtj/xinptlml/mqreoysu/gnauqgnm")
                .depth(3)
                .recursive()
                .filesOnly().fetch();
        if(!(p3 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p3 should be an instanceof TreeFsFolder...#badboyswhachagonnado");
        }

        Assert.assertEquals("gnauqgnm", p3.name());
        Assert.assertEquals("depthtest/flabcdou/tvkujtcf/cbtcsweg/dioqzvrq/tljwufob/" +
            "ephowjrx/fjweuxtj/xinptlml/mqreoysu/gnauqgnm", p3.path());
        Assert.assertEquals("folder", p3.type());

        TreeFsFolder f3 = (TreeFsFolder)p3;
        f3.walk(new TreeFsPathVisitor() {
            @Override
            public void visit(TreeFsPath path) {
                count[2]++;
                switch (count[2]) {
                    case  1:  Assert.assertEquals("pbizqiix", path.name()); break;
                    case  2:  Assert.assertEquals("virutoed", path.name()); break;
                    case  3:  Assert.assertEquals("zemhmuos", path.name()); break;
                }
            }
        });

    }

    //@Test
    public void lsTxt() {
        Context treefs = TreeFs.create();

        // list all text files recursively up to a depth of 10
        TreeFsPath p1 = treefs.ls("demo/n1_1")
                .depth(10)
                .filesOnly()
                .glob("*.txt")
                .fetch();

        if(!(p1 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p1 is not a folder and my mom said it should be #mommanothappy");
        }

        TreeFsFolder f1 = (TreeFsFolder)p1;
        Assert.assertEquals("n1_1", f1.name());
        Assert.assertEquals("demo/n1_1", f1.path());

        // better be only txt files (and the folders that contain them)
        f1.walk(new TreeFsPathVisitor() {
            @Override
            public void visit(TreeFsPath path) {
                if(path instanceof TreeFsFile) {
                    TreeFsFile file = (TreeFsFile)path;
                    Assert.assertEquals(true, file.name().endsWith(".txt"));
                }
            }
        });
    }

    /**
     * list examples
     */
    //@Test
    public void lsTxtPngJpg() {

        Context treefs = TreeFs.create();

        // list all text files recursively up to a depth of 10
        TreeFsPath p1 = treefs.ls("demo/n1_1")
                .depth(10)
                .filesOnly()
                .glob("*.txt").glob("*.png").glob("*.jpg") // include .txt or .png or .jpg
                .fetch();

        if(!(p1 instanceof TreeFsFolder)) {
            Assert.fail("TreeFsPath p1 is not a folder and my mom said it should be #mommanothappy");
        }

        TreeFsFolder f1 = (TreeFsFolder)p1;
        Assert.assertEquals("n1_1", f1.name());
        Assert.assertEquals("demo/n1_1", f1.path());

        // better be only txt files (and the folders that contain them)
        f1.walk(new TreeFsPathVisitor() {
            @Override
            public void visit(TreeFsPath path) {
                if(path instanceof TreeFsFile) {
                    TreeFsFile file = (TreeFsFile)path;
                    Assert.assertEquals(true,
                       (file.name().endsWith(".txt") ||
                        file.name().endsWith(".png") ||
                        file.name().endsWith(".jpg"))
                    );
                }
            }
        });
    }

    /**
     * list all power-point and pdf files in a flatten structure (i.e. not tree like), instead
     * using a simple list.
     */
    //@Test
    public void lsPptxPdfFlat() {

        Context treefs = TreeFs.create();

        List<TreeFsPath> paths = treefs.ls("demo/n1_1")
            .filesOnly()
            .recursive()
            .glob("*.txt")
                .glob("*.png")
                    .glob("*.jpg").fetchList();

        for(TreeFsPath path : paths) {
            TreeFsFile file = (TreeFsFile)path;
            Assert.assertEquals(true,
                    (file.name().endsWith(".txt") ||
                            file.name().endsWith(".png") ||
                            file.name().endsWith(".jpg"))
            );
        }

    }



// =================================================================================================
// Create Path (Folders and Files) examples
// =================================================================================================

    @Test
    public void createPathExamples() {

        Context treefs = TreeFs.create();

        // create path
        TreeFsPath path1 = treefs.newPath("a/new/path/" + System.currentTimeMillis())
            .addMeta("a", "is for Apple")
            .addMeta("b", "is for Button")
            .addMeta("c", "is for Candy")
            .overwrite()
            .create();

        TreeFsPath path2 = treefs.ls("a/new/path").fetch();
        Assert.assertEquals("a/new/path", path2.path());
        Assert.assertEquals("path", path2.name());
    }

    /**
     * Get metadata for paths (files and folders)
     */
    public void metadataExamples() {

        Context treefs = TreeFs.create();

        // get all metadata associated with the given path
        //Map<String, Object> m1 = treefs.meta("demo/n1_1").fetch();

        // get all metadata associated with the given path
        //Map<String, Object> m2 = treefs.meta("demo/n1_2/nacho_libre.pdf").fetch();
    }

    /**
     * Update path
     */
    public void updatePathMeta() {

        Context treefs = TreeFs.create();

        // update path with metadata
//        treefs.path("a/path/with/metadata")
//            .withMeta("key4", new JsonObject()).update();
//
//        // update path with metadata and add a callback handler
//        treefs.path("a/path/with/metadata")
//            .withMeta("key2", new Long(2000))
//            .withHandler(new Handler<Message>() {
//                public void handle(Message response) {
//                    System.out.println(response.statusCode());
//                }
//            }).update();

    }

    /**
     * Trash path
     */
    public void trashPath() {

        Context treefs = TreeFs.create();


        // move to trash
        //treefs.path("a/path/to/trash").trash();

        // move to trash recursively
//        treefs.path("a/path/to/trash/two")
//            .recursive()
//            .trash();

        // move to trash and get status of op
//        treefs.path("a/path/to/trash/three")
//            .recursive()
//            .withHandler(new Handler<Message>() {
//                public void handle(Message response) {
//                    System.out.println(response.statusCode());
//                }
//            }).trash();
    }

    /**
     * Create file methods will take a file from the local file system and upload it to TreeFs
     */
    public void createFileExamples() {

        Context treefs = TreeFs.create();

        // basic create file with no key
        TreeFsFile f1 = treefs.newFile(new File("chuck.jpg"))
            .in("/a/remote/folder/mypics")
            .create();

        TreeFsFile f2 = treefs.newFile(new File("some/local/file.zip"))
            .in("a/remote/place")
            .key("my-special-key-for-this-file")
            .saveAs("mySpecialFile.zip")
            .create();

        // create file with some metadata and a sha1
        // if sha1 is given then file content will be compared to sha1
        // if compare is successfull the file is stored otherwise it's rejected
        TreeFsFile f3 = treefs.newFile(new File("a/local/file.zip"))
            .in("/a/remote/folder")
            .addMeta("name", "chuck")
            .addMeta("phone", "817-123-9999")
            .addMeta("patientId", "88888888")
            .sha1("a-sha1-of-file.zip").overwrite(true).create();

        // Create a file and include the number of bytes that compose the file in size()
        // TODO Not sure why we would need this
        TreeFsFile f4 = treefs.newFile(new File(""))
            .in("a/remote/folder")
            .size(10000L).create();

    }

}
