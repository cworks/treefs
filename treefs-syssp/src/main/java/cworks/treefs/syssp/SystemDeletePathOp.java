package cworks.treefs.syssp;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

class SystemDeletePathOp implements FileVisitor<Path> {

    boolean delete(Path path) throws IOException {

        boolean success = Files.deleteIfExists(path);
        String metadata = path.toString() + ".f";
        Files.deleteIfExists(Paths.get(metadata));

        return success;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        boolean success = delete(file);
        if(success) {
            // TODO audit log here
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
        if(ex == null) {
            boolean success = delete(dir);
            if(success) {
                // TODO audit log here
            }
        } else {
            throw ex;
        }
        return FileVisitResult.CONTINUE;
    }
}
