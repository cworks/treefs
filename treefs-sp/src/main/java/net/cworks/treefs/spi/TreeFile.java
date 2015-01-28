package net.cworks.treefs.spi;

public interface TreeFile extends TreePath {
    /**
     * Number of bytes with Path consumes
     * @return
     */
    public Long size();

    /**
     * Implementers should return a SHA1 checksum of the content contained for the file that goes with this metadata
     * instance or null if checksum exists.
     * @return
     */
    public String checksum();

    /**
     * Implementers should return the content-type of the file that goes with this metadata instance
     * @return
     */
    public TreePathContentType contentType();
}
