package cworks.treefs.client;

public interface Config {
    public String protocol();
    public int port();
    public String fileSystem();
    public String host();
    public String accountId();
    public String authToken();
}
