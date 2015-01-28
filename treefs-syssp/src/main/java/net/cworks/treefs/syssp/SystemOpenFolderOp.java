package net.cworks.treefs.syssp;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

class SystemOpenFolderOp implements FileVisitor<Path> {

    private DirectoryStream.Filter<Path> filter = null;
    private SystemStorageProvider provider;
    private Stack<SystemFolder> stack = null;
    private SystemFolder topFolder = null;
    /**
     * Create with a full path to the folder to open, this is the top-level folder
     * and with a filter that determines what folder and/or files this OpenOp loads
     * into memory.
     *
     * @param filter
     * @param storageProvider
     */
    SystemOpenFolderOp(DirectoryStream.Filter<Path> filter, SystemStorageProvider storageProvider) {
        this.filter = filter;
        this.provider = storageProvider;
        stack = new Stack<>();
    }

    /**
     * pre-visit this directory before visiting files inside it and before going deeper
     * @param folder
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path folder, BasicFileAttributes attrs)
        throws IOException {
        //System.out.println("preVisitDirectory: " + folder);

        if(!provider._isManagedFolder(folder)) {
            return FileVisitResult.SKIP_SUBTREE;
        }

        SystemFolder systemFolder = SystemPathIO.readSystemFolder(folder);
        stack.push(systemFolder);

        return FileVisitResult.CONTINUE;
    }

    /**
     * Visit a folder or file
     * @param path
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

        //System.out.println("visitFile: " + path);
        if(provider._isManagedFolder(path)) {
            SystemFolder systemFolder = SystemPathIO.readSystemFolder(path);
            // maxDepth was 0, meaning first visit was to the folder/file
            if(stack.size() == 0) {
                stack.push(systemFolder);
                return FileVisitResult.CONTINUE;
            }

            SystemFolder top = stack.peek();
            top.addItem(systemFolder);
        } else if(provider._isManagedFile(path)) {
            if(filter != null && filter.accept(path)) {
                SystemFile systemFile = SystemPathIO.readSystemFile(path);
                SystemFolder top = stack.peek();
                top.addItem(systemFile);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        //System.out.println("visitFileFailed: " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path folder, IOException exc) throws IOException {
        //System.out.println("postVisitDirectory: " + folder);

        topFolder = stack.pop();
        if(stack.size() > 0) {
            stack.peek().addItem(topFolder);
        }

        return FileVisitResult.CONTINUE;
    }

    /**
     * After this walk is complete this will return the top-level SystemFolder that
     * is representing the fullPath argument of the constructor
     *
     * @return
     */
    public SystemFolder folder() {
        SystemFolder top = null;
        if(stack.size() == 0) {
            top = topFolder;
        } else if(stack.size() == 1) {
            top = stack.pop();
        }

        return top;
    }
}
