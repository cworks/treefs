package net.cworks.treefs.domain;


import net.cworks.treefs.common.dt.ISO8601DateParser;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TreeFsFileMaker {

    protected String name;
    protected String path;
    protected String description;
    protected String createdBy;
    protected String createdAt;
    protected Map<String, Object> metadata;
    protected long size;
    protected String sha1;

    /**
     * Use TreeFsFactory to create
     */
    protected TreeFsFileMaker() {
        metadata = new HashMap<>();
    }

    public TreeFsFileMaker withName(String name) {
        this.name = name;
        return this;
    }

    public TreeFsFileMaker withDescription(String description) {
        this.description = description;
        return this;
    }

    public TreeFsFileMaker withPath(String path) {
        this.path = path;
        return this;
    }

    public TreeFsFileMaker withPath(Path path) {
        this.path = path.toString();
        return this;
    }

    public TreeFsFileMaker withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public TreeFsFileMaker withCreatedAt(Date createdAt) {
        this.createdAt = ISO8601DateParser.toString(createdAt);
        return this;
    }

    public TreeFsFileMaker withMetadata(String tag, Object value) {
        this.metadata.put(tag, value);
        return this;
    }

    public TreeFsFileMaker withSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public TreeFsFileMaker withSize(long size) {
        this.size = size;
        return this;
    }

    /**
     * Create a TreeFsFileMaker with which to define a <code>TreeFsFolder</code> instance
     * @return
     */
    static TreeFsFileMaker newFile() {
        return new TreeFsFileMaker();
    }

    /**
     * Perform validation here
     * @return
     */
    public TreeFsFile make() {

        TreeFsFile file = new TreeFsFile();
        if(name != null) file.name(name);
        if(description != null) file.description(description);
        if(path != null) file.path(path);
        if(createdAt != null) file.createdAt(createdAt);
        if(createdBy != null) file.createdBy(createdBy);
        if(metadata != null && metadata.size() > 0) file.metadata(metadata);
        if(sha1 != null) file.sha1(sha1);
        file.size(size);
        return file;
    }
}
