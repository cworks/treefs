package cworks.treefs.spi;

import java.util.List;

public interface TreeFolder extends TreePath {
    /**
     * Implementers should return a list of CloudItems that exist in this CloudFolder
     * @return
     */
    public List<TreePath> items();

    /**
     * Implementers should return false if this CloudFolder does not contain any CloudItems
     *
     * @return
     */
    public boolean hasItems();
}
