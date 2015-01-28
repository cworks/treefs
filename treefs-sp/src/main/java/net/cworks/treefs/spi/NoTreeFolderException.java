package net.cworks.treefs.spi;

import java.nio.file.Path;

public class NoTreeFolderException extends NoTreePathException {

    public NoTreeFolderException(Path path) {
        super(path);
    }
}
