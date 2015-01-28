package net.cworks.treefs.spi;

import java.nio.file.Path;

public class TreeFileExistsException extends TreePathExistsException {

    public TreeFileExistsException(String message, Path folder) {
        super(message, folder);
    }

    public TreeFileExistsException(Path folder) {
        super(folder);
    }
}
