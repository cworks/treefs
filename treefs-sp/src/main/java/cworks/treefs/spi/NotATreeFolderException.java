package cworks.treefs.spi;

import java.nio.file.Path;

public class NotATreeFolderException extends StorageException {

    private Path folder = null;

    public NotATreeFolderException(String message) {
        super(message);
    }

    public NotATreeFolderException(Path notAFolder) {
        super(notAFolder.toString());
        this.folder = notAFolder;
    }

    public Path folder() {
        return this.folder;
    }
}
