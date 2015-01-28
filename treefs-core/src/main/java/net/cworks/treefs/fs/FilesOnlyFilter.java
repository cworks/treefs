package net.cworks.treefs.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesOnlyFilter implements DirectoryStream.Filter<Path> {

    @Override
    public boolean accept(Path path) throws IOException {
        return Files.isRegularFile(path);
    }

}
