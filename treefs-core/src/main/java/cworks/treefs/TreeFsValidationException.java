package cworks.treefs;

public class TreeFsValidationException extends TreeFsException {
    public TreeFsValidationException(Throwable cause) {
        super(cause);
    }

    public TreeFsValidationException(String message) {
        super(message);
    }
}
