package cworks.treefs.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TreeFsPath extends TreeFsObject {

    /**
     * name property
     */
    static final String PROPERTY_NAME = "name";

    /**
     * description property
     */
    static final String PROPERTY_DESCRIPTION = "description";

    /**
     * createdAt property
     */
    static final String PROPERTY_CREATED_AT = "createdAt";

    /**
     * createdBy property
     */
    static final String PROPERTY_CREATED_BY = "createdBy";

    /**
     * updatedAt property
     */
    static final String PROPERTY_UPDATED_AT = "updatedAt";

    /**
     * updatedBy property
     */
    static final String PROPERTY_UPDATED_BY = "updatedBy";

    /**
     * path property
     */
    static final String PROPERTY_PATH = "path";

    /**
     * metadata property
     */
    static final String PROPERTY_METADATA = "metadata";

    /**
     * Empty treefsitem
     */
    public TreeFsPath() {
        super();
        type("path");
    }

    /**
     * Copy Constructor
     * @param object
     */
    public TreeFsPath(TreeFsObject object) {
        super(object);
        type("path");
    }

    /**
     * Copy constructor
     *
     * @param object
     */
    public TreeFsPath(TreeFsPath object) {
        super(object);
        type("path");
    }

    /**
     * Create this item instance from some properties
     * @param properties
     */
    public TreeFsPath(Map<String, Object> properties) {
        super(properties);
        type("path");
    }

    /**
     * Get the metadata
     * @return metadata
     */
    @JsonProperty(PROPERTY_METADATA)
    public Map<String, Object> metadata() {
        return (Map<String,Object>)get(PROPERTY_METADATA);
    }

    /**
     * Return a specific metadata item
     * @param key
     * @return
     */
    public Object metadata(String key) {
        Map<String, Object> metadata = (Map<String, Object>)get(PROPERTY_METADATA);
        if(metadata == null) {
            return null;
        }
        Object value = metadata.get(key);
        return value;
    }

    /**
     * Set the metadata
     * @param metadata
     */
    @JsonProperty(PROPERTY_METADATA)
    TreeFsPath metadata(Map<String, Object> metadata) {
        put(PROPERTY_METADATA, metadata);
        return this;
    }

    /**
     * Get the name.
     * @return name
     */
    @JsonProperty(PROPERTY_NAME)
    public String name() {
        return (String)get(PROPERTY_NAME);
    }

    /**
     * Set the name
     * @param name
     */
    @JsonProperty(PROPERTY_NAME)
    TreeFsPath name(String name) {
        put(PROPERTY_NAME, name);
        return this;
    }

    /**
     * Get the description.
     * @return description
     */
    @JsonProperty(PROPERTY_DESCRIPTION)
    public String description() {
        return (String)get(PROPERTY_DESCRIPTION);
    }

    /**
     * Set the description
     * @param description
     */
    @JsonProperty(PROPERTY_DESCRIPTION)
    TreeFsPath description(String description) {
        put(PROPERTY_DESCRIPTION, description);
        return this;
    }

    /**
     * Get the createdAt property.
     * @return type
     */
    @JsonProperty(PROPERTY_CREATED_AT)
    public String createdAt() {
        return (String)get(PROPERTY_CREATED_AT);
    }

    /**
     * Set the createdAt property
     * @param createdAt
     */
    @JsonProperty(PROPERTY_CREATED_AT)
    TreeFsPath createdAt(String createdAt) {
        put(PROPERTY_CREATED_AT, createdAt);
        return this;
    }

    /**
     * Get the createdBy property.
     * @return createdBy
     */
    @JsonProperty(PROPERTY_CREATED_BY)
    public String createdBy() {
        return (String)get(PROPERTY_CREATED_BY);
    }

    /**
     * Set the createdBy property
     * @param createdBy
     */
    @JsonProperty(PROPERTY_CREATED_BY)
    TreeFsPath createdBy(String createdBy) {
        put(PROPERTY_CREATED_BY, createdBy);
        return this;
    }

    /**
     * Get the updatedAt property.
     * @return type
     */
    @JsonProperty(PROPERTY_UPDATED_AT)
    public String updatedAt() {
        return (String)get(PROPERTY_UPDATED_AT);
    }

    /**
     * Set the updatedAt property
     * @param updatedAt
     */
    @JsonProperty(PROPERTY_UPDATED_AT)
    TreeFsPath updatedAt(String updatedAt) {
        put(PROPERTY_UPDATED_AT, updatedAt);
        return this;
    }

    /**
     * Get the updatedBy property.
     * @return updatedBy
     */
    @JsonProperty(PROPERTY_UPDATED_BY)
    public String updatedBy() {
        return (String)get(PROPERTY_UPDATED_BY);
    }

    /**
     * Set the updatedBy property
     * @param updatedBy
     */
    @JsonProperty(PROPERTY_UPDATED_BY)
    TreeFsPath updatedBy(String updatedBy) {
        put(PROPERTY_UPDATED_BY, updatedBy);
        return this;
    }

    /**
     * Get the path property.
     * @return path
     */
    @JsonProperty(PROPERTY_PATH)
    public String path() {
        String path = (String)get(PROPERTY_PATH);
        if(path != null) {
            path = path.replace("\\", "/");
            return path;
        }
        return path;
    }

    /**
     * Set the path property
     * @param path
     */
    @JsonProperty(PROPERTY_PATH)
    TreeFsPath path(String path) {
        put(PROPERTY_PATH, path);
        return this;
    }
}
