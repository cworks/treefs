package cworks.treefs.syssp;

import cworks.json.Json;
import cworks.json.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static cworks.treefs.common.ObjectUtils.isNull;
import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;

public class SystemPathIO {
    /**
     * default file-IO buffer size for read and write ops
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * metadata file suffix for directories/folders
     */
    private static final String FOLDER_METADATA_SUFFIX = ".d";

    /**
     * metadata file suffix for files
     */
    private static final String FILE_METADATA_SUFFIX = ".f";

    /**
     * default character set used by this class
     */
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * Read a metadata file into a Map instance
     * @param path
     * @return
     * @throws IOException
     */
    static Map<String, Object> readMetadata(Path path) throws IOException {
        SystemPath sp = readSystemPath(path);
        if(isNull(sp)) {
            return null;
        }

        return sp.metadata();
    }

    static SystemPath readSystemPath(Path path) throws IOException {
        SystemPath systemPath = null;
        if(Files.isDirectory(path)) {
            systemPath = readSystemFolder(path);
        } else if(Files.isRegularFile(path)) {
            systemPath = readSystemFile(path);
        }
        if(isNull(systemPath)) {
            throw new IOException("Don't be crazy there is no content for: " + path);
        }
        return systemPath;
    }

    static SystemFolder readSystemFolder(Path path) throws IOException {
        String content = FileUtils.readFileToString(
            new File(path.toFile(), path.getFileName().toString() + FOLDER_METADATA_SUFFIX),
            DEFAULT_CHARSET);
        if(isNullOrEmpty(content)) {
            throw new IOException("Don't be crazy there is no content for folder: " + path);
        }
        SystemFolder sf = Json.asObject(content, SystemFolder.class);
        return sf;
    }

    static SystemFile readSystemFile(Path path) throws IOException {
        String content = FileUtils.readFileToString(
            new File(path.getParent().toFile(), path.getFileName().toString() + FILE_METADATA_SUFFIX),
            DEFAULT_CHARSET);
        if(isNullOrEmpty(content)) {
            throw new IOException("Don't be crazy there is no content for folder: " + path);
        }
        SystemFile sf = Json.asObject(content, SystemFile.class);
        return sf;
    }

    static void createSystemFolder(SystemFolder systemFolder) throws IOException {
        String encoded = Json.asString(systemFolder);
        Path metadataFile = Paths.get(systemFolder.fullPath().toString()
                + File.separator
                + systemFolder.fullPath().getFileName().toString() + FOLDER_METADATA_SUFFIX);
        Files.write(metadataFile, encoded.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE);
    }

    public static void createMetadata(SystemFile systemFile) throws IOException {
        String encoded = Json.asString(systemFile);
        Path metadataFile  = Paths.get(systemFile.fullPath().toString() + FILE_METADATA_SUFFIX);
        Files.write(metadataFile, encoded.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE);
    }

    /**
     * Test if a path contains a metadata file
     * @param path
     * @return
     */
    public static boolean hasMetadata(Path path) {
        boolean exists = false;
        try {
            Path target = null;
            if(Files.isDirectory(path)) {
                target = Paths.get(path.toString() + File.separator
                    + path.getFileName().toString()
                    + FOLDER_METADATA_SUFFIX);
            } else if(Files.isRegularFile(path)) {
                target = Paths.get(path.toString() + FILE_METADATA_SUFFIX);
            }
            if(target == null) {
                return exists;
            }
            exists = Files.exists(target);
        } catch(Exception ex) {
            exists = false;
        }
        return exists;
    }

    static JsonObject readJson(Path path) throws IOException {
        String content = FileUtils.readFileToString(path.toFile());
        if(isNullOrEmpty(content)) {
            throw new IOException("Don't be crazy there is no content for path: " + path);
        }
        JsonObject object = new JsonObject(content);
        return object;
    }

    public static void writeJson(Path file, JsonObject json) throws IOException {
        String content = json.asString();
        FileUtils.writeStringToFile(file.toFile(), content);
    }
}
