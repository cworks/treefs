package cworks.treefs.spi;

/**
 * Parent class of all StorageProvider exceptions.
 * More specific exceptions should extend this class
 */
public class StorageException extends Exception {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }
}
