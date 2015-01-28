package net.cworks.treefs.server.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

// # MimeType
//
// Basic MimeType support inspired by the Apache Http Server project.
public class MimeType {

    // @private
    // Internal map with all known mime types
    private static final Map<String, String> mimes = new HashMap<>();
    // @private
    // Internal default content encoding (charset)
    private static final String defaultContentEncoding = Charset.defaultCharset().name();

    // Loads a file from a input stream containing all known mime types. The InputStream is a resource mapped from the
    // project resource directory.
    //
    // @private
    // @static
    // @method loadFile
    // @param {InputStream} in
    private static void loadFile(InputStream in) {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String l;

            while ((l = br.readLine()) != null) {
                if (l.length() > 0 && l.charAt(0) != '#') {
                    String[] tokens = l.split("\\s+");
                    for (int i = 1; i < tokens.length; i++) {
                        mimes.put(tokens[i], tokens[0]);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // @constructor MimeType
    // Static constructor to load the mime types from the resource directory inside the jar file.
    static {
        loadFile(MimeType.class.getResourceAsStream("mime.types"));
        loadFile(MimeType.class.getResourceAsStream("mimex.types"));
    }

    // Returns a mime type string by parsing the file extension of a file string. If the extension is not found or
    // unknown the default value is returned.
    //
    // @method getMime
    // @static
    // @getter
    // @param {String} file - path to a file with extension
    // @param {String} defaultMimeType - what to return if not found
    // @return {String} mime type
    public static String getMime(String file, String defaultMimeType) {
        int sep = file.lastIndexOf('.');
        if (sep != -1) {
            String extension = file.substring(sep + 1, file.length());

            String mime = mimes.get(extension);

            if (mime != null) {
                return mime;
            }
        }

        return defaultMimeType;
    }

    // Gets the mime type string for a file with fallback to text/plain
    //
    // @method getMime
    // @static
    // @getter
    // @param {String} file - path to a file with extension
    // @return {String} mime type
    public static String getMime(String file) {
        return getMime(file, "text/plain");
    }

    // Gets the default charset for a file.
    // for now all mime types that start with text returns UTF-8 otherwise the fallback.
    //
    // @method getCharset
    // @static
    // @getter
    // @param {String} mime the mime type to query
    // @param {String} fallback if not found returns fallback
    // @return {String} charset string
    public static String getCharset(String mime, String fallback) {
        // TODO: exceptions json and which other should also be marked as text
        if (mime.startsWith("text")) {
            return defaultContentEncoding;
        }

        return fallback;
    }

    // Gets the default charset for a file with default fallback null
    //
    // @method getCharset
    // @static
    // @getter
    // @param {String} mime the mime type to query
    // @return {String} charset string
    public static String getCharset(String mime) {
        return getCharset(mime, null);
    }
}
