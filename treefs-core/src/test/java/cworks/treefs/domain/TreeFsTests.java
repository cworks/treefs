package cworks.treefs.domain;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TreeFsTests {
    static void writeFile(String filename, String content) throws IOException {
        Files.write(Paths.get("src/test/resources/data/" + filename),
            content.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    static String readFile(String filename) throws IOException {
        String content = FileUtils.readFileToString(new File("src/test/resources/data/" + filename));
        return content;

    }
}
