package net.cworks.treefs.spi;

import java.nio.file.Path;

public class TreePathExistsException extends StorageException {

    private Path path = null;

    public TreePathExistsException(String message, Path path) {
        super(message);
        this.path = path;
    }

    public TreePathExistsException(Path path) {
        super(path.toString());
        this.path = path;
    }

    public Path path() {
        return this.path;
    }
}
