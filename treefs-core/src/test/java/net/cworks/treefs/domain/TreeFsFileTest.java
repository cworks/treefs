package net.cworks.treefs.domain;

import net.cworks.treefs.common.dt.ISO8601DateParser;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;

public class TreeFsFileTest {

    @Test
    public void createTreeFsFileExamples() throws IOException, ParseException {

        TreeFsFile emptyFile = TreeFsFactory.file().make();
        TreeFsTests.writeFile("emptyFile.json", emptyFile.toString());

        Date createdAt = ISO8601DateParser.parse("2014-01-14T20:59:02+00:00");

        TreeFsFile file = TreeFsFactory.file()
            .withName("corbetts-xray.jpg")
            .withPath("documents/images")
            .withDescription("Corbett's brain...he has none")
            .withCreatedAt(createdAt)
            .withCreatedBy("Dr. Who")
            .withMetadata("patientId", "corbett123")
            .withMetadata("xray-side", "lefthand side of head")
            .withSha1("cCkuLEibnbcLhiv3OBGl")
            .withSize(10000).make();

        TreeFsTests.writeFile("fileWithAttr.json", file.toString());

        TreeFsFile fileWithNameAndPath = TreeFsFactory.file("brain.png", Paths.get("documents/pics")).make();
        TreeFsTests.writeFile("fileWithNameAndPath.json", fileWithNameAndPath.toString());

    }
}
