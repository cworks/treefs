package cworks.treefs.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * Use a glob pattern to filter
 */
public class GlobFilter implements DirectoryStream.Filter<Path> {

    private final PathMatcher matcher;

    private String glob = null;

    public GlobFilter(String glob) {
        this.glob = glob;
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    }

    @Override
    public boolean accept(Path path) throws IOException {

        if(matcher == null) {
            return true;
        }

        Path name = path.getFileName();
        if(name != null && matcher.matches(name)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object thing) {
        if(!(thing instanceof GlobFilter)) {
            return false;
        }
        GlobFilter rhs = (GlobFilter)thing;
        // defensively take care of any null situations that may arise
        if(this.glob == null) {
            if(rhs.glob == null) {
                return true;
            } else {
                return false;
            }
        } else if(rhs.glob == null) {
            if(this.glob == null) {
                return true;
            } else {
                return false;
            }
        }

        if(this.glob.equals(rhs.glob)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        if(this.glob != null) {
            return glob.hashCode();
        }

        return ((Object)this).hashCode();
    }
}
