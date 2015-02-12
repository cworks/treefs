package cworks.treefs;

public class TreeFsException extends RuntimeException {

    public TreeFsException(Throwable cause) {
        super(cause);
    }

    public TreeFsException(String message) {
        super(message);
    }

    public TreeFsException(String message, Throwable cause) {
        super(message, cause);
    }
}
