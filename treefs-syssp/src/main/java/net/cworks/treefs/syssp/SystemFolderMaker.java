package net.cworks.treefs.syssp;

import net.cworks.treefs.spi.TreePath;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemFolderMaker {

    String name;
    String description;
    Map<String, Object> metadata;
    Date lastModifiedTime;
    Date lastAccessedTime;
    Date creationTime;
    List<TreePath> items;
    Path root;
    Path fullPath;

    /**
     * Use SystemFolderMaker.newFolder() to create
     */
    SystemFolderMaker() {
        items = new ArrayList<>();
        metadata = new HashMap<>();
    }

    /**
     * Create a SystemFolder with which to define a <code>SystemFolder</code> instance
     * @return
     */
    static SystemFolderMaker newFolder() {
        return new SystemFolderMaker();
    }

    public SystemFolderMaker withRoot(Path root) {
        this.root = root;
        return this;
    }

    public SystemFolderMaker withName(String name) {
        this.name = name;
        return this;
    }

    public SystemFolderMaker withDescription(String description) {
        this.description = description;
        return this;
    }

    public SystemFolderMaker withFullPath(Path fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public SystemFolderMaker withMetadata(String tag, Object value) {
        this.metadata.put(tag, value);
        return this;
    }

    public SystemFolderMaker withMetadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }

    public SystemFolderMaker withLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    public SystemFolderMaker withLastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
        return this;
    }

    public SystemFolderMaker withCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public void addFolder(SystemFolder folder) {
        this.items.add(folder);
    }

    public void addFile(SystemFile file) {
        this.items.add(file);
    }

    /**
     * Perform validation here
     * @return
     */
    public SystemFolder make() {

        SystemFolder folder = new SystemFolder(root);
        if(name != null) folder.name(name);
        if(description != null) folder.description(description);
        if(fullPath != null) folder.fullPath(fullPath);
        if(lastAccessedTime != null) folder.lastAccessedTime(lastAccessedTime);
        if(lastModifiedTime != null) folder.lastModifiedTime(lastModifiedTime);
        if(creationTime != null) folder.creationTime(creationTime);
        if(metadata != null) folder.metadata(metadata);
        if(items != null && items.size() > 0) folder.items(items);

        return folder;
    }
}
