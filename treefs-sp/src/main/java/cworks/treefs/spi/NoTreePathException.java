package cworks.treefs.spi;

import java.nio.file.Path;

public class NoTreePathException extends StorageException {
    private Path path = null;

    public NoTreePathException(String message) {
        super(message);
    }

    public NoTreePathException(Path path) {
        super(path.toString());
        this.path = path;
    }

    public Path path() {
        return this.path;
    }}
