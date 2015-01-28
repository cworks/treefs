package net.cworks.treefs.client;

import net.cworks.http.Http;
import net.cworks.http.HttpGetBuilder;
import net.cworks.http.HttpPostBuilder;
import net.cworks.http.HttpRequestBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

public class TreeFsClient {

    /**
     * Version of this TreeFsClient
     */
    public static final String VERSION = "1.0.0";

    /**
     * Default character set
     */
    private static final String CHARSET = "UTF-8";

    /**
     * TreeFsClient is baked with Json Goodness
     */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * Default HTTP connection timeout
     */
    public static final int CONNECTION_TIMEOUT = 60000;

    /**
     * Default timeout to use for requests to TreeFs
     */
    public static final int READ_TIMEOUT = 60000;

    /**
     * Number of times a request is tried before calling it quits
     */
    private static int RETRY_LIMIT = 3;

    /**
     * Default endpoint
     */
    private static final String defaultEndpoint = "http://localhost:4444";

    /**
     * Actual endpoint in use
     */
    private String endpoint;

    /**
     * TreeFs Account id
     */
    private String accountId;

    /**
     * Authorization Token
     */
    private String authToken;

    /**
     * TreeFs Account wrapper
     */
    private Account account;

    /**
     * Apache http-client instance this class delegates to
     */
    private HttpClient httpClient;

    /**
     * Create RestClient with default params
     * @return
     */
    public static TreeFsClient create() {
        TreeFsClient client = new TreeFsClient("", "");
        return client;
    }

    /**
     * create a RESTful client
     * @param accountSid
     * @param authToken
     * @return
     */
    public static TreeFsClient create(String accountSid, String authToken) {
        TreeFsClient client = new TreeFsClient(accountSid, authToken);
        return client;
    }

    /**
     * Create this RestClient with an accountId and an authToken
     * @param accountId
     * @param authToken
     */
    TreeFsClient(String accountId, String authToken) {
        this(accountId, authToken, defaultEndpoint);
    }

    /**
     * Create this RestClient with an accountId and an authToken and endpoint (i.e. URL)
     * @param accountId
     * @param authToken
     * @param endpoint
     */
    TreeFsClient(String accountId, String authToken, String endpoint) {

        validateAccountSid(accountId);
        validateAuthToken(authToken);

        accountId(accountId);
        authToken(authToken);

        if((endpoint != null) && (!endpoint.equals(""))) {
            endpoint(endpoint);
        }

        HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        HttpClient client = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig())
            .setConnectionManager(connectionManager)
            .build();
        httpClient(client);

        this.account = new Account(this);
        this.account.sid(this.accountId);
        this.account.authToken(this.authToken);
    }

    /**
     * Creates a builder object for a POST-request.
     *
     * @param url the URL to use for this request.
     * @return the builder object for this URL.
     */
    public HttpPostBuilder post(final String url) {
        HttpPostBuilder builder = Http.post(getUrl(url));
        builder.use(httpClient);
        init(builder);
        return builder;
    }

    /**
     * Creates a builder object for a GET-request.
     *
     * @param url the URL to use for this request.
     * @return the builder object for the this URL.
     */
    public HttpGetBuilder get(final String url) {
        HttpGetBuilder builder = Http.get(getUrl(url));
        builder.use(httpClient);
        init(builder);
        return builder;
    }

    /**
     * Put TreeFs specific headers into the request
     * @param builder
     */
    void init(HttpRequestBuilder builder) {
        builder.header(new BasicHeader("treefs-client", accountId))
            .header(new BasicHeader("AuthToken", authToken))
            .header(new BasicHeader("User-Agent", "treefs/" + VERSION))
            .header(new BasicHeader("Accept-Charset", CHARSET))
            .header(new BasicHeader("Accept", APPLICATION_JSON))
            .header(new BasicHeader("Content-Type", APPLICATION_JSON));
    }

    /**
     * Tidy up the Url
     * @param uri
     * @return
     */
    String getUrl(String uri) {
        String path = uri.toLowerCase();
        StringBuilder sb = new StringBuilder();
        if(path.startsWith("http://") || path.startsWith("https://")) {
            sb.append(path);
        } else {
            sb.append(endpoint());
            if(!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
        }

        uri = sb.toString();
        return uri;
    }

    void endpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    String endpoint() {
        return this.endpoint;
    }

    void accountId(String accountId) {
        this.accountId = accountId;
    }

    void authToken(String authToken) {
        this.authToken = authToken;
    }

    //
    // private methods
    //

    private RequestConfig requestConfig() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setConnectionRequestTimeout(READ_TIMEOUT)
            .build();
        return config;
    }

    private void httpClient(HttpClient client) {
        this.httpClient = client;
    }

    private void validateAccountSid(String accountSid) {

    }

    private void validateAuthToken(String authToken) {

    }



}
