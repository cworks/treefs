package net.cworks.treefs.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value=TreeFsPath.class, name="path"),
    @JsonSubTypes.Type(value=TreeFsFile.class, name="file"),
    @JsonSubTypes.Type(value=TreeFsFolder.class, name="folder"),
    @JsonSubTypes.Type(value=TreeFsError.class, name="error")

})
public class TreeFsObject {

    private final Map<String, Object> properties;

    /**
     * type property
     */
    static final String PROPERTY_TYPE = "type";

    public TreeFsObject() {
        properties = new HashMap<String, Object>();
        type("object");
    }

    /**
     * create from a map of properties
     * @param properties
     */
    public TreeFsObject(Map<String, Object> properties) {
        this();
        cloneMap(this.properties, properties);
    }

    /**
     * Copy constructor
     * @param object
     */
    public TreeFsObject(TreeFsObject object) {
        this();
        cloneMap(properties, object.properties);
    }

    /**
     * Get the type.
     * @return type
     */
    public String type() {
        return (String)get(PROPERTY_TYPE);
    }

    /**
     * Set the type
     * @param type
     */
    protected void type(String type) {
        put(PROPERTY_TYPE, type);
    }

    protected void put(String property, Object value) {
        properties.put(property, value);
    }

    protected Object get(String property) {
        return properties.get(property);
    }

    protected boolean contains(String key) {
        return properties.containsKey(key);
    }

    private static void cloneMap(Map<String, Object> destination, Map<String, Object> source) {

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof TreeFsObject) {
                try {
                    destination.put(entry.getKey(),
                        value.getClass().getConstructor(
                            value.getClass()).newInstance(value));
                } catch (Exception e) {
                }
            } else if (value instanceof ArrayList<?>) {
                ArrayList<Object> list = new ArrayList<Object>();
                cloneList(list, (ArrayList<Object>) value);
                destination.put(entry.getKey(), list);
            } else {
                destination.put(entry.getKey(), value);
            }
        }
    }

    private static void cloneList(List<Object> target, List<Object> source) {
        for (Object obj : source) {
            if (obj instanceof TreeFsObject) {
                try {
                    target.add(obj.getClass().getConstructor(
                        obj.getClass()).newInstance(obj));
                } catch (Exception e) { }
            } else {
                target.add(obj);
            }
        }
    }

}
