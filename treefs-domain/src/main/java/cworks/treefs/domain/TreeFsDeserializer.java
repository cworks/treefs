package cworks.treefs.domain;

import cworks.json.Json;

public class TreeFsDeserializer {

    private TreeFsDeserializer() { }

    public TreeFsFolder folder(String serialized) {
        TreeFsFolder folder = Json.asObject(serialized, TreeFsFolder.class);
        return folder;
    }

    public TreeFsFile file(String serialized) {
        TreeFsFile file = Json.asObject(serialized, TreeFsFile.class);
        return file;
    }

    public TreeFsObject object(String serialized) {
        TreeFsObject object = Json.asObject(serialized, TreeFsObject.class);
        return object;
    }

    public static TreeFsDeserializer newDeserializer() {
        return new TreeFsDeserializer();
    }
}
