package cworks.treefs.awssp;

import cworks.treefs.common.FileUtils;
import cworks.treefs.common.IOUtils;
import cworks.treefs.common.StringUtils;
import cworks.treefs.spi.NoTreePathException;
import cworks.treefs.spi.StorageException;
import cworks.treefs.spi.StorageProvider;
import cworks.treefs.spi.TreeFile;
import cworks.treefs.spi.TreeFileExistsException;
import cworks.treefs.spi.TreeFolder;
import cworks.treefs.spi.TreeFolderNotEmptyException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cworks.treefs.common.IOUtils.closeQuietly;
import static cworks.treefs.common.ObjectUtils.isNull;

/**
 * This unit test is geared towards testing the S3StorageProvider implementation
 * but it also serves as a general guide on how to use the StorageProvider API.
 *
 * @author comartin
 */
public class S3StorageProviderTest {

    /**
     * Source PDF file that's used thorough this unit test
     */
    private static String NACHO_TEST_FILE = "src/test/resources/data/nacho_libre.pdf";

    /**
     * Callable that wraps creating a Folder so we can create many quickly
     */
    private static class CreateFolder implements Callable<TreeFolder> {
        private StorageProvider provider = null;
        private String path = null;
        public CreateFolder(StorageProvider provider, String path) {
            this.provider = provider;
            this.path = path;
        }
        @Override
        public TreeFolder call() throws Exception {
            return provider.createFolder(Paths.get(path));
        }
    }

    /**
     * Test metadata used in some of the tests
     */
    private static final Map<String, Object> TEST_METADATA = newTestMetadata();

    /**
     * These paths are created in AWS S3 for this unit test
     */
    static final String[] demoPaths = new String[] {
        "unittest/",
        "unittest/n1_1/",
        "unittest/n1_1/n2_1/",
        "unittest/n1_1/n2_1/corn.txt",
        "unittest/n1_1/n2_1/eagles_eggs.txt",
        "unittest/n1_1/n2_1/n3_1/",
        "unittest/n1_1/n2_1/n3_1/hooray_nacho.xlsx",
        "unittest/n1_1/n2_1/n3_1/ouch.docx",
        "unittest/n1_1/n2_1/n3_2/",
        "unittest/n1_1/n2_1/n3_2/n4_1/",
        "unittest/n1_1/n2_1/n3_2/n4_1/corn.txt",
        "unittest/n1_1/n2_1/n3_2/n4_1/encarnacion.txt",
        "unittest/n1_1/n2_1/n3_2/n4_1/ramses.txt",
        "unittest/n1_1/n2_1/n3_2/n4_2/",
        "unittest/n1_1/n2_1/n3_2/n4_2/eagles_eggs.txt",
        "unittest/n1_1/n2_1/n3_2/nacho_hero.jpg",
        "unittest/n1_1/n2_1/nacho_libre.zip",
        "unittest/n1_1/n2_2/",
        "unittest/n1_1/n2_2/n3_1/",
        "unittest/n1_1/n2_2/n3_1/nacho_fight.pptx",
        "unittest/n1_1/n2_2/n3_2/",
        "unittest/n1_1/n2_2/n3_2/n4_1/",
        "unittest/n1_1/n2_2/n3_2/n4_1/hooray_nacho.xlsx",
        "unittest/n1_1/n2_2/n3_2/n4_1/nacho_libre.pdf",
        "unittest/n1_1/n2_2/n3_2/n4_1/ouch.docx",
        "unittest/n1_1/n2_2/n3_2/n4_2/",
        "unittest/n1_1/n2_2/n3_2/n4_2/corn.txt",
        "unittest/n1_1/n2_2/n3_2/n4_2/ouch.docx",
        "unittest/n1_1/n2_2/n3_2/n4_2/religious_man.txt",
        "unittest/n1_1/n2_2/n3_2/nacho_eggs.png",
        "unittest/n1_1/n2_2/n3_2/religious_man.txt",
        "unittest/n1_1/n2_2/n3_3/",
        "unittest/n1_1/n2_2/n3_3/n4_1/",
        "unittest/n1_1/n2_2/n3_3/n4_1/nacho_eggs.png",
        "unittest/n1_1/n2_2/n3_3/n4_1/nacho_libre.pdf",
        "unittest/n1_1/n2_2/n3_3/n4_1/ouch.docx",
        "unittest/n1_1/n2_2/n3_3/n4_1/religious_man.txt",
        "unittest/n1_1/n2_2/n3_3/nacho_eggs.png",
        "unittest/n1_1/n2_2/n3_3/nacho_libre.pdf",
        "unittest/n1_1/n2_2/nacho_fight.pptx",
        "unittest/n1_1/nacho_eggs.png",
        "unittest/n1_1/nacho_fight.pptx",
        "unittest/n1_1/nacho_libre.pdf",
        "unittest/n1_2/",
        "unittest/n1_2/n2_1/",
        "unittest/n1_2/n2_2/",
        "unittest/n1_3/",
        "unittest/nacho_libre.pdf",
    };

    /**
     * S3 Storage Provider implementation
     */
    private StorageProvider provider = null;

    /**
     * ThreadPooling support for this test
     */
    private ExecutorService executor = null;

    /**
     * Create this test and init the StorageProvider we'll be using
     */
    public S3StorageProviderTest() {
        provider = S3StorageProvider.create("treefs");
        executor = Executors.newFixedThreadPool(13);

    }

    /**
     * Create folders in S3 for each folder listed in the demoPaths array given above.
     * Folders are specified with a '/' on the end.
     *
     */
    @Test
    public void testCreateDemoFolders() {

        List<Future<TreeFolder>> folders = new ArrayList<>();

        try {
            List<String> paths = new ArrayList<>();
            for(String path : Arrays.asList(demoPaths)) {
                if(path.endsWith("/")) {
                    System.out.println("creating path: " + path);
                    paths.add(StringUtils.removeEnd(path, "/"));
                    CreateFolder callable = new CreateFolder(provider, path);
                    Future<TreeFolder> submitted = executor.submit(callable);
                    folders.add(submitted);
                }
            }

            // Retrieve the results by synchronizing on get() call.
            for (Future<TreeFolder> future : folders) {
                TreeFolder folder = future.get();
                paths.remove(StringUtils.unixPath(folder.path().toString()));
            }

            // if we created all folders then this list shouldn't contain anything
            Assert.assertEquals(0L, paths.size());

        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testCreateFolders() {

        try {
            TreeFolder folder = provider.createFolder(
                Paths.get("unittest/testCreateFolders/n1_1"));
            Assert.assertEquals("n1_1", folder.name());

            folder = provider.createFolder(
                Paths.get("unittest/testCreateFolders/n1_1/n2_1"),
                TEST_METADATA);
            Assert.assertEquals("n2_1", folder.name());

            Map<String, Object> metadata = provider.readMetadata(
                Paths.get("unittest/testCreateFolders/n1_1/n2_1"));
            Assert.assertEquals(TEST_METADATA, metadata);

            folder = provider.createFolder(
                Paths.get("unittest/testCreateFolders/n1_2"));
            Assert.assertEquals("n1_2", folder.name());

            folder = provider.createFolder(
                Paths.get("unittest/testCreateFolders/n1_3"));
            Assert.assertEquals("n1_3", folder.name());

            // trash all paths created by this test
            provider.trash(Paths.get("unittest/testCreateFolders"), true);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateFile() {

        InputStream inputStream = null;
        try {
            try {
                provider.trash(Paths.get("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf"));
            } catch(NoTreePathException ex) { /* ignore */ }
            inputStream = getInputStream(NACHO_TEST_FILE);
            TreeFile file = provider.createFile(
                Paths.get("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf"),
                inputStream,
                TEST_METADATA);
            Assert.assertEquals("nacho_libre.pdf", file.name());

        } catch(TreeFileExistsException ex) {
            System.out.println(StringUtils.unixPath(ex.path().toString()) + " already exists.");
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        } finally {
            closeQuietly(inputStream);
        }
    }

    @Test
    public void testFileDownload() throws StorageException {

        String uploadFile   = "src/test/resources/data/encarnacion.txt";
        String s3File       = "unittest/testFileDownload/encarnacion.txt";
        String downloadFile = "downloads/testFileDownload/encarnacion.txt";
        try {
            testCreateFile();
            upload(uploadFile, s3File);
            InputStream input = provider.read(Paths.get(s3File));
            Files.createDirectories(Paths.get("downloads/testFileDownload"));
            IOUtils.copy(input, Paths.get(downloadFile));
            String uploadSha1   = FileUtils.sha1(new File(uploadFile));
            String downloadSha1 = FileUtils.sha1(new File(downloadFile));
            if(!uploadSha1.equals(downloadSha1)) {
                String message = uploadFile + " - sha1: " + uploadSha1 + "\n"
                    + downloadFile + " - sha1: " + downloadSha1 + "\n"
                    + "are not equal and should be #epicfail.";
                Assert.fail(message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Exception caught when attempting to download: " + s3File);
        }
    }

    @Test
    public void testFileDownloadToFile() throws StorageException {

        String downloadTarget = "unittest/n1_1/n2_1/n3_1/nacho_libre.pdf";

        try {
            testCreateFile();
            InputStream input = provider.read(Paths.get(downloadTarget));
            Files.createDirectories(Paths.get("downloads/testDownloadingFile"));
            IOUtils.copy(input, Paths.get("downloads/testDownloadingFile/nacho_libre.pdf"));
            // should do some sort of comparison between the uploaded file and the downloaded file
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Exception caught when attempting to download: " + downloadTarget);
        }

    }

    @Test
    public void testReadFolderAndFileInfo() throws StorageException {
        TreeFolder folder = provider.openFolder(Paths.get("unittest/n1_1/"), 3);
        Assert.assertEquals("n1_1", folder.name());
        Assert.assertEquals("unittest/n1_1", StringUtils.unixPath(folder.path().toString()));

        TreeFolder f21 = (TreeFolder)folder.items().get(0);
        Assert.assertEquals("n2_1", f21.name());
        Assert.assertEquals("unittest/n1_1/n2_1", StringUtils.unixPath(f21.path().toString()));

        TreeFolder f2131 = (TreeFolder)f21.items().get(0);
        Assert.assertEquals("n3_1", f2131.name());
        Assert.assertEquals("unittest/n1_1/n2_1/n3_1", StringUtils.unixPath(f2131.path().toString()));

        TreeFile f2131_nacho_libre_pdf = (TreeFile)f2131.items().get(0);
        Assert.assertEquals("nacho_libre.pdf", f2131_nacho_libre_pdf.name());
        Assert.assertEquals("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf",
            StringUtils.unixPath(f2131_nacho_libre_pdf.path().toString()));

        TreeFolder f2132 = (TreeFolder)f21.items().get(1);
        Assert.assertEquals("n3_2", f2132.name());
        Assert.assertEquals("unittest/n1_1/n2_1/n3_2", StringUtils.unixPath(f2132.path().toString()));

        TreeFolder f213241 = (TreeFolder)f2132.items().get(0);
        Assert.assertEquals("n4_1", f213241.name());
        Assert.assertEquals("unittest/n1_1/n2_1/n3_2/n4_1", StringUtils.unixPath(f213241.path().toString()));

        TreeFolder f213242 = (TreeFolder)f2132.items().get(1);
        Assert.assertEquals("n4_2", f213242.name());
        Assert.assertEquals("unittest/n1_1/n2_1/n3_2/n4_2", StringUtils.unixPath(f213242.path().toString()));

        TreeFolder f22 = (TreeFolder)folder.items().get(1);
        Assert.assertEquals("n2_2", f22.name());
        Assert.assertEquals("unittest/n1_1/n2_2", StringUtils.unixPath(f22.path().toString()));
    }

    @Test
    public void testS3Exists() throws StorageException {
        Assert.assertTrue(provider.exists(Paths.get("unittest/n1_1/n2_1/n3_1/")));
        Assert.assertTrue(provider.exists(Paths.get("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf")));

        Assert.assertTrue(provider.exists(Paths.get("unittest/n1_1/n2_1/n3_1")));
        Assert.assertTrue(provider.exists(Paths.get("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf/")));
    }

    @Test
    public void testIsFolder() throws StorageException {
        Assert.assertTrue(provider.isFolder(Paths.get("unittest/n1_1/n2_1/n3_1/")));
    }

    @Test
    public void testIsFile() throws StorageException {
        Assert.assertTrue(provider.isFile(Paths.get("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf")));
    }

    @Test
    public void testIsEmpty() throws StorageException {
        Assert.assertFalse(provider.isEmpty(Paths.get("unittest/n1_1/n2_1/n3_1")));
        Assert.assertTrue(provider.isEmpty(Paths.get("unittest/n1_1/n2_1/n3_2/n4_1")));
    }

    @Test
    public void testReadMetadata() throws StorageException {

        testCreateFile();
        Map<String, Object> metadata = provider.readMetadata(Paths.get("unittest/n1_1/n2_1/n3_1/nacho_libre.pdf"));
        Assert.assertEquals(TEST_METADATA, metadata);
    }

    /**
     * TODO left off here on 06/24/2014 - not working, needs recursive ops performed on S3
     * @throws StorageException
     */
    @Test
    public void testTrashFolder() throws StorageException, IOException {

        TreeFolder sFolder = provider.createFolder(Paths.get("unittest/a/b/c/d/e/f/g"));
        Assert.assertEquals("g", sFolder.name());

        upload("src/test/resources/data/encarnacion.txt", "unittest/a/encarnacion.txt");
        upload("src/test/resources/data/encarnacion.txt", "unittest/a/b/encarnacion.txt");
        upload("src/test/resources/data/encarnacion.txt", "unittest/a/b/c/encarnacion.txt");
        upload("src/test/resources/data/encarnacion.txt", "unittest/a/b/c/d/encarnacion.txt");
        upload("src/test/resources/data/encarnacion.txt", "unittest/a/b/c/d/e/encarnacion.txt");
        upload("src/test/resources/data/encarnacion.txt", "unittest/a/b/c/d/e/f/encarnacion.txt");
        upload("src/test/resources/data/encarnacion.txt", "unittest/a/b/c/d/e/f/g/encarnacion.txt");

        TreeFolder a = provider.openFolder(Paths.get("unittest/a"));
        Assert.assertEquals("a", a.name());
        TreeFolder b = provider.openFolder(Paths.get("unittest/a/b"));
        Assert.assertEquals("b", b.name());
        TreeFolder c = provider.openFolder(Paths.get("unittest/a/b/c"));
        Assert.assertEquals("c", c.name());
        TreeFolder d = provider.openFolder(Paths.get("unittest/a/b/c/d"));
        Assert.assertEquals("d", d.name());
        TreeFolder e = provider.openFolder(Paths.get("unittest/a/b/c/d/e"));
        Assert.assertEquals("e", e.name());
        TreeFolder f = provider.openFolder(Paths.get("unittest/a/b/c/d/e/f"));
        Assert.assertEquals("f", f.name());
        TreeFolder g = provider.openFolder(Paths.get("unittest/a/b/c/d/e/f/g"));
        Assert.assertEquals("g", g.name());

        // attempt to trash a folder that's not empty
        try {
            provider.trash(Paths.get("unittest/a"));
            Assert.fail("folder: unittest/a IS NOT EMPTY so call to " +
                "trash() should raise TreeFolderNotEmptyException.");
        } catch(TreeFolderNotEmptyException ex) {
            Assert.assertNotNull(ex);
        }

        // attempt to trash a file
        provider.trash(Paths.get("unittest/a/b/c/d/e/f/g/encarnacion.txt"));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b/c/d/e/f/g/encarnacion.txt")));
        // attempt to trash a now empty folder
        provider.trash(Paths.get("unittest/a/b/c/d/e/f/g"));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b/c/d/e/f/g")));

        // attempt to trash a folder that's not empty but set forceDelete flag to true
        provider.trash(Paths.get("unittest/a"), true);
        Assert.assertFalse(provider.exists(Paths.get("unittest/a")));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b")));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b/c")));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b/c/d")));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b/c/d/e")));
        Assert.assertFalse(provider.exists(Paths.get("unittest/a/b/c/d/e/f")));
    }

    /**
     * Return an InputStream to the file
     * @param file file name to return an InputStream for
     * @return the InputStream for the file name passed in
     */
    static InputStream getInputStream(String file) throws IOException {

        Path path = Paths.get(file);
        InputStream is = Files.newInputStream(path, StandardOpenOption.READ);
        if(isNull(is)) {
            throw new RuntimeException("couldn't obtain InputStream for: " + file);
        }

        return is;
    }

    /**
     * creates a constant map used to inject some metadata into select StorageProvider operations
     * @return
     */
    static final Map<String, Object> newTestMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("directedBy",  "Jared Hess");
        metadata.put("starring",    "Jack Black");
        metadata.put("title",       "Nacho Libre");
        metadata.put("releaseDate", "06/16/2006");
        metadata.put("runningTime", "92 minutes");
        metadata.put("budget",      "5 million");
        metadata.put("boxOffice",   "9 million");
        return metadata;
    }

    /**
     * Upload a test file
     * @param uploadFile
     * @param s3FilePath
     * @throws StorageException
     * @throws IOException
     */
     void upload(String uploadFile, String s3FilePath) throws StorageException, IOException {

        InputStream inputStream = null;
        try {
            inputStream = getInputStream(uploadFile);
            provider.createFile(
                Paths.get(s3FilePath),
                inputStream,
                TEST_METADATA);
        } catch(TreeFileExistsException ex) {
            System.out.println(StringUtils.unixPath(ex.path().toString()) + " already exists.");
        } finally {
            closeQuietly(inputStream);
        }
    }

}
