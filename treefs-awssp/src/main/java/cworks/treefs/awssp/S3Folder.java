package cworks.treefs.awssp;

import cworks.json.JsonObject;
import cworks.treefs.spi.TreeFolder;
import cworks.treefs.spi.TreePath;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a folder object in AWS S3.
 * @author comartin
 */
public class S3Folder extends S3Path implements TreeFolder {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(S3Folder.class);

    /**
     * LinkedHashSet preserves insertion order
     */
    private final Set<S3Path> children;

    /**
     * Create this SFolder instance from a JsonObject rendered from S3 metadata
     * @param data
     */
    public S3Folder(JsonObject data) {
        super(data);
        this.children = new LinkedHashSet<S3Path>();
    }

    /**
     * Return a list of SPath instances contained within this S3Folder instance
     * @return
     */
    @Override
    public List<TreePath> items() {
        if(hasItems()) {
            return new ArrayList<TreePath>(children);
        } else {
            return null;
        }
    }

    /**
     * Does this S3Folder instance have SPath items in it?
     * @return
     */
    @Override
    public boolean hasItems() {
        return !this.children.isEmpty();
    }

    S3Path addItem(S3Path item) {

        // iterate over our children
        for (S3Path child: children ) {
            // if we have a child with same name as item then return it
            if (child.name().equals(item.name())) {
                return child;
            }
        }

        // if we've not returned then add this item as a child
        return child(item);
    }

    S3Path child(S3Path child) {
        children.add(child);
        return child;
    }

    /**
     * Traverse the list of items in the items list
     * @param folder
     */
    private static void _traverse(TreeFolder folder) {
        if(folder.items() == null) {
            return;
        }
        List<TreePath> items = folder.items();
        for(TreePath item : items) {
            System.out.println(item.path().toString());
            if(item instanceof TreeFolder) {
                _traverse((TreeFolder)item);
            }
        }
    }

}
