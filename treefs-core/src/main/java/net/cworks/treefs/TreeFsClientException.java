package net.cworks.treefs;

public class TreeFsClientException extends RuntimeException {

    /**
     * Creates a new CSPClientException with the specified message, and root cause.
     * @param message An error message describing why this exception was thrown.
     * @param t The underlying cause of this exception.
     */
    public TreeFsClientException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * Creates a new CSPClientException with the specified message.
     * @param message An error message describing why this exception was thrown.
     */
    public TreeFsClientException(String message) {
        super(message);
    }
}
