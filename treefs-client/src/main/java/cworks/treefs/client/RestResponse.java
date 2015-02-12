package cworks.treefs.client;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestResponse {

    /**
     * Request URI
     */
    private String uri;

    /**
     * Http Header values
     */
    private Map<String, String> headers;

    /**
     * Does the response contain an error?
     */
    private boolean hasError;

    /**
     * Http Status Code
     */
    private int httpStatus;

    /**
     * Request Query String
     */
    private String queryString;

    /**
     * Payload of Response
     */
    private String responseBody;

    /**
     * Pattern to match uri so we can parcel it
     */
    private static Pattern uriPattern = Pattern.compile("([^?]+)\\??(.*)");

    /**
     * Create a new RestResponse and assocaite it with uri, responseBody and statusCode
     * @param uri
     * @param headers
     * @param responseBody
     * @param httpStatus
     */
    public RestResponse(String uri, Map<String, String> headers, String responseBody, int httpStatus) {

        Matcher m = uriPattern.matcher(uri);
        m.matches();
        this.headers = headers;
        this.uri = m.group(1);
        this.queryString = m.group(2);
        this.responseBody = responseBody;
        this.httpStatus = httpStatus;
        this.hasError = (httpStatus >= 400);
    }

    /**
     * Returns the content type
     * @return
     */
    public String contentType() {

        return headers.get("Content-Type");
    }

    /**
     * Does this response contain an error?
     * @return
     */
    public boolean hasError() {
        return hasError;
    }

    /**
     * Is the error a client error?
     * @return
     */
    public boolean isClientError() {
        return (httpStatus() >= 400 && httpStatus() < 500 );
    }

    /**
     * Is the error a server side error?
     * @return
     */
    public boolean isServerError() {
        return (httpStatus() >= 500);
    }

    /**
     * Http Status code of the response
     * @return
     */
    public int httpStatus() {
        return httpStatus;
    }

    /**
     * Is the response content type Json?
     * @return
     */
    public boolean isJson() {
        for(String hv : headers.values()) {
            if(hv.toLowerCase().contains("application/json")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is the response content type Xml?
     * @return
     */
    public boolean isXml() {
        for(String hv : headers.values()) {
            String v = hv.toLowerCase();
            if(v.contains("text/xml") || v.contains("application/xml")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the response body
     * @return
     */
    public String body() {
        return this.responseBody;
    }

    /**
     * toString
     * @return
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("uri=")
            .append(uri)
            .append(" headers=")
            .append(headers)
            .append(" body=")
            .append(responseBody).toString();
    }
}
