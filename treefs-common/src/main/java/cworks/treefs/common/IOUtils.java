package cworks.treefs.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IOUtils {

    /**
     * File system slash constant
     */
    public static final String SLASH = System.getProperty("file.separator");

    /**
     * End of File
     */
    static final int EOF = -1;

    /**
     * default file-IO buffer size for read and write ops
     */
    static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Utility method to read from the InputStream and write to the OutputStream, does not close the
     * streams, reads until EOF is found.
     *
     * @param input - the InputStream we read from
     * @param output - the OutputStream we write to
     * @return int - number of bytes we read
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        int count = 0;
        int n = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Utility method to read from the InputStream and write to the given Path, reads until
     * the InputStream is exhausted and closes it after all data is read.
     *
     * @param in
     * @param output
     * @throws IOException
     */
    public static void copy(InputStream in, Path output) throws IOException {

        OutputStream out = Files.newOutputStream(
            output, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        try {
            copy(in, out);
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    /**
     * Quietly close a Closeable
     * @param out
     */
    public static void closeQuietly(Closeable out) {
        if(out == null) {
            return;
        }
        try { out.close(); } catch (Exception e) { }
    }
}
