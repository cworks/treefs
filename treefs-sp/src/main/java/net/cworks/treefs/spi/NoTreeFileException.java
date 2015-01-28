package net.cworks.treefs.spi;

import java.nio.file.Path;

public class NoTreeFileException extends NoTreePathException {
    public NoTreeFileException(Path path) {
        super(path);
    }
}
