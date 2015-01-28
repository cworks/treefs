package net.cworks.treefs.syssp;

import net.cworks.treefs.spi.NoTreeFolderException;
import net.cworks.treefs.spi.NoTreePathException;
import net.cworks.treefs.spi.NotATreeFolderException;
import net.cworks.treefs.spi.TreeCopyOption;
import net.cworks.treefs.spi.TreeFile;
import net.cworks.treefs.spi.TreeFileExistsException;
import net.cworks.treefs.spi.TreeFolder;
import net.cworks.treefs.spi.TreeFolderExistsException;
import net.cworks.treefs.spi.TreeFolderNotEmptyException;
import net.cworks.treefs.spi.TreePath;
import net.cworks.treefs.spi.TreePathExistsException;
import net.cworks.treefs.spi.StorageException;
import net.cworks.treefs.spi.StorageProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.cworks.treefs.common.IOUtils.closeQuietly;
import static net.cworks.treefs.common.ObjectUtils.isNull;
import static net.cworks.treefs.common.ObjectUtils.isNullOrEmpty;

/**
 * System Storage Provider uses the local system as the provider framework, mainly for demo
 * testing purposes.
 *
 * IMPORTANT Conventions
 *
 * All private methods start with an underscore, for example _isManagedFolder(...)
 * Path arguments into a private method are always full paths
 * Path arguments into non-private methods are always relative
 *
 * @author comartin
 */
public class SystemStorageProvider implements StorageProvider {

    /**
     * default file-IO buffer size for read and write ops
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * metadata file suffix for directories/folders
     */
    private static final String FOLDER_METADATA_SUFFIX = ".d";

    /**
     * metadata file suffix for files
     */
    private static final String FILE_METADATA_SUFFIX = ".f";

    /**
     * Used to filter out folders and files that this provider does not know about
     */
    private class SystemPathFilter implements DirectoryStream.Filter<Path> {
        DirectoryStream.Filter<Path> delegate = null;
        SystemPathFilter() {
            this.delegate = null;
        }
        SystemPathFilter(DirectoryStream.Filter<Path> delegate) {
            this.delegate = delegate;
        }
        @Override
        public boolean accept(Path entry) throws IOException {
            // first make sure entry exists as a managed file/folder by this provider
            boolean accept = _exists(entry);
            // if no delegate to forward to then return
            if(isNull(delegate)) {
                return accept;
            }

            return delegate.accept(entry);
        }
    }

    /**
     * This filter considers everything, even stuff the provider does not know about
     */
    private class LocalPathFilter implements DirectoryStream.Filter<Path> {
        @Override
        public boolean accept(Path entry) throws IOException {
            if(Files.isDirectory(entry) || Files.isRegularFile(entry)) {
                return true;
            }
            return false;
        }
    }

    private class SystemFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return false;
        }
    }

    /**
     * By default the mount point is set from the SystemConfig instance
     */
    private String mount = SystemConfig.mount();

    /**
     * Bucket within the root that this provider is using for storage
     */
    private String bucket = SystemConfig.bucket();

    /**
     * This is the local root folder for this StorageProvider, all content is persisted
     * under the root. Root is computed from the mount + bucket.
     */
    private Path root = Paths.get(mount, bucket);

    /**
     * Package-private constructor, used from SystemStorageProviderBuilder
     * This constructor creates a SystemStorageProvider with the default mount and bucket
     */
    SystemStorageProvider() {
        // uses default mount and bucket
    }

    /**
     * Package-private constructor
     * @param mount the mount point of this storage provider on the local file-system
     */
    SystemStorageProvider(String mount) {
        this.mount = mount;
        this.root = Paths.get(_mount(), _bucket());
    }

    /**
     * Package-private constructor, used from SystemStorageProviderBuilder
     * This creates a SystemStorageProvider that uses custom mount and bucket locations
     * to persist data into.
     *
     * @param mount the mount point of this storage provider on the local file-system
     * @param bucket the bucket within the root this instance is associated with
     */
    SystemStorageProvider(String mount, String bucket) {
        this.mount = mount;
        this.bucket = bucket;
        this.root = Paths.get(_mount(), _bucket());
    }

    /**
     * The public way to create a SystemStorageProvider is to use this builder
     * @return SystemStorageProviderBuilder that will create the SystemStorageProvider instance
     */
    public static SystemStorageProviderBuilder newProvider() {
        return new SystemStorageProviderBuilder();
    }

    /**
     * Creates a folder by creating all nonexistent parent folders first then creates the target
     * folder.  If parent folders already exist then no exception should be thrown and the
     * implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw
     * {@link net.cworks.treefs.spi.TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     */
    @Override
    public TreeFolder createFolder(Path folder) throws StorageException {
        TreeFolder sFolder = createFolder(folder, new HashMap<String, Object>());
        return sFolder;
    }

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * metadata with the new folder.  If parent folders already exist then no exception should be
     * thrown and the implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw
     * {@link net.cworks.treefs.spi.TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param metadata Map that contains application specific metadata
     */
    @Override
    public TreeFolder createFolder(Path folder, Map<String, Object> metadata) throws StorageException {
        TreeFolder sFolder = createFolder(folder, null, metadata);
        return sFolder;
    }

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * description with the new folder.  If parent folders already exist then no exception should
     * be thrown and the implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw
     * {@link net.cworks.treefs.spi.TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param description A description that should be associated with the given folder
     * @throws StorageException
     */
    @Override
    public TreeFolder createFolder(Path folder, String description) throws StorageException {
        TreeFolder sFolder = createFolder(folder, description, null);
        return sFolder;
    }

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * metadata and description with the new folder.  If parent folders already exist then no
     * exception should be thrown and the implementation should cause no side-effects on existing
     * parent folders.
     *
     * If the target folder already exists then throw
     * {@link net.cworks.treefs.spi.TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param description A description that should be associated with the given folder
     * @param metadata Map that contains application specific metadata
     * @throws StorageException
     */
    @Override
    public TreeFolder createFolder(Path folder, String description, Map<String, Object> metadata)
        throws StorageException {

        if(isNull(folder)) {
            throw new IllegalArgumentException("path is a required arguments and cannot be null");
        }

        if(exists(folder)) {
            // TODO I know the javadocs say to throw but it just seems cleaner to do a silent return?
            throw new TreeFolderExistsException(
                "folder " + folder.toString() + " already exists", folder);
        }

        Path fullPath = _prependRoot(folder);
        try {
            // creates actual Folders on the local file-system if they don't exist
            // along with a basic metadata file
            _createFolders(folder);
        } catch(Exception ex) {
            throw new StorageException("exception creating folder: " + folder.toString(), ex);
        }

        SystemFolder systemFolder = null;
        try {
            SystemFolderMaker maker = SystemFolderMaker.newFolder().withRoot(root);
            _loadFolderAttributes(fullPath, maker);
            if(!isNullOrEmpty(description)) {
                maker.withDescription(description);
            }
            if(!isNull(metadata)) {
                // TODO rework the maker to accept a Map
                maker.withMetadata(metadata);
            }

            systemFolder = maker.make();
            SystemPathIO.createSystemFolder(systemFolder);
        } catch(Exception ex) {
            throw new StorageException("exception creating metadata for file: " + fullPath.getFileName().toString(), ex);
        }

        return systemFolder;
    }

    /**
     * Opens a folder and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link net.cworks.treefs.spi.TreeFolder#items()}
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder) throws StorageException {
        TreeFolder sFolder = openFolder(folder, new SystemPathFilter());
        return sFolder;
    }

    /**
     * Opens a folder and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link net.cworks.treefs.spi.TreeFolder#items()} loaded.
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder, int maxLevels) throws StorageException {
        TreeFolder sFolder = openFolder(folder, new SystemPathFilter() , maxLevels);
        return sFolder;
    }

    /**
     * Opens a folder and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link net.cworks.treefs.spi.TreeFolder#items()}.  The filter should be used
     * to determine what child items will be loaded into {@link net.cworks.treefs.spi.TreeFolder#items()}
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param filter A filter to apply to the children in this folder
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder, DirectoryStream.Filter<Path> filter)
        throws StorageException {

        SystemFolder systemFolder = null;
        Path fullPath = _prependRoot(folder);
        DirectoryStream<Path> ds = null;
        if(!exists(folder)) {
            throw new NoTreeFolderException(folder);
        }
        try {
            DirectoryStream.Filter theFilter = null;
            if(isNull(filter)) {
                theFilter = new SystemPathFilter();
            } else if(filter instanceof SystemPathFilter) {
                theFilter = filter;
            } else {
                theFilter = new SystemPathFilter(filter);
            }
            ds = Files.newDirectoryStream(fullPath, theFilter);
            if(isNull(ds)) {
                throw new StorageException("unable to open folder: " + folder);
            }
            systemFolder = SystemPathIO.readSystemFolder(fullPath);
            _loadFolderItems(ds, systemFolder);
        } catch(Exception ex) {
            throw new StorageException(ex);
        } finally {
            closeQuietly(ds);
        }
        return systemFolder;
    }

    /**
     * Opens a folder and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link net.cworks.treefs.spi.TreeFolder#items()}.  The filter should be used
     * to determine what child items will be loaded into {@link net.cworks.treefs.spi.TreeFolder#items()}
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param filter A filter to apply to the children in this folder
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder, DirectoryStream.Filter<Path> filter, int maxLevels)
        throws StorageException {

        SystemFolder systemFolder = null;
        Path fullPath = _prependRoot(folder);

        if(!exists(folder)) {
            throw new NoTreeFolderException(folder);
        }

        try {
            DirectoryStream.Filter theFilter = null;
            if(isNull(filter)) {
                theFilter = new SystemPathFilter();
            } else if(filter instanceof SystemPathFilter) {
                theFilter = filter;
            } else {
                theFilter = new SystemPathFilter(filter);
            }
            Set<FileVisitOption> opts = Collections.emptySet();
            SystemOpenFolderOp openOp = new SystemOpenFolderOp(theFilter, this);
            if(maxLevels < 0) {
                maxLevels = 0;
            }
            Files.walkFileTree(fullPath, opts, maxLevels, openOp);
            systemFolder = openOp.folder();
        } catch(Exception ex) {
            throw new StorageException(ex);
        }
        return systemFolder;
    }

    /**
     * Opens a folder and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link net.cworks.treefs.spi.TreeFolder#items()}.  The glob pattern should be used
     * to determine what child items will be loaded into {@link net.cworks.treefs.spi.TreeFolder#items()}
     *
     * Glob patterns
     * 1) * - match any number of characters
     * 2) ? - match exactly one character
     * 3) {} - match a collection of sub patterns (example *.{txt, pdf, docx})
     * 4) [] - match a set or characters or a range if a hyphen is used
     *         (example [A-Z] matches any uppercase letter)
     *         (example [abc] matches any of a, b, c
     * 5) *, ?, \ - must be escaped by \ to be matched
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param glob A glob pattern according 1-5 above
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     * @throws StorageException
     */
    @Override
    public TreeFolder openFolder(Path folder, String glob) throws StorageException {
        TreeFolder sFolder = openFolder(folder, glob, 0);
        return sFolder;
    }

    /**
     * Opens a folder and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link net.cworks.treefs.spi.TreeFolder#items()}.  The glob pattern should be
     * used to determine what child items will be loaded into {@link net.cworks.treefs.spi.TreeFolder#items()}
     *
     * Glob patterns
     * 1) * - match any number of characters
     * 2) ? - match exactly one character
     * 3) {} - match a collection of sub patterns (example *.{txt, pdf, docx})
     * 4) [] - match a set or characters or a range if a hyphen is used
     *         (example [A-Z] matches any uppercase letter)
     *         (example [abc] matches any of a, b, c
     * 5) *, ?, \ - must be escaped by \ to be matched
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @param glob A glob pattern according 1-5 above
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     * @throws StorageException
     */
    @Override
    public TreeFolder openFolder(Path folder, String glob, int maxLevels) throws StorageException {
        TreeFolder sFolder = null;
        if(isNullOrEmpty(glob)) {
            sFolder = openFolder(folder, maxLevels);
        } else if("*".equals(glob)) {
            sFolder = openFolder(folder, maxLevels);
        } else {
            FileSystem fs = folder.getFileSystem();
            final PathMatcher matcher = fs.getPathMatcher("glob:" + glob);
            DirectoryStream.Filter<Path> globFilter = new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path entry)  {
                    return matcher.matches(entry.getFileName());
                }
            };
            sFolder = openFolder(folder, globFilter, maxLevels);
        }

        return sFolder;
    }

    /**
     * Moves a file or folder to the Trash.
     *
     * If path does not exist then throw {@link net.cworks.treefs.spi.NoTreePathException}
     * If path is a folder and IS NOT EMPTY then throw {@link net.cworks.treefs.spi.TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link net.cworks.treefs.spi.NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @throws StorageException
     */
    @Override
    public void trash(Path path) throws StorageException {
        trash(path, false);
    }

    @Override
    public void trash(Path path, boolean force) throws StorageException {
        try {
            // this checks for existence and emptiness
            if(!force) {
                if (!isEmpty(path)) {
                    throw new TreeFolderNotEmptyException("folder " + path.toString() + "not empty", path);
                }
            }
        } catch(NoTreePathException ex)     { throw ex; }
          catch(NotATreeFolderException ex) { /* eat */ }

        // at this point it is a manged empty folder or managed file
        try {
            Path fullPath  = _prependRoot(path);
            Path trashPath = _toTrashPath(path);
            if(Files.isDirectory(fullPath)) {
                SystemMovePathOp movePathOp = new SystemMovePathOp(fullPath, trashPath);
                Files.walkFileTree(fullPath, movePathOp);
            } else if(Files.isRegularFile(fullPath)) {
                Files.createDirectories(trashPath.getParent());
                // move file
                Files.move(fullPath, trashPath,
                        StandardCopyOption.REPLACE_EXISTING);
                // move metadata file
                Files.move(
                        Paths.get(fullPath.toString() + FILE_METADATA_SUFFIX),
                        Paths.get(trashPath.toString() + FILE_METADATA_SUFFIX),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(Exception ex) {
            throw new StorageException(ex);
        }
    }

    /**
     * Moves a file or folder to the Trash if it exists
     *
     * If path does not exist then simply return and DO NOT THROW {@link net.cworks.treefs.spi.NoTreePathException}
     * If path is a folder and IS NOT EMPTY then throw {@link net.cworks.treefs.spi.TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link net.cworks.treefs.spi.NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @throws StorageException
     */
    @Override
    public void trashIfExists(Path path) throws StorageException {
        if(!exists(path)) {
            return;
        }
        trash(path);
    }

    /**
     * Permanently deletes a file or folder.
     *
     * If path does not exist in Trash then throw {@link net.cworks.treefs.spi.NoTreePathException}
     * If path is a folder the operation will delete both the folder and ALL children
     *
     * Successful completion of this method cannot be reversed.
     *
     * @param path A relative Path to the file or folder in trash
     * @throws StorageException
     */
    @Override
    public void delete(Path path) throws StorageException {
        if(!_existsInTrash(path)) {
            throw new NoTreePathException("path " + path.toString() + " not in trash.");
        }

        try {
            Path trashPath = _prependTrashRoot(path);
            SystemDeletePathOp deletePathOp = new SystemDeletePathOp();
            Files.walkFileTree(trashPath, deletePathOp);
        } catch(Exception ex) {
            throw new StorageException(
                "error deleting path: " + path.toString(), ex);
        }
    }

    /**
     * Permanently deletes a file or folder.
     *
     * If path does not exist then simply return and DO NOT THROW {@link net.cworks.treefs.spi.NoTreePathException}
     * If path is a folder the operation will delete both the folder and ALL children
     *
     * Successful completion of this method cannot be reversed.
     *
     * @param path A relative Path to the file or folder in trash
     * @throws StorageException
     */
    @Override
    public void deleteIfExists(Path path) throws StorageException {
        if(!_existsInTrash(path)) {
            return;
        }
        delete(path);
    }

    /**
     * Restore a file or folder from the trash.  If path is a folder then restore all sub items.
     *
     * If path does not exist in Trash then throw {@link net.cworks.treefs.spi.NoTreePathException}
     * If restore path exists then throw
     * {@link net.cworks.treefs.spi.TreePathExistsException} and abort restore operation
     *
     * @param from A relative Path to the file or folder in trash
     * @param to A relative Path to restore the file or folder to
     * @throws StorageException
     */
    @Override
    public void restore(Path from, Path to) throws StorageException {

    }

    /**
     * Restore a file or folder from the trash if it exists in trash.  If path is a folder then
     * restore all sub items.
     *
     * If path does not exist then simply return and DO NOT THROW {@link net.cworks.treefs.spi.NoTreePathException}
     * If restore path exists then throw {@link net.cworks.treefs.spi.TreePathExistsException}
     * and abort restore operation
     *
     * @param from A relative Path to the file or folder in trash
     * @param to A relative Path to restore the file or folder to
     * @throws StorageException
     */
    @Override
    public void restoreIfExists(Path from, Path to) throws StorageException {

    }

    /**
     * Opens a folder in the trash and returns a {@link net.cworks.treefs.spi.TreeFolder} instance that contains information
     * about the folder and ONE level of children {@link net.cworks.treefs.spi.TreeFolder#items()}
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link net.cworks.treefs.spi.TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @return folder as a {@link net.cworks.treefs.spi.TreeFolder}
     * @throws StorageException
     */
    @Override
    public TreeFolder openTrash(Path folder) throws StorageException {
        return null;
    }

    /**
     * Tests if a file or folder exists and returns true otherwise false
     *
     * DO NOT throw {@link net.cworks.treefs.spi.NoTreePathException} if path does not exist, just return false
     *
     * @param path A relative Path to the file or folder to test for existence
     * @return true if folder exists otherwise false
     * @throws StorageException
     */
    @Override
    public boolean exists(Path path) throws StorageException {
        if(isNull(path)) {
            return false;
        }
        Path fullPath = _prependRoot(path);
        boolean exists = false;
        try {
            exists = Files.exists(fullPath);
            // now test if the path contains the required metadata file
            // if not then this provider doesn't know anything about the path
            // ...its just a wayward path
            if(exists) {
                exists = SystemPathIO.hasMetadata(fullPath);
            }
        } catch(Exception ex) {
            throw new StorageException("exception performing exists for path: " + path, ex);
        }
        return exists;
    }

    /**
     * private version of {@link SystemStorageProvider#exists(java.nio.file.Path)} that catches
     * {@link StorageException} and handles fullPath instead of relative path argument.
     * @param fullPath
     * @return
     */
    boolean _exists(Path fullPath) {
        boolean exists = false;
        Path path = _removeRoot(fullPath);
        try {
            exists = exists(path);
        } catch(StorageException ex) {
            exists = false;
        }
        return exists;
    }

    /**
     * Test if a folder is empty and returns true otherwise false
     *
     * If folder does not exist then throw {@link net.cworks.treefs.spi.NoTreePathException}
     * If folder is not a folder then throw {@link net.cworks.treefs.spi.NotATreeFolderException}
     *
     * @param folder A relative Path to the folder to test for emptiness
     * @return true if folder exists otherwise false
     * @throws StorageException
     */
    @Override
    public boolean isEmpty(Path folder) throws StorageException {
        // test if path exists and is managed by this SystemStorageProvider
        if(!exists(folder)) {
            throw new NoTreePathException(folder);
        }
        Path fullPath = _prependRoot(folder);
        if(_isManagedFile(fullPath)) {
            throw new NotATreeFolderException("folder " + folder.toString() + " is not a folder");
        }

        TreeFolder sFolder = openFolder(folder, 1);
        return !sFolder.hasItems();
    }

    /**
     * Test if a path is a valid Folder
     * @param path to test
     * @return true if path is Folder otherwise false
     * @throws StorageException
     */
    @Override
    public boolean isFolder(Path path) throws StorageException {

        Path fullPath = _prependRoot(path);
        boolean isFolder = _isManagedFolder(fullPath);

        return isFolder;
    }

    /**
     * Test if a path is a valid File
     * @param path to test
     * @return true if path is File otherwise false
     * @throws StorageException
     */
    @Override
    public boolean isFile(Path path) throws StorageException {

        Path fullPath = _prependRoot(path);
        boolean isFile = _isManagedFile(fullPath);

        return isFile;
    }

    /**
     * Copy a file or folder from source to a target.
     *
     * If source is a folder and SCopyOption.RECURSIVE is passed then copy source and all children
     * to target
     *
     * If source is a folder and SCopyOption.RECURSIVE IS NOT passed then just copy the source
     * folder to target
     *
     * If the path to the target folder exists then NO exceptions should be raised
     *
     * If the path to the target folder does not exist then it should be created
     *
     * However if the target folder exists and
     * {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} is not passed then throw
     * {@link net.cworks.treefs.spi.TreePathExistsException} otherwise if should be replace the existing
     * folder or file.
     *
     * If source path does not exist then throw {@link net.cworks.treefs.spi.NoTreePathException}
     *
     * This method call should not result in the source being changed
     *
     * In case of a failed copy any target folders and files that were created should be removed as
     * if this method call was never made...it never happened, you didn't see anything, this is not
     * the operation your looking for.  And the source should be left in the state it was prior to
     * this method being called.
     *
     * TODO extra notes:
     * Cant copy file into a folder that does not exist
     * Can copy a folder into a folder that does not exist
     *
     * @param source the source file or folder to copy from
     * @param target the target file or folder to copy to
     * @param options options that affect the copy
     *     {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} and
     *     {@link net.cworks.treefs.spi.TreeCopyOption}
     * @throws StorageException
     */
    @Override
    public void copy(Path source, Path target, CopyOption... options) throws StorageException {
        // 1) dir  to dir    cp -R source target,                     target/source
        // 2) dir  to file   error
        // 3) file to dir    cp someFile.txt target,                  target/someFile.txt
        // 4) file to file   cp someFile.txt target/copySomeFile.txt, target/copySomeFile.txt

        if(!exists(source)) {
            throw new NoTreePathException("source " + source + " does not exist");
        }

        try {
            final Path sourcePath = _prependRoot(source);
            final Path targetPath = _prependRoot(target);
            boolean recursive = _hasCopyRecursive(options);
            boolean into      = _hasCopyIntoOption(options);
            // copying folder to...
            if(Files.isDirectory(sourcePath)) {
                if(!exists(target)) {
                    throw new NoTreePathException("target " + target + " does not exist");
                }
                if(recursive) {
                    if(into) {
                        // cp source target/source
                        FileUtils.copyDirectoryToDirectory(
                                sourcePath.toFile(),
                                targetPath.toFile());
                        _indexPaths(sourcePath, targetPath, options);
                    } else {
                        // cp source/* target
                        FileUtils.copyDirectory(
                                sourcePath.toFile(),
                                targetPath.toFile(),
                                new FileFilter() {
                                    @Override
                                    public boolean accept(File pathname) {
                                        return !pathname.getPath().equals(
                                                sourcePath.resolve(
                                                        sourcePath.getFileName() + FOLDER_METADATA_SUFFIX)
                                                        .toString());
                                    }
                                });
                        _indexPaths(sourcePath, targetPath, options);
                    }
                }
            // copying file to ...
            } else if(Files.isRegularFile(sourcePath)) {
                boolean success = _tryCopyToFolder(sourcePath, targetPath, options);
                if(success) {
                    return;
                }
                success = _tryCopyFileToFileNoRename(sourcePath, targetPath, options);
                if(success) {
                    return;
                }
            }
        } catch(IOException ex) {
            throw new StorageException(ex);
        }
    }

    /**
     * Move a file or folder from source to a target.
     *
     * If source is a folder and SCopyOption.RECURSIVE is passed then move source and all children
     * to target
     *
     * If source is a folder and SCopyOption.RECURSIVE IS NOT passed then just move the EMPTY
     * source folder to target
     *
     * If the path to the target folder exists then NO exceptions should be raised
     *
     * If the path to the target folder does not exist then it should be created
     *
     * However if the target folder exists and
     * {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} is not passed then throw
     * {@link net.cworks.treefs.spi.TreePathExistsException} otherwise it should replace the existing
     * folder or file.
     *
     * If source path does not exist then throw {@link net.cworks.treefs.spi.NoTreeFolderException}
     *
     * On success the source should be completely moved to target
     *
     * On failure any target folders and files that were created should be removed as if this
     * method call was never made...it never happened, you didn't see anything, this is not the
     * operation your looking for.  And the source should be left in the state it was prior to this
     * method being called.
     *
     * @param source the source file or folder to move from
     * @param target the target file or folder to move to
     * @param options options that affect the move {@link java.nio.file.StandardCopyOption}
     *                and {@link net.cworks.treefs.spi.TreeCopyOption}
     * @throws StorageException
     */
    @Override
    public void move(Path source, Path target, CopyOption... options) throws StorageException {
        copy(source, target, options);
        trash(source, true);
    }

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link net.cworks.treefs.spi.TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @throws StorageException
     */
    @Override
    public TreeFile createFile(Path path, InputStream stream) throws StorageException {
        TreeFile sFile = createFile(path, stream, new HashMap<String, Object>());
        return sFile;
    }

    @Override
    public TreeFile createFile(String path, File file) throws StorageException {
        TreeFile sFile = createFile(Paths.get(path), file);
        return sFile;
    }

    @Override
    public TreeFile createFile(Path path, File file) throws StorageException {
        InputStream stream = null;
        try {
            stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        } catch (IOException ex) {
            throw new StorageException(ex);
        } finally {
            closeQuietly(stream);
        }

        TreeFile sFile = createFile(path, stream);
        return sFile;
    }

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream and associates the given description with it.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link net.cworks.treefs.spi.TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @param description the description to associate with the new file
     * @throws StorageException
     */
    @Override
    public TreeFile createFile(Path path, InputStream stream, String description) throws StorageException {
        TreeFile sFile = createFile(path, stream, description, null);
        return sFile;
    }

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream and associates the given metadata with it.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link net.cworks.treefs.spi.TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @param metadata the application specific metadata to associate with the new file
     * @throws StorageException
     */
    @Override
    public TreeFile createFile(Path path, InputStream stream, Map<String, Object> metadata)
        throws StorageException {
        TreeFile sFile = createFile(path, stream, null, metadata);
        return sFile;
    }

    @Override
    public TreeFile createFile(Path path, File file, Map<String, Object> metadata)
        throws StorageException {

        InputStream stream = null;
        SystemFile sFile = null;
        try {
            Path source = file.toPath();
            stream = Files.newInputStream(source, StandardOpenOption.READ);
            sFile = (SystemFile)createFile(path, stream, metadata);
            try {
                BasicFileAttributes attributes = Files.readAttributes(source,
                    BasicFileAttributes.class);
                sFile.size(attributes.size());
            } catch (IOException e) { /* eat and don't set size attribute */ }
        } catch (IOException ex) {
            throw new StorageException(ex);
        } finally {
            closeQuietly(stream);
        }

        return sFile;
    }

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream and associates the given description and metadata with it.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link net.cworks.treefs.spi.TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @param description the description to associate with the new file
     * @param metadata the application specific metadata to associate with the new file
     * @throws StorageException
     */
    @Override
    public TreeFile createFile(Path path, InputStream stream, String description,
        Map<String, Object> metadata) throws StorageException {

        if(isNull(path) || isNull(stream)) {
            throw new IllegalArgumentException(
                "Both path and stream are required arguments and cannot be null.");
        }

        if(exists(path)) {
            throw new TreeFileExistsException("file: " + path.toString() + " exists", path);
        }

        Path fullPath = _prependRoot(path);
        OutputStream out = null;
        try {
            _createDirectoriesForFile(path);
            out = Files.newOutputStream(fullPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
            long nread = 0L;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int n;
            while ((n = stream.read(buffer)) > 0) {
                out.write(buffer, 0, n);
                nread += n;
            }
            System.out.println("createFile: " + fullPath.toFile() + " size=" + nread + " bytes");
        } catch(Exception ex) {
            throw new StorageException("exception creating file: "
                + fullPath.getFileName().toString(), ex);
        } finally {
            closeQuietly(out);
        }

        SystemFile systemFile = null;
        try {
            SystemFileMaker maker = SystemFileMaker.newFile().withRoot(root);
            _loadFileAttributes(fullPath, maker);
            if(!isNullOrEmpty(description)) {
                maker.withDescription(description);
            }
            if(!isNullOrEmpty(metadata)) {
                maker.withMetadata(metadata);
            }
            systemFile = maker.make();
            SystemPathIO.createMetadata(systemFile);
        } catch(Exception ex) {
            throw new StorageException("exception creating metadata for file: "
                + fullPath.getFileName().toString(), ex);
        }

        // TODO fix me!
        return systemFile;
    }

    /**
     * Opens an existing file for reading and returns an InputStream to read the file content from
     *
     * If the file path does not exist then {@link net.cworks.treefs.spi.NoTreePathException} should be thrown
     * null should never be returned, only a viable InputStream or an exception subclass of
     * {@link StorageException}
     *
     * @param path the path to the file to read
     * @return a valid InputStream to read file content from
     * @throws StorageException
     */
    @Override
    public InputStream read(Path path) throws StorageException {

        InputStream in = null;
        Path fullPath = _prependRoot(path);

        try {
            in = Files.newInputStream(fullPath, StandardOpenOption.READ);
            if(isNull(in)) {
                throw new StorageException("unable to open path: " + path + " for reading.");
            }
        } catch(Exception ex) {
            throw new StorageException(ex);
        }

        return in;
    }

    /**
     * Returns application specific metadata for a given file or folder
     *
     * If the path does not exist then {@link net.cworks.treefs.spi.NoTreePathException} should be thrown
     * If the file or folder does not have metadata then null should be returned
     *
     * @param path the path to the file or folder from which metadata is requested
     * @return null if no metadata exists or a {@link java.util.Map} containing application
     *     specific metadata
     * @throws StorageException
     */
    @Override
    public Map<String, Object> readMetadata(Path path) throws StorageException {

        Path fullPath = _prependRoot(path);
        Map<String, Object> metadata = null;
        if(!exists(path)) {
            throw new NoTreePathException(path);
        }

        try {
            metadata = SystemPathIO.readMetadata(fullPath);
        } catch (IOException e) {
            throw new StorageException("Sorry to shatter your dreams but we couldn't get metadata for: " + path);
        }

        return metadata;
    }

    /**
     * Compares the glob pattern against the file or folder name and returns only the first match
     * or null if no match is found.
     *
     * If the path does not exist then {@link net.cworks.treefs.spi.NoTreePathException} should be thrown
     *
     * Glob patterns
     * 1) * - match any number of characters
     * 2) ? - match exactly one character
     * 3) {} - match a collection of sub patterns (example *.{txt, pdf, docx})
     * 4) [] - match a set or characters or a range if a hyphen is used
     *         (example [A-Z] matches any uppercase letter)
     *         (example [abc] matches any of a, b, c
     * 5) *, ?, \ - must be escaped by \ to be matched
     *
     * @param path the folder to start searching from
     * @param glob the glob filter pattern to use
     * @return the first {@link net.cworks.treefs.spi.TreePath} matched
     */
    @Override
    public TreePath findFirst(Path path, String glob) throws StorageException {
        return null;
    }

    /**
     * Compares the glob pattern against the file or folder name and returns first match or null
     * if no match is found.  Search implementation should only consider maxLevels deep.
     *
     * If the path does not exist then {@link net.cworks.treefs.spi.NoTreePathException} should be thrown
     *
     * Glob patterns
     * 1) * - match any number of characters
     * 2) ? - match exactly one character
     * 3) {} - match a collection of sub patterns (example *.{txt, pdf, docx})
     * 4) [] - match a set or characters or a range if a hyphen is used
     *         (example [A-Z] matches any uppercase letter)
     *         (example [abc] matches any of a, b, c
     * 5) *, ?, \ - must be escaped by \ to be matched
     *
     * @param path the folder to start searching from
     * @param glob the glob filter pattern to use
     * @param maxLevels the number of levels deep to consider
     * @return the first {@link net.cworks.treefs.spi.TreePath} matched
     */
    @Override
    public TreePath findFirst(Path path, String glob, int maxLevels) throws StorageException {
        return null;
    }

    /**
     * Compares the glob pattern against the file or folder name and return a list of {@link net.cworks.treefs.spi.TreePath}
     * instance that match the glob pattern or null if no match is found.
     *
     * If the path does not exist then {@link net.cworks.treefs.spi.NoTreePathException} should be thrown
     *
     * Glob patterns
     * 1) * - match any number of characters
     * 2) ? - match exactly one character
     * 3) {} - match a collection of sub patterns (example *.{txt, pdf, docx})
     * 4) [] - match a set or characters or a range if a hyphen is used
     *         (example [A-Z] matches any uppercase letter)
     *         (example [abc] matches any of a, b, c
     * 5) *, ?, \ - must be escaped by \ to be matched
     *
     * @param path the folder to start searching from
     * @param glob the glob filter pattern to use
     * @return a list of matched {@link net.cworks.treefs.spi.TreePath}
     */
    @Override
    public List<TreePath> find(Path path, String glob) throws StorageException {
        return null;
    }

    /**
     * Compares the glob pattern against the file and/or folder name and returns all matches or null
     * if no match is found.  Search implementation should only consider maxLevels deep.
     *
     * If the path does not exist then {@link net.cworks.treefs.spi.NoTreePathException} should be thrown
     *
     * Glob patterns
     * 1) * - match any number of characters
     * 2) ? - match exactly one character
     * 3) {} - match a collection of sub patterns (example *.{txt, pdf, docx})
     * 4) [] - match a set or characters or a range if a hyphen is used
     *         (example [A-Z] matches any uppercase letter)
     *         (example [abc] matches any of a, b, c
     * 5) *, ?, \ - must be escaped by \ to be matched
     *
     * @param path
     * @param glob
     * @param maxLevels
     * @return a list of matched {@link net.cworks.treefs.spi.TreePath}
     */
    @Override
    public List<TreePath> find(Path path, String glob, int maxLevels) throws StorageException {
        return null;
    }

    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    // internal methods
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //

    /**
     * Root path as a String
     * @return
     */
    String _rootAsString() {
        return root.toFile().getPath();
    }

    Path _root() {
        return root;
    }

    String _mount() {
        return this.mount;
    }

    void _mount(String mount) {
        this.mount = mount;
        this.root = Paths.get(_mount(), _bucket());
    }

    String _bucket() {
        return this.bucket;
    }

    void _bucket(String bucket) {
        this.bucket = bucket;
        this.root = Paths.get(_mount(), _bucket());
    }

    /**
     * Prepend root on relative path
     * @param relative
     * @return
     */
    private Path _prependRoot(Path relative) {
        return this.root.resolve(relative);
    }

    /**
     * Prepend root of the trash onto the relative path
     * @param relativeTrashPath
     * @return
     */
    private Path _prependTrashRoot(Path relativeTrashPath) {
        //String to = relativeTrashPath.toString().replace("/", "+");
        String to = relativeTrashPath.toString().replace("\\", "/");
        return Paths.get(SystemConfig.trashDir(), bucket, to);
    }

    /**
     * Check if path exists in trash
     * @param relative
     * @return
     */
    private boolean _existsInTrash(Path relative) {
        Path trashPath = _prependTrashRoot(relative);
        return Files.exists(trashPath);
    }

    /**
     * Remove root from absolute Path
     * @param absolute
     * @return
     */
    Path _removeRoot(Path absolute) {
        return this.root.relativize(absolute);
    }

    /**
     * Create directories required from top to bottom for a directory
     * @param path
     */
    private void _createFolders(Path path) throws StorageException {
        // start creating directory path
        if(path.getNameCount() > 0) {
            Iterator<Path> it = path.iterator();
            Path parent = Paths.get(_rootAsString());
            while(it.hasNext()) {
                Path subdir = it.next();
                try {
                    if(!exists(parent.resolve(subdir))) {
                        Files.createDirectory(parent.resolve(subdir));
                        SystemFolderMaker maker = SystemFolderMaker.newFolder().withRoot(root);
                        _loadFolderAttributes(parent.resolve(subdir), maker);
                        SystemPathIO.createSystemFolder(maker.make());
                    }
                } catch(IOException ex) { /* ignore */ }
                // deepen the parent
                parent = parent.resolve(subdir);
            }
        }
    }

    /**
     * Create directories required from top to bottom for a file
     * @param path
     * @throws StorageException
     */
    private void _createDirectoriesForFile(Path path) throws StorageException {
        // start creating directory path
        if(path.getNameCount() > 1) {
            Iterator<Path> it = path.iterator();
            Path parent = Paths.get(_rootAsString());
            while(it.hasNext()) {
                Path subdir = it.next();
                if(subdir.equals(path.getFileName())) {
                    break;
                }
                try {
                    if(!exists(parent.resolve(subdir))) {
                        Files.createDirectory(parent.resolve(subdir));
                        SystemFolderMaker maker = SystemFolderMaker.newFolder().withRoot(root);
                        _loadFolderAttributes(parent.resolve(subdir), maker);
                        SystemPathIO.createSystemFolder(maker.make());
                    }
                } catch(IOException ex) { /* ignore */ }
                // deepen the parent
                parent = parent.resolve(subdir);
            }
        }
    }

    private void _loadFolderAttributes(Path fullPath, SystemFolderMaker maker) throws IOException {
        maker.withFullPath(fullPath)
            .withName(fullPath.getFileName().toString());
        // like file attributes
        BasicFileAttributes attributes = Files.readAttributes(fullPath, BasicFileAttributes.class);
        if(!isNull(attributes)) {
            maker.withCreationTime(new Date(attributes.creationTime().toMillis()))
                .withLastAccessedTime(new Date(attributes.lastAccessTime().toMillis()))
                .withLastModifiedTime(new Date(attributes.lastModifiedTime().toMillis()));
        }
    }

    private void _loadFileAttributes(Path fullPath, SystemFileMaker maker) throws IOException {
        maker.withFullPath(fullPath)
            .withName(fullPath.getFileName().toString());
        // like file attributes
        BasicFileAttributes attributes = Files.readAttributes(fullPath, BasicFileAttributes.class);
        if(!isNull(attributes)) {
            maker.withCreationTime(new Date(attributes.creationTime().toMillis()))
                .withLastAccessedTime(new Date(attributes.lastAccessTime().toMillis()))
                .withLastModifiedTime(new Date(attributes.lastModifiedTime().toMillis()))
                .withSize(attributes.size());
        }
    }

    /**
     * Load valid SystemPath instances that are inside the given SystemFolder
     * @param ds
     * @param systemFolder
     * @throws StorageException
     * @throws IOException
     */
    private void _loadFolderItems(DirectoryStream<Path> ds, SystemFolder systemFolder)
        throws StorageException, IOException {
        List<TreePath> items = new ArrayList<>();
        // set the sub-paths in this folder (i.e. 1 level deep only)
        Iterator<Path> it = ds.iterator();
        while(it.hasNext()) {
            Path path = it.next();
            // verify path is known by SystemStorageProvider
            if(!exists(path)) {
                break;
            }
            SystemPath systemPath = SystemPathIO.readSystemPath(path);
            if(systemPath != null) {
                items.add(systemPath);
            }
        }
        if(items.size() > 0) {
            systemFolder.items(items);
        }
    }

    /**
     * Tests if a full path contains any files or folders
     * @param fullPath
     */
    private boolean _containsUnmanaged(Path fullPath) throws IOException {
        DirectoryStream<Path> ds = null;
        boolean contains = true;
        try {
            ds = Files.newDirectoryStream(fullPath, new LocalPathFilter());
            contains = ds.iterator().hasNext();
        } finally {
            closeQuietly(ds);
        }
        return contains;
    }

    /**
     * Converts a relative path passed from caller into a path in the trash
     * @param path
     * @return
     */
    private Path _toTrashPath(Path path) {
        //String trashy = path.toString().replace("/", "+").replace("\\", "+");
        Path trashPath = Paths.get(SystemConfig.trashDir(),
                bucket,
                path.toString());
        return trashPath;
    }

    /**
     * Blows away any unmanaged files and folders in the fullPath but does not remove the fullPath
     * @param fullPath
     */
    private void _blowAwayUnmanaged(Path fullPath) throws IOException {
        DirectoryStream<Path> ds = null;
        try {
            ds = Files.newDirectoryStream(fullPath, new LocalPathFilter());
            Iterator<Path> it = ds.iterator();
            SystemDeletePathOp deleteOp = new SystemDeletePathOp();
            while(it.hasNext()) {
                Path item = it.next();
                if(Files.isDirectory(item)) {
                    Files.walkFileTree(it.next(), deleteOp);
                } else if(Files.isRegularFile(item)) {
                    Files.delete(item);
                }
            }
        } finally {
            closeQuietly(ds);
        }
    }

    boolean _isManagedFolder(Path fullPath) {
        SystemFolder folder = null;
        try {
            folder = SystemPathIO.readSystemFolder(fullPath);
        } catch (IOException e) {
            return false;
        }
        if(isNull(folder)) {
            return false;
        }
        return true;
    }

    boolean _isManagedFile(Path fullPath) {
        SystemFile file = null;
        try {
            file = SystemPathIO.readSystemFile(fullPath);
        } catch (IOException e) {
            return false;
        }
        if(isNull(file)) {
            return false;
        }
        return true;
    }

    /**
     * Check if path ends in the wildcard '*'
     * @param path
     * @return
     */
    private boolean _hasWildcard(Path path) {
        String temp = path.toString().replace("\\", "/");
        return temp.endsWith("/*");
    }

    /**
     * Returns true if one of the options is SCopyOption.RECURSIVE
     * @param options
     * @return
     */
    private boolean _hasCopyRecursive(CopyOption[] options) {
        for(CopyOption option : options) {
            if(option == TreeCopyOption.RECURSIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if COPY_INTO is set in the options
     * @param options
     * @return
     */
    private boolean _hasCopyIntoOption(CopyOption[] options) {
        for(CopyOption option : options) {
            if(option == TreeCopyOption.INTO) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if StandardCopyOption.REPLACE_EXISTING is set in the options
     * @param options
     * @return
     */
    private boolean _hasReplaceOption(CopyOption[] options) {
        for(CopyOption option : options) {
            if(option == StandardCopyOption.REPLACE_EXISTING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fixes path field in SystemPath files to reflect the proper relative location from
     * root.  Typically this needs to be invoked after a move or copy operation.
     *
     * @param sourcePath
     * @param targetPath
     */
    private void _indexPaths(Path sourcePath, Path targetPath, CopyOption... options) throws IOException {
        SystemIndexOp indexOp = new SystemIndexOp(this, Files.isRegularFile(sourcePath));
        if(_hasCopyIntoOption(options)) {
            Files.walkFileTree(
                Paths.get(targetPath.toString(), sourcePath.getFileName().toString()),
                indexOp);
        } else {
            Files.walkFileTree(targetPath, indexOp);
        }

    }

    /**
     * Attempt to copy a file (sourcePath) into a directory (targetPath) and return true if
     * operation is successful otherwise return false.
     *
     * Assumptions: sourcePath is a confirmed managed file
     *
     * @param sourcePath
     * @param targetPath
     * @param options
     * @return
     */
    private boolean _tryCopyToFolder(Path sourcePath, Path targetPath, CopyOption ...options)
        throws TreePathExistsException, IOException {

        boolean success = false;
        // if target is a folder, then we check for collisions and if all clear we copy
        if(_isManagedFolder(targetPath)) {
            Collection<File> collisions = FileUtils.listFiles(
                    targetPath.toFile(),
                    TrueFileFilter.INSTANCE,
                    TrueFileFilter.INSTANCE);
            for(File file : collisions) {
                if(file.getName().equals(sourcePath.getFileName().toString())) {
                    if(!_hasReplaceOption(options)) {
                        // collision and overwrite/replace not specified as option
                        throw new TreePathExistsException("target file exist", _removeRoot(targetPath));
                    }
                }
            }
            FileUtils.copyFileToDirectory(sourcePath.toFile(), targetPath.toFile());
            FileUtils.copyFileToDirectory(
                    new File(sourcePath.toString() + FILE_METADATA_SUFFIX),
                    targetPath.toFile());
            _indexPaths(sourcePath, targetPath, options);
            success = true;
        } else {
            success = false;
        }

        return success;
    }

    /**
     * Attempt to copy a file (sourcePath) to a file with the same name but different path
     * (targetPath)
     *
     * Assumptions: sourcePath is a confirmed managed file
     *
     * @param sourcePath
     * @param targetPath
     * @param options
     * @return
     */
    private boolean _tryCopyFileToFileNoRename(Path sourcePath, Path targetPath,
        CopyOption ...options) throws TreePathExistsException, IOException {

        // sourcePath and targetPath must not be exactly equal because we don't copy same
        // file to same file
        if(targetPath.equals(sourcePath)) {
            throw new TreePathExistsException("cannot copy a file into itself",
                    _removeRoot(sourcePath));
        }
        // file to file with no rename, so names must be same
        //if(!targetPath.getFileName().equals(sourcePath.getFileName())) {
        //    return false;
        //}
        // target files path must exist as a managed folder
        if(!_exists(targetPath.getParent())) {
            return false;
        }

        // I have no idea what this is for, leaving commented out just in case it comes to me
        // man getting old is going to be fun!
//        Collection<File> collisions = FileUtils.listFiles(
//                targetPath.getParent().toFile(),
//                TrueFileFilter.INSTANCE,
//                TrueFileFilter.INSTANCE);
//        for(File file : collisions) {
//            if(file.getName().equals(sourcePath.getFileName().toString())) {
//                if(!_hasReplaceOption(options)) {
//                    // collision and overwrite/replace not specified as option
//                    throw new SPathExistsException("target file exist and overwrite not specified",
//                        _removeRoot(targetPath));
//                }
//            }
//        }
        // perform file copy
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
            Paths.get(sourcePath.toString() + FILE_METADATA_SUFFIX),
            Paths.get(targetPath.toString() + FILE_METADATA_SUFFIX),
                StandardCopyOption.REPLACE_EXISTING);
        _indexPaths(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

}
