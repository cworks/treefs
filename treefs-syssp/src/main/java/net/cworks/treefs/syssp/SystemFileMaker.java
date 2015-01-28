package net.cworks.treefs.syssp;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

public class SystemFileMaker {

    String name;
    String description;
    Map<String, Object> metadata;
    Date lastModifiedTime;
    Date lastAccessedTime;
    Date creationTime;
    Long size = 0L;
    Path root;
    Path fullPath;

    SystemFileMaker() {
    }

    static SystemFileMaker newFile() {
        return new SystemFileMaker();
    }

    public SystemFileMaker withRoot(Path root) {
        this.root = root;
        return this;
    }

    public SystemFileMaker withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public SystemFileMaker withDescription(String description) {
        this.description = description;
        return this;
    }

    public SystemFileMaker withFullPath(Path fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public SystemFileMaker withCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public SystemFileMaker withLastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
        return this;
    }

    public SystemFileMaker withLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    public SystemFileMaker withName(String name) {
        this.name = name;
        return this;
    }

    public SystemFileMaker withSize(long size) {
        this.size = size;
        return this;
    }

    public SystemFile make() {
        SystemFile file = new SystemFile(root);
        if(name != null) file.name(name);
        if(description != null) file.description(description);
        if(fullPath != null) file.fullPath(fullPath);
        if(lastAccessedTime != null) file.lastAccessedTime(lastAccessedTime);
        if(lastModifiedTime != null) file.lastModifiedTime(lastModifiedTime);
        if(creationTime != null) file.creationTime(creationTime);
        if(size != null) file.size(size);
        if(metadata != null) file.metadata(metadata);

        return file;
    }
}
