package net.cworks.treefs.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static net.cworks.json.Json.Json;

public class TreeFsFile extends TreeFsPath {

    /**
     * sha1 of the file content
     */
    public final static String PROPERTY_SHA1 = "sha1";

    /**
     * size property
     */
    public static final String PROPERTY_SIZE = "size";

    /**
     * Create this TreeFsFile instance and set type name to 'file'
     */
    public TreeFsFile() {
        super();
        type("file");
    }

    /**
     * Copy constructor
     *
     * @param object
     */
    public TreeFsFile(TreeFsFile object) {
        super(object);
        type("file");
    }

    /**
     * Create this TreeFsFile from some properties
     * @param properties
     */
    private TreeFsFile(Map<String, Object> properties) {
        super(properties);
        type("file");
    }

    /**
     * Get the sha1
     * @return sha1
     */
    @JsonProperty(PROPERTY_SHA1)
    public String sha1() {
        return (String)get(PROPERTY_SHA1);
    }

    /**
     * Set the sha1
     * @param sha1
     */
    @JsonProperty(PROPERTY_SHA1)
    void sha1(String sha1) {
        put(PROPERTY_SHA1, sha1);
    }

    /**
     * Get the size property.
     * @return size
     */
    @JsonProperty(PROPERTY_SIZE)
    public Long size() {
        return (Long)get(PROPERTY_SIZE);
    }

    /**
     * Set the size property
     * @param size
     */
    @JsonProperty(PROPERTY_SIZE)
    void size(Long size) {
        put(PROPERTY_SIZE, size);
    }

    /**
     * Try to create a TreeFsFile instance from a JSON string
     * @param json
     * @return
     */
    public static TreeFsFile fromJSON(String json) {
        Map<String, Object> properties = Json().toObject(json, Map.class);
        TreeFsFile file = new TreeFsFile(properties);
        return file;
    }

    /**
     * Try to create a TreeFsFile instance from a Map
     * @param properties
     * @return
     */
    public static TreeFsFile fromMap(Map properties) {
        TreeFsFile file = new TreeFsFile(properties);
        return file;
    }

    @Override
    public String toString() {
        return Json().toJson(this);
    }
}
