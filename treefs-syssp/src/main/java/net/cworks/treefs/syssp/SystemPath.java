package net.cworks.treefs.syssp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.cworks.treefs.spi.TreePath;
import net.cworks.treefs.common.dt.ISO8601DateParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class SystemPath implements TreePath {

    static final String PROPERTY_DESCRIPTION = "description";
    static final String PROPERTY_NAME = "name";
    static final String PROPERTY_PATH = "path";
    static final String PROPERTY_METADATA = "metadata";
    static final String PROPERTY_TYPE = "type";
    static final String PROPERTY_LAST_MODIFIED_TIME = "lastModifiedTime";
    static final String PROPERTY_LAST_ACCESSED_TIME = "lastAccessedTime";
    static final String PROPERTY_CREATION_TIME = "creationTime";

    /**
     * Description of this SystemPath
     */
    String description;

    /**
     * Metadata for this SystemPath
     */
    Map<String, Object> metadata;

    /**
     * last modified time of this SystemPath
     */
    Date lastModifiedTime;

    /**
     * last accessed time of this SystemPath
     */
    Date lastAccessedTime;

    /**
     * time this SystemPath was created
     */
    Date creationTime;

    /**
     * Name of this SystemPath
     */
    String name;

    /**
     * Type of this SystemPath, typically will be "file" or "folder"
     */
    String type;

    /**
     * The root Path of this SystemPath, never exposed outside of this package
     */
    Path root;

    /**
     * The full Path of this SystemPath, contains the root Path, never exposed outside of this package
     */
    Path fullPath;

    /**
     * The relative Path of this SystemPath, this can be exposed outside of this package
     */
    Path path;

    SystemPath(Path root) {
        this.root = root;
        this.type = "path";
    }

    @Override
    @JsonProperty(value = PROPERTY_DESCRIPTION)
    public String description() {
        return this.description;
    }

    @Override
    @JsonIgnore
    public boolean hasMetadata() {
        if(metadata != null && !metadata.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    @JsonIgnore
    public Date lastModifiedTime() {
        return this.lastModifiedTime;
    }

    @JsonProperty(PROPERTY_LAST_MODIFIED_TIME)
    public String lastModifiedTimeString() {
        if(lastModifiedTime() != null) {
            return ISO8601DateParser.toString(lastModifiedTime());
        }
        return null;
    }

    @Override
    @JsonIgnore
    public Date lastAccessedTime() {
        return this.lastAccessedTime;
    }

    @JsonProperty(PROPERTY_LAST_ACCESSED_TIME)
    public String lastAccessedTimeString() {
        if(lastAccessedTime() != null) {
            return ISO8601DateParser.toString(lastAccessedTime());
        }
        return null;
    }

    @Override
    @JsonIgnore
    public Date creationTime() {
        return this.creationTime;
    }

    @JsonProperty(PROPERTY_CREATION_TIME)
    public String creationTimeString() {
        if(creationTime() != null) {
            return ISO8601DateParser.toString(creationTime());
        }
        return null;
    }

    @Override
    @JsonIgnore
    public Path path() {
        if(fullPath()!= null) {
            Path rel = this.root.relativize(fullPath());
            return rel;
        }
        return this.path;
    }

    @JsonIgnore
    Path fullPath() {
        return fullPath;
    }

    @JsonIgnore
    void root(Path root) {
        this.root = root;
    }

    @JsonProperty(PROPERTY_PATH)
    public String pathString() {
        if(path() != null) {
            return path().toString().replace("\\", "/");
        }
        return null;
    }

    @Override
    @JsonProperty(PROPERTY_NAME)
    public String name() {
        String pathName = null;
        if(name != null && name.trim().length() > 0) {
            pathName = this.name;
        } else {
            Path p = path();
            if(p != null) {
                pathName = p.getFileName().toString();
            }
        }
        return pathName;
    }

    @JsonProperty(PROPERTY_METADATA)
    public Map<String, Object> metadata() {
        if(hasMetadata()) {
            return metadata;
        } else {
            return null;
        }
    }

    @JsonProperty(PROPERTY_TYPE)
    public String type() {
        return this.type;
    }

    // setters

    @JsonProperty(PROPERTY_TYPE)
    void type(String type) {
        this.type = type;
    }

    @JsonProperty(PROPERTY_NAME)
    void name(String name) {
        this.name = name;
    }

    @JsonIgnore
    void fullPath(Path fullPath) {
        this.fullPath = fullPath;
    }

    @JsonProperty(PROPERTY_PATH)
    void path(String path) {
        this.path = Paths.get(path);
    }

    @JsonProperty(PROPERTY_DESCRIPTION)
    void description(String description) {
        this.description = description;
    }

    @JsonIgnore
    void creationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @JsonProperty(PROPERTY_CREATION_TIME)
    void creationTimeString(String creationTime) {
        if(creationTime == null || creationTime.trim().length() < 1) {
            return;
        }
        try {
            Date d = ISO8601DateParser.parse(creationTime);
            this.creationTime = d;
        } catch (ParseException e) {
            // do nothing
        }
    }

    void lastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @JsonProperty(PROPERTY_LAST_ACCESSED_TIME)
    void lastAccessedTimeString(String lastAccessedTime) {
        if(lastAccessedTime == null || lastAccessedTime.trim().length() < 1) {
            return;
        }
        try {
            Date d = ISO8601DateParser.parse(lastAccessedTime);
            this.lastAccessedTime = d;
        } catch (ParseException e) {
            // do nothing
        }
    }

    void lastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @JsonProperty(PROPERTY_LAST_MODIFIED_TIME)
    void lastModifiedTimeString(String lastModifiedTime) {
        if(lastModifiedTime == null || lastModifiedTime.trim().length() < 1) {
            return;
        }
        try {
            Date d = ISO8601DateParser.parse(lastModifiedTime);
            this.lastModifiedTime = d;
        } catch (ParseException e) {
            // do nothing
        }
    }

    @JsonProperty(PROPERTY_METADATA)
    void metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return this.name();
    }
}
