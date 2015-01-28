package net.cworks.treefs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.cworks.json.Json.Json;

public class TreeFsFolder extends TreeFsPath {

    /**
     * folders can have sub items
     */
    public static final String PROPERTY_PATHS = "paths";

    /**
     * Create this TreeFsFolder instance and set type name to 'folder'
     */
    protected TreeFsFolder() {
        super();
        type("folder");
    }

    /**
     * Copy constructor
     *
     * @param object
     */
    protected TreeFsFolder(TreeFsFolder object) {
        super(object);
        type("folder");
    }

    /**
     * Create this TreeFsFolder from some properties
     * @param properties
     */
    private TreeFsFolder(Map<String, Object> properties) {
        super(properties);
        type("folder");
    }

    /**
     * @return the paths
     */
    @SuppressWarnings("unchecked")
    @JsonProperty("paths")
    public List<TreeFsPath> paths() {
        return (List<TreeFsPath>) super.get(PROPERTY_PATHS);
    }

    /**
     * Visit all paths()
     * @param visitor
     */
    @JsonIgnore
    public void walk(TreeFsPathVisitor visitor) {

        if(paths() == null) {
            return;
        }
        _walk(this, visitor);
    }

    private void _walk(TreeFsFolder folder, TreeFsPathVisitor visitor) {

        if(folder.paths() == null) {
            return;
        }
        List<TreeFsPath> items = folder.paths();
        for(TreeFsPath item : items) {
            visitor.visit(item);
            if(item instanceof TreeFsFolder) {
                _walk((TreeFsFolder)item, visitor);
            }
        }
    }

//    /**
//     * traverse the folder for fun
//     * @param folder
//     */
//    private static void _traverse(SFolder folder) {
//        if(folder.items() == null) {
//            return;
//        }
//        List<SPath> items = folder.items();
//        for(SPath item : items) {
//            System.out.println(item.path().toString());
//            if(item instanceof SFolder) {
//                _traverse((SFolder)item);
//            }
//        }
//    }
//    if(!(path instanceof TreeFsFolder)) {
//        visitor.visit(path);
//        return;
//    }
//
//    TreeFsFolder folder = (TreeFsFolder)path;
//    if(folder.paths() == null) {
//        visitor.visit(folder);
//        return;
//    }
//
//    visitor.preVisit(folder);
//
//    List<TreeFsPath> items = folder.paths();
//    for(TreeFsPath item : items) {
//        visitor.visit(item);
//        _walk(item, visitor);
//    }
//
//    visitor.postVisit(folder);

    /**
     * @param paths the paths to set
     */
    @JsonProperty("paths")
    void paths(List<TreeFsPath> paths) {
        put(PROPERTY_PATHS, paths);
    }

    /**
     * Add a file as a child of this folder
     * @param file
     */
    @JsonIgnore
    void addFile(TreeFsFile file) {
        List<TreeFsPath> paths = paths();
        if(paths == null) {
            paths(new ArrayList<TreeFsPath>());
        }
        paths().add(file);
    }

    /**
     * Add a folder as a child of this folder
     * @param folder
     */
    @JsonIgnore
    void addFolder(TreeFsFolder folder) {
        List<TreeFsPath> paths = paths();
        if(paths == null) {
            paths(new ArrayList<TreeFsPath>());
        }
        paths().add(folder);
    }

    /**
     * Trye to create a TreeFsFolder instance from a JSON string
     * @param json
     * @return
     */
    public static TreeFsFolder fromJSON(String json) {
        Map<String, Object> properties = Json().toObject(json, Map.class);
        TreeFsFolder folder = new TreeFsFolder(properties);
        return folder;
    }

    @Override
    public String toString() {
        return Json().toJson(this);
    }
}
