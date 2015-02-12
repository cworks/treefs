package cworks.treefs;

public class TreeFsClient {
    String clientId = null;
    public TreeFsClient(String clientId) {
        this.clientId = clientId;
    }

    public String id() {
        return clientId;
    }
}
