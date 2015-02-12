package cworks.treefs.domain;

import cworks.json.Json;

public class TreeFsSerializer {

    /**
     * User newSerializer or TreeFsFactory
     */
    private TreeFsSerializer() { }

    /**
     * When this is called kick some TreeFsFolder serializing buttocks
     * @return
     */
    public String folder(TreeFsFolder folder) {
        String text = Json.asString(folder);
        return text;
    }

    /**
     *
     * @return
     */
    static TreeFsSerializer newSerializer() {
        return new TreeFsSerializer();
    }
}
