package net.cworks.treefs.domain;

import static net.cworks.json.Json.Json;

public class TreeFsDeserializer {

    private TreeFsDeserializer() { }

    public TreeFsFolder folder(String serialized) {
        TreeFsFolder folder = Json().toObject(serialized, TreeFsFolder.class);
        return folder;
    }

    public TreeFsFile file(String serialized) {
        TreeFsFile file = Json().toObject(serialized, TreeFsFile.class);
        return file;
    }

    public TreeFsObject object(String serialized) {
        TreeFsObject object = Json().toObject(serialized, TreeFsObject.class);
        return object;
    }

    public static TreeFsDeserializer newDeserializer() {
        return new TreeFsDeserializer();
    }
}
