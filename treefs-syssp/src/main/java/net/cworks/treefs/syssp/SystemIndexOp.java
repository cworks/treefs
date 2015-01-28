package net.cworks.treefs.syssp;

import net.cworks.json.JsonObject;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

class SystemIndexOp implements FileVisitor<Path> {

    private SystemStorageProvider provider;
    private boolean isFileTarget = false;

    public SystemIndexOp(SystemStorageProvider provider, boolean isFileTarget) {
        this.provider = provider;
        this.isFileTarget = isFileTarget;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        // This FileVisitor is just targeted at one file (i.e. not a folder)
        if(isFileTarget) {
            _visitFileTarget(file, attrs);
        } else {
            _visitFolderTarget(file, attrs);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    void isFileTarget(boolean isFile) {
        this.isFileTarget = isFile;
    }

    private void _visitFileTarget(Path file, BasicFileAttributes attributes) throws IOException {
        if(file.toString().endsWith(".f") || file.toString().endsWith(".d")) {
            return;
        }
        Path target = Paths.get(file.toString() + ".f");
        JsonObject targetObject = SystemPathIO.readJson(target);
        Path targetPath = provider._removeRoot(target);
        String targetString = targetPath.toString().substring(
            0, targetPath.toString().length()-2);
        targetObject.setString("path", targetString);
        SystemPathIO.writeJson(target, targetObject);
    }

    private void _visitFolderTarget(Path file, BasicFileAttributes attributes) throws IOException {
        if(Files.isRegularFile(file)) {
            if(file.toString().endsWith(".f")) {
                JsonObject targetObject = SystemPathIO.readJson(file);
                Path targetPath = provider._removeRoot(file);
                String targetString = targetPath.toString().substring(
                    0, targetPath.toString().length()-2);
                targetObject.setString("path", targetString);
                SystemPathIO.writeJson(file, targetObject);
            } else if(file.toString().endsWith(".d")) {
                JsonObject targetObject = SystemPathIO.readJson(file);
                Path targetPath = provider._removeRoot(file);
                String targetString = targetPath.getParent().toString();
                targetObject.setString("path", targetString);
                SystemPathIO.writeJson(file, targetObject);
            }
        }
    }
}
