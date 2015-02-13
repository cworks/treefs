package cworks.treefs;

public class TreeFsClient {

    private String clientId = null;
    private boolean enabled = false;

    public TreeFsClient(String clientId) {
        this.clientId = clientId;
    }

    public TreeFsClient(String clientId, boolean enabled) {
        this.clientId = clientId;
        this.enabled = enabled;
    }

    public String id() {
        return this.clientId;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public String toString() {
        return "clientId: " + clientId + " enabled: " + enabled;
    }

    public boolean equals(Object object) {
        if(!(object instanceof TreeFsClient)) {
            return false;
        }

        if(object == this) {
            return true;
        }

        TreeFsClient other = (TreeFsClient)object;
        return other.id().equals(this.id());
    }
}
