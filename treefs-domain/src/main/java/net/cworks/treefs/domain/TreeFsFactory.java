package net.cworks.treefs.domain;

import net.cworks.treefs.common.ObjectUtils;
import net.cworks.treefs.common.dt.ISO8601DateParser;
import net.cworks.treefs.spi.TreeFile;
import net.cworks.treefs.spi.TreeFolder;
import net.cworks.treefs.spi.TreePath;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TreeFsFactory {

    /**
     * Create a TreeFsFolderMaker with which to define and create a TreeFsFolder instance
     * @return
     */
    public static TreeFsFolderMaker folder() {
        return TreeFsFolderMaker.newFolder();
    }

    /**
     * Convenience method for creating a folder with name and path properties
     * @param name
     * @param path
     * @return
     */
    public static TreeFsFolderMaker folder(String name, Path path) {
        TreeFsFolderMaker maker = TreeFsFolderMaker.newFolder();
        return maker.withName(name).withPath(path);
    }

    /**
     * Convenience method for creating a folder with name, path and description
     * @param name
     * @param path
     * @param description
     * @return
     */
    public static TreeFsFolderMaker folder(String name, Path path, String description) {
        return TreeFsFolderMaker.newFolder().withName(name).withPath(path).withDescription(description);
    }

    /**
     * Convenience method for creating a folder from a SFolder instance
     * @param sourceFolder
     * @return
     */
    public static TreeFsFolder folder(TreeFolder sourceFolder) {
        TreeFsFolder targetFolder = TreeFsFactory.folder().make();
        _mapping(sourceFolder, targetFolder);
        return targetFolder;
    }

    /**
     * Create a TreeFsFileMaker with which to define a TreeFsFile instance
     * @return
     */
    public static TreeFsFileMaker file() {
        return TreeFsFileMaker.newFile();
    }

    /**
     * Convenience method for creating a file with name and path
     * @param name
     * @param path
     * @return
     */
    public static TreeFsFileMaker file(String name, Path path) {
        TreeFsFileMaker maker = TreeFsFileMaker.newFile();
        return maker.withName(name).withPath(path);
    }

    /**
     * Convenience method for creating a file from a JDK Path
     * @param file
     * @return
     */
    public static TreeFsFileMaker file(Path file) {
        TreeFsFileMaker maker = TreeFsFileMaker.newFile();
        Path parent = file.getParent();
        if(ObjectUtils.isNull(parent)) {
            parent = Paths.get("/");
        }
        return maker.withName(file.getFileName().toString()).withPath(parent);
    }

    /**
     * Convenience method for creating a file from a JDK File
     * @param file
     * @return
     */
    public static TreeFsFileMaker file(File file) {
        TreeFsFileMaker maker = TreeFsFileMaker.newFile();
        String parent = file.getParent();
        if(ObjectUtils.isNullOrEmpty(parent)) {
            parent = "/";
        }
        return maker.withName(file.getName()).withPath(Paths.get(parent));
    }

    /**
     * Create a TreeFsPathMaker to make paths
     * @return
     */
    public static TreeFsPathMaker path() {
        return new TreeFsPathMaker();
    }

    public static TreeFsSerializer serializer() {
        return TreeFsSerializer.newSerializer();
    }

    public static TreeFsDeserializer deserializer() {
        return TreeFsDeserializer.newDeserializer();
    }

    /**
     * Utility method for mapping SFolder to TreeFsFolder
     * @param source
     * @param folder
     */
    private static void _mapping(TreeFolder source, TreeFsFolder folder) {

        folder.name(source.name())
            .path(source.path().toString())
            .description(source.description())
            .createdAt(ISO8601DateParser.toString(source.creationTime()));

        if(source.hasMetadata()) {
            folder.metadata(source.metadata());
        }

        if(source.hasItems()) {
            List<TreePath> items = source.items();
            for(TreePath item : items) {
                System.out.println("> " + item.path().toString());

                if(item instanceof TreeFile) {
                    TreeFile fitem = (TreeFile)item;
                    TreeFsFileMaker maker = file(fitem.path());
                    TreeFsFile f = maker
                        .withDescription(fitem.description())
                        .withCreatedAt(fitem.creationTime())
                        .withSize(fitem.size()).make();
                    folder.addFile(f);

                } else if(item instanceof TreeFolder) {
                    TreeFolder fitem = (TreeFolder)item;
                    TreeFsFolderMaker maker = TreeFsFactory.folder(
                            fitem.path().getFileName().toString(), fitem.path());
                    TreeFsFolder f = maker
                        .withDescription(fitem.description())
                        .withCreatedAt(fitem.creationTime())
                        .make();
                    folder.addFolder(f);
                    _mapping(fitem, f);
                }
            }
        }
    }

}
