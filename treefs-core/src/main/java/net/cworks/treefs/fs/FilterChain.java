package net.cworks.treefs.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter thats applied in order to file system elements
 */
public class FilterChain implements DirectoryStream.Filter<Path> {

    /**
     * List of filters that contain glob patterns
     */
    private List<DirectoryStream.Filter<Path>> globFilters = null;

    /**
     * package-private access - clients should use newChain()
     */
    private FilterChain() {
        globFilters = new ArrayList<DirectoryStream.Filter<Path>>();
    }

    boolean filesOnly = false;

    boolean foldersOnly = false;

    /**
     * create a filter chain
     * @return
     */
    public static FilterChain newChain() {
        FilterChain chain = new FilterChain();
        return chain;
    }

    /**
     * only accept folders
     */
    public void includeFolders() {
        foldersOnly = true;
    }

    /**
     * only accept files
     */
    public void includeFiles() {
        filesOnly = true;
    }

    /**
     * only accept files matching the glob pattern
     * @param glob
     */
    public void globFilter(String glob) {
        GlobFilter gf = new GlobFilter(glob);
        if(!globFilters.contains(gf)) {
            globFilters.add(gf);
        }
    }

    /**
     * Roll through the filters and accept if at least one filters accepts the path
     *
     * files | folders
     * ---------------
     * false | false - consider both files and folders
     * false | true
     * true  | false
     * true  | true - consider both files and folders
     *
     * @param entry
     * @return
     * @throws IOException
     */
    @Override
    public boolean accept(Path entry) throws IOException {

        // entry must match at least one glob
        // entry can't be both a folder and a file

        // if folders only is selected then test entry for being a folder + glob filters
        if(foldersOnly && !filesOnly) {

            if(Files.isRegularFile(entry)) {
                return false;
            }

        } else if(filesOnly && !foldersOnly) {
            // if files only is selected then test entry for being a file + glob filters
            if(Files.isDirectory(entry)) {
                return false;
            }
        }

        // if no glob filters and we've gotten to this point then accept because
        // condition is either both files and folders or none
        if(globFilters.size() == 0) {
            return true;
        }

        // if entry is accepted by one glob filter then accept should return true
        for(DirectoryStream.Filter<Path> filter : globFilters) {
            if(filter.accept(entry)) {
                return true;
            }
        }

        return false;
    }
}
