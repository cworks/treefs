package net.cworks.treefs;

import java.nio.file.Path;

public class TreeFsPathExistsException extends TreeFsException {

    /**
     * Path that already exists
     */
    private Path path;

    public TreeFsPathExistsException(String message, Throwable cause, Path path) {
        super(message, cause);
        this.path = path;
    }

    public TreeFsPathExistsException(Throwable cause, Path path) {
        super(path.toString(), cause);
        this.path = path;
    }

    public String path() {
        return TreeFs.unixPath(path.toString());
    }

}
