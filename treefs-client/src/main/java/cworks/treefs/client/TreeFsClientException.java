package cworks.treefs.client;

public class TreeFsClientException extends RuntimeException {

    public TreeFsClientException(String message, Throwable t) {
        super(message, t);
    }

    public TreeFsClientException(String message) {
        super(message);
    }
}
