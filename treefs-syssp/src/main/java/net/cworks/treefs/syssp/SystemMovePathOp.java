package net.cworks.treefs.syssp;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * Utility class to move a Path tree.  By default the Files.move method is not recursive.
 * @author comartin
 */
class SystemMovePathOp implements FileVisitor<Path> {
    private final Path from;
    private final Path to;
    static FileTime time = null;

    public SystemMovePathOp(Path from, Path to) {
        this.from = from;
        this.to = to;
    }

    public void move(Path from, Path to) throws IOException {
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path folder, BasicFileAttributes attrs)
        throws IOException {
        Path newFolder = to.resolve(from.relativize(folder));
        try {
            Files.createDirectories(newFolder);
            Files.copy(folder, newFolder,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);
            time = Files.getLastModifiedTime(folder);
        } catch(DirectoryNotEmptyException ex) {
            // continue if newFolder directory is not empty
            return FileVisitResult.CONTINUE;
        } catch(NoSuchFileException ex) {
            return FileVisitResult.CONTINUE;
        } catch(IOException ex) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        move(file, to.resolve(from.relativize(file)));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path folder, IOException exc) throws IOException {
        Path newFolder = to.resolve(from.relativize(folder));
        try {
            Files.setLastModifiedTime(newFolder, time);
            Files.delete(folder);
        } catch(IOException ex) {
            // do nothing and continue
        }
        return FileVisitResult.CONTINUE;
    }
}
