package net.cworks.treefs.spi;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

public interface TreePath {

    /**
     * Return the description for this path
     * @return
     */
    public String description();

    /**
     * Time at which this Path was last modified
     * @return
     */
    public Date lastModifiedTime();

    /**
     * Time at which this Path was last accessed
     * @return
     */
    public Date lastAccessedTime();

    /**
     * Time at which this Path was created
     * @return
     */
    public Date creationTime();

    /**
     * The Path instance associated with this PathMetadata instance
     * @return
     */
    public Path path();

    /**
     * The name of the Path item this PathMetadata instance represents
     * @return
     */
    public String name();

    /**
     * Implementers should return true when this PathMetadata instance does not contain any metadata tags
     * @return
     */
    public boolean hasMetadata();

    /**
     * The metadata structure for this CloudPath
     * @return
     */
    public Map<String, Object> metadata();
}
