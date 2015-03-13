package cworks.treefs.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    /**
     * Compute a Sha1 hash for the content of file
     * @param file the File to perform a Sha1 on
     * @return the computed sha1 in hex format
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String sha1(final File file) throws IOException, NoSuchAlgorithmException {
        byte[] hash = sha1Hash(file);
        String hex = hexString(hash);
        return hex;
    }

    /**
     * Compute a Sha1 byte array for the content of file
     * @param file file to sha1
     * @return sha1 bytes
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] sha1Hash(File file) throws IOException, NoSuchAlgorithmException {
        int bytesRead  = 0;
        byte[] buffer  = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
        byte[] hash    = null;
        InputStream is = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            is = new FileInputStream(file);
            while ((bytesRead = is.read(buffer)) != IOUtils.EOF) {
                digest.update(buffer, 0, bytesRead);
            }
            hash = digest.digest();
        } finally {
            IOUtils.closeQuietly(is);
        }

        return hash;
    }

    /**
     * Convert the bytes into a hexidecimal string
     * @param data data to convert
     * @return hex string
     */
    public static String hexString(byte[] data) {
        StringBuffer hexBuffer = new StringBuffer("");
        for(int i = 0; i < data.length; i++) {
            hexBuffer.append(Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1));
        }
        return hexBuffer.toString();
    }
}
