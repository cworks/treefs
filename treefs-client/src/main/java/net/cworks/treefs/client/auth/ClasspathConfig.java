package net.cworks.treefs.client.auth;

import net.cworks.treefs.client.Config;
import net.cworks.treefs.client.TreeFsClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Config class that looks into the Classpath for treefsclient.properties to load
 * and expose to TreeFs client code.
 *
 */
public class ClasspathConfig implements Config {

    /**
     * default name for treefs client property file
     */
    private static final String DefaultConfigurationFile = "treefsclient.properties";

    /**
     * Properties from configuration file used
     */
    private Properties properties;

    /**
     * Create ClasspathConfig from default config file in classpath
     * @return
     * @throws IOException
     */
    public static ClasspathConfig load() throws IOException {

        return load(DefaultConfigurationFile);
    }

    /**
     * Create ClasspathConfig from custom config file in classpath
     * @param configurationFile
     * @return
     * @throws IOException
     */
    public static ClasspathConfig load(String configurationFile) throws IOException {
        String file = null;
        if(configurationFile == null) {
            throw new IllegalArgumentException(
                    "custom treefs configuration file can't be null...cray cray");
        }

        // configurationFile needs to start with slash or loading from classpath will freak
        if(!configurationFile.startsWith("/")) {
            file = "/" + configurationFile;
        } else {
            file = configurationFile;
        }

        InputStream inputStream = ClasspathConfig.class.getResourceAsStream(file);
        if (inputStream == null) {
            throw new TreeFsClientException("Can't load treefs configuration file: "
                + configurationFile + " from classpath!");
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } finally {
            try {inputStream.close();} catch (Exception ex) {}
        }

        return new ClasspathConfig(properties);
    }

    /**
     * Create Config instance from properties instance
     */
    private ClasspathConfig(Properties properties) {
        this.properties = properties;
    }

    /**
     * Communication protocol, either http or https, http by default
     * @return
     */
    @Override
    public String protocol() {
        String protocol = properties.getProperty("treefs.protocol", "http");
        return protocol;
    }

    /**
     * Communication port, default is 4444
     * @return
     */
    @Override
    public int port() {
        int port = Integer.parseInt(properties.getProperty("treefs.port", "4444"));
        return port;
    }

    /**
     * FileSystem to use, "default" is...you guessed it...the default
     * @return
     */
    @Override
    public String fileSystem() {
        String fs = properties.getProperty("treefs.fs", "default");
        return fs;
    }

    /**
     * host, default is localhost
     * @return
     */
    @Override
    public String host() {
        String host = properties.getProperty("treefs.host", "127.0.0.1");
        return host;
    }

    /**
     * return accountId
     * @return
     */
    @Override
    public String accountId() {
        String accountSid = properties.getProperty("treefs.accountId", "corbofett");
        return accountSid;
    }

    /**
     * return authToken
     * @return
     */
    @Override
    public String authToken() {
        String authToken = properties.getProperty("treefs.authToken", "onefishtwofishredfishbluefish");
        return authToken;
    }

}
