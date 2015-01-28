package net.cworks.treefs.client;

import net.cworks.treefs.client.auth.ClasspathConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class TreeFs {

    /**
     * Create TreeFs Context with default Config
     * @return
     */
    public static Context create() {
        // read config from classpath
        Config config = classpathConfig();
        return new Context(config);
    }

    /**
     * Create a TreeFs Context with the provided Config instance
     * @param config
     * @return
     */
    public static Context create(Config config) {
        if(config == null) {
            throw new IllegalArgumentException("Config can't be null silly goose.");
        }
        return new Context(config);
    }

    /**
     * Create a TreeFs Context to the provided endpoint create the default Config
     * @param endpoint
     * @return
     */
    public static Context create(String endpoint) {
        if(endpoint == null || endpoint.trim().length() < 1) {
            throw new IllegalArgumentException(
                "Endpoint cannot be null or empty, that's cray cray.");
        }

        final Map<String, String> ep = parseEndpoint(endpoint);
        // first load defaults from classpath
        final Config defaultConfig = classpathConfig();
        // next override the host
        Config config = new Config() {
            @Override
            public String protocol() {
                return (
                    ep.containsKey("protocol") == false ?
                        defaultConfig.protocol() : ep.get("protocol")
                );
            }
            @Override
            public int port() { return defaultConfig.port(); }
            @Override
            public String fileSystem() { return defaultConfig.fileSystem(); }
            @Override
            public String host() {
                return (
                    ep.containsKey("endpoint") == false ?
                        defaultConfig.host() : ep.get("endpoint")
                );
            }
            @Override
            public String accountId() { return defaultConfig.accountId(); }
            @Override
            public String authToken() { return defaultConfig.authToken(); }
        };

        return new Context(config);
    }

    /**
     * Create a TreeFs Context to the provided endpoint and port
     * @param endpoint
     * @param port
     * @return
     */
    public static Context create(String endpoint, final int port) {

        if(endpoint == null || endpoint.trim().length() < 1) {
            throw new IllegalArgumentException(
                "Endpoint cannot be null or empty, that's cray cray.");
        }

        if(port < 1024) {
            throw new IllegalArgumentException("Port must be >= 1024.");
        }

        final Map<String, String> ep = parseEndpoint(endpoint);
        // first load defaults from classpath
        final Config defaultConfig = classpathConfig();
        // next override the host
        Config config = new Config() {
            @Override
            public String protocol() {
                return (
                    ep.containsKey("protocol") == false ?
                        defaultConfig.protocol() : ep.get("protocol")
                );
            }
            @Override
            public int port() {
                return port;
            }
            @Override
            public String fileSystem() { return defaultConfig.fileSystem(); }
            @Override
            public String host() {
                return (
                    ep.containsKey("endpoint") == false ?
                        defaultConfig.host() : ep.get("endpoint")
                );
            }
            @Override
            public String accountId() { return defaultConfig.accountId(); }
            @Override
            public String authToken() { return defaultConfig.authToken(); }
        };

        return new Context(config);
    }

    /**
     * Class utility that parses a uri string into protocol parts and endpoint parts.
     * @param endpoint
     * @return
     */
    private static Map<String, String> parseEndpoint(String endpoint) {
        Map<String, String> ep = new HashMap<String, String>();
        if(endpoint.startsWith("http://")) {
            ep.put("protocol", "http");
            ep.put("endpoint", endpoint.substring("http://".length()));
        } else if(endpoint.startsWith("https://")) {
            ep.put("protocol", "https");
            ep.put("endpoint", endpoint.substring("https://".length()));
        } else {
            ep.put("endpoint", endpoint);
        }
        return ep;
    }

    /**
     * load default config from classpath
     * @return
     */
    private static Config classpathConfig() {
        Config config = null;
        try {
            config = ClasspathConfig.load();
        } catch (IOException ex) {
            throw new TreeFsClientException("Error loading default config from classpath.", ex);
        }
        return config;
    }


}
