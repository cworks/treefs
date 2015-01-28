package net.cworks.treefs.syssp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.cworks.treefs.spi.TreeFolder;
import net.cworks.treefs.spi.TreePath;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SystemFolder extends SystemPath implements TreeFolder {

    private static final String PROPERTY_ITEMS = "items";

    private List<TreePath> items = null;

    SystemFolder() {
        super(null);
        type("folder");
        items = new ArrayList<>();
    }

    SystemFolder(Path root) {
        super(root);
        type("folder");
        items = new ArrayList<>();
    }

    @Override
    @JsonProperty(PROPERTY_ITEMS)
    public List<TreePath> items() {
        if(hasItems()) {
            return this.items;
        } else {
            return null;
        }
    }

    @Override
    @JsonIgnore
    public boolean hasItems() {
        return !this.items.isEmpty();
    }

    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    // internal methods
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //

    @JsonIgnore
    void items(List<TreePath> items) {
        this.items = items;
    }

    @JsonIgnore
    void addItem(TreePath item) {
        if(item == null) {
            return;
        }
        this.items.add(item);
    }

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

    private static TreeFolder _find(TreeFolder folder, String path) {
        if(folder.items() == null) {
            return null;
        }
        List<TreePath> items = folder.items();
        for(TreePath item : items) {
            System.out.println(item.path().toString());
            String p1 = folder.path().toString().replace("\\", "/");

            if(item instanceof TreeFolder) {
                _traverse((TreeFolder)item);
            }
        }

        return null;
    }
}
