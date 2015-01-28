package net.cworks.treefs.spi;

import java.nio.file.Path;

public class TreeFolderNotEmptyException extends StorageException {

    private Path folder = null;

    public TreeFolderNotEmptyException(String message, Path folder) {
        super(message);
    }

    public TreeFolderNotEmptyException(String message, Throwable cause, Path folder) {
        super(message, cause);
    }

    public Path folder() {
        return this.folder;
    }
}
