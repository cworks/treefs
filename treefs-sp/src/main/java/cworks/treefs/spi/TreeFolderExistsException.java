package cworks.treefs.spi;

import java.nio.file.Path;

public class TreeFolderExistsException extends TreePathExistsException {

    public TreeFolderExistsException(String message, Path folder) {
        super(message, folder);
    }

    public TreeFolderExistsException(Path folder) {
        super(folder);
    }
}
