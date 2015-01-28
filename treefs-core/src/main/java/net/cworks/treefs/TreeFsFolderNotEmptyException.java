package net.cworks.treefs;

public class TreeFsFolderNotEmptyException extends TreeFsException {

    public TreeFsFolderNotEmptyException(Throwable cause) {
        super(cause);
    }

    public TreeFsFolderNotEmptyException(String message) {
        super(message);
    }

    public TreeFsFolderNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
