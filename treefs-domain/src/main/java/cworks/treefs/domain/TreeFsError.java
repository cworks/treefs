package cworks.treefs.domain;

public class TreeFsError extends TreeFsObject {
    public TreeFsError(String errorMessage) {
        put("message", errorMessage);
    }
}
