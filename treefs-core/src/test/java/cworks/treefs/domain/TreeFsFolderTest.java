package cworks.treefs.domain;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.common.dt.ISO8601DateParser;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TreeFsFolderTest {

    @Test
    public void testWriteTreeFsFolder() throws IOException {
        String folderJson = FileUtils.readFileToString(new File("src/test/resources/data/folder.json"));
        TreeFsFolder folder = Json.asObject(folderJson, TreeFsFolder.class);
        FileUtils.writeStringToFile(File.createTempFile("folder-copy", ".json"), Json.asString(folder));
    }

    @Test
    public void decodeTreeFsFolder() throws IOException {
        String folderJson = FileUtils.readFileToString(new File("src/test/resources/data/encodeTreeFsFolder.json"));
        TreeFsFolder folder = Json.asObject(folderJson, TreeFsFolder.class);
        System.out.println("folder.name=" + folder.name());
        System.out.println("folder.createdAt=" + folder.createdAt());
        System.out.println("folder.createdBy=" + folder.createdBy());
        System.out.println("folder.description=" + folder.description());
        Map<String, Object> metadata = folder.metadata();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            System.out.println("folder.metadata." + entry.getKey() + "=" + entry.getValue());
        }

    }

    @Test
    public void encodeTreeFsFolderFromMap() throws IOException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "mydocs");
        properties.put("createdAt", "2014-01-10 10:00");
        properties.put("path", "corbett/stuff");
        properties.put("parent", "stuff");
        properties.put("createdBy", "corbett");
        properties.put("description", "A folder for my docs");
        JsonObject obj = new JsonObject(properties);

        String jsonFolder = obj.asString();
        Files.write(Paths.get("src/test/resources/data/encodeTreeFsFolderFromMap.json"),
            jsonFolder.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    /**
     * Just plain ole Object creation patterns for TreeFsFolder
     */
    @Test
    public void createTreeFsFolderExamples() throws IOException {

        // Create a blank folder
        TreeFsFolder blankFolder = TreeFsFactory.folder().make();
        TreeFsTests.writeFile("blankFolder.json", blankFolder.toString());
        // Create a folder with some attributes
        TreeFsFolder folderWithAttr = TreeFsFactory.folder()
            .withName("folder3")
            .withDescription("A folder")
            .withPath("corbett/stuff")
            .withCreatedBy("corbett")
            .withCreatedAt(new Date())
            .withMetadata("patientId", "corbett123")
            .withMetadata("address", "641 Post Oak Dr. Hurst Texas, 76053")
            .addPath(Paths.get("docs/foo")).addPath(Paths.get("docs/reports/bar")).make();
        TreeFsTests.writeFile("folderWithAttr.json", folderWithAttr.toString());

        TreeFsFolder folderWithNameAndPath = TreeFsFactory.folder("A Name", Paths.get("a/path")).make();
        TreeFsTests.writeFile("folderWithNameAndPath.json", folderWithNameAndPath.toString());

        TreeFsFolder folderWithNameAndPathAndDesc =  TreeFsFactory.folder(
                "A Name", Paths.get("a/path"), "A Description").make();
        TreeFsTests.writeFile("folderWothNameAndPathAndDesc.json", folderWithNameAndPathAndDesc.toString());
    }

    /**
     * Serialize TreeFsFolder to a string
     */
    @Test
    public void serializeFolderTest() throws IOException, ParseException {
        Date createdAt = ISO8601DateParser.parse("2014-01-14T20:59:02+00:00");
        // Create a folder with some attributes
        TreeFsFolder folder = TreeFsFactory.folder()
            .withName("folder3")
            .withDescription("A folder")
            .withPath("corbett/stuff")
            .withCreatedBy("corbett")
            .withCreatedAt(createdAt)
            .withMetadata("patientId", "corbett123")
            .withMetadata("address", "641 Post Oak Dr. Hurst Texas, 76053").make();

        // Create a folder from a serialized version of it
        String text = TreeFsFactory.serializer().folder(folder);
        TreeFsTests.writeFile("serializeFolderTest.json", text);
    }

    /**
     * DeSerialize some text into a TreeFsFolder instance
     */
    @Test
    public void deserializeFolderText() throws IOException {

        String content = TreeFsTests.readFile("serializeFolderTest-withExtraCrap.json");
        TreeFsFolder folder = TreeFsFactory.deserializer().folder(content);
        Assert.assertEquals("folder3", folder.name());
        Assert.assertEquals("folder", folder.type());
        Assert.assertEquals("corbett/stuff", folder.path());
        Assert.assertEquals("A folder", folder.description());
        Assert.assertEquals("corbett", folder.createdBy());
        Assert.assertEquals("2014-01-14T20:59:02+00:00", folder.createdAt());
        Assert.assertEquals("corbett123", folder.metadata("patientId"));
        Assert.assertEquals("641 Post Oak Dr. Hurst Texas, 76053", folder.metadata("address"));
    }
}
























