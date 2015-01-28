package net.cworks.treefs.domain;

import net.cworks.treefs.common.dt.ISO8601DateParser;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TreeFsPathMaker {
    protected String name;
    protected String path;
    protected String description;
    protected String createdBy;
    protected String createdAt;
    protected Map<String, Object> metadata;

    /**
     * Use TreeFsFactory to create
     */
    protected TreeFsPathMaker() {
        metadata = new HashMap<>();
    }

    public TreeFsPathMaker withName(String name) {
        this.name = name;
        return this;
    }

    public TreeFsPathMaker withDescription(String description) {
        this.description = description;
        return this;
    }

    public TreeFsPathMaker withPath(String path) {
        this.path = path;
        return this;
    }

    public TreeFsPathMaker withPath(Path path) {
        this.path = path.toString();
        return this;
    }

    public TreeFsPathMaker withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public TreeFsPathMaker withCreatedAt(Date createdAt) {
        this.createdAt = ISO8601DateParser.toString(createdAt);
        return this;
    }

    public TreeFsPathMaker withMetadata(String tag, Object value) {
        this.metadata.put(tag, value);
        return this;
    }
    /**
     * Create a TreeFsPathMaker with which to define a <code>TreeFsPath</code> instance
     * @return
     */
    static TreeFsPathMaker newPath() {
        return new TreeFsPathMaker();
    }

    public TreeFsPath make() {
        TreeFsPath path = new TreeFsPath();
        if(name != null) path.name(name);
        if(description != null) path.description(description);
        if(path != null) path.path(this.path);
        if(createdAt != null) path.createdAt(createdAt);
        if(createdBy != null) path.createdBy(createdBy);
        if(metadata != null && metadata.size() > 0) path.metadata(metadata);
        return path;
    }
}
