package cworks.treefs.spi;

import java.io.File;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface StorageProvider {

    /**
     * Creates a folder by creating all nonexistent parent folders first then creates the target
     * folder.  If parent folders already exist then no exception should be thrown and the
     * implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw {@link TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     */
    public TreeFolder createFolder(Path folder) throws StorageException;

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * metadata with the new folder.  If parent folders already exist then no exception should be
     * thrown and the implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw {@link TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param metadata Map that contains application specific metadata
     */
    public TreeFolder createFolder(Path folder, Map<String, Object> metadata) throws StorageException;

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * description with the new folder.  If parent folders already exist then no exception should
     * be thrown and the implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw {@link TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param description A description that should be associated with the given folder
     * @throws StorageException
     */
    public TreeFolder createFolder(Path folder, String description) throws StorageException;

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * metadata and description with the new folder.  If parent folders already exist then no
     * exception should be thrown and the implementation should cause no side-effects on existing
     * parent folders.
     *
     * If the target folder already exists then throw {@link TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param description A description that should be associated with the given folder
     * @param metadata Map that contains application specific metadata
     * @throws StorageException
     */
    public TreeFolder createFolder(Path folder, String description, Map<String, Object> metadata)
        throws StorageException;

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @return folder as a {@link TreeFolder}
     */
    public TreeFolder openFolder(Path folder) throws StorageException;

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link TreeFolder#items()} loaded.
     *
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link TreeFolder}
     */
    public TreeFolder openFolder(Path folder, int maxLevels) throws StorageException;

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link TreeFolder#items()}.  The filter should be used
     * to determine what child items will be loaded into {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param filter A filter to apply to the children in this folder
     * @return folder as a {@link TreeFolder}
     */
    public TreeFolder openFolder(Path folder, DirectoryStream.Filter<Path> filter)
            throws StorageException;

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link TreeFolder#items()}.  The filter should be used
     * to determine what child items will be loaded into {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param filter A filter to apply to the children in this folder
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link TreeFolder}
     */
    public TreeFolder openFolder(Path folder, DirectoryStream.Filter<Path> filter, int maxLevels)
        throws StorageException;

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link TreeFolder#items()}.  The glob pattern should be used
     * to determine what child items will be loaded into {@link TreeFolder#items()}
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
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param glob A glob pattern according 1-5 above
     * @return folder as a {@link TreeFolder}
     * @throws StorageException
     */
    public TreeFolder openFolder(Path folder, String glob) throws StorageException;

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link TreeFolder#items()}.  The glob pattern should be
     * used to determine what child items will be loaded into {@link TreeFolder#items()}
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
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @param glob A glob pattern according 1-5 above
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link TreeFolder}
     * @throws StorageException
     */
    public TreeFolder openFolder(Path folder, String glob, int maxLevels) throws StorageException;

    /**
     * Moves a file or folder to the Trash.
     *
     * If path does not exist then throw {@link NoTreePathException}
     * If path is a folder and IS NOT EMPTY then throw {@link TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @throws StorageException
     */
    public void trash(Path path) throws StorageException;

    /**
     * Moves a file or folder to the Trash.
     *
     * If path does not exist then throw {@link NoTreePathException}
     *
     * If force is true then trashing NON EMPTY folders is allowed otherwise...
     * If path is a folder and IS NOT EMPTY then throw {@link TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @param force if true move path into trash even if its non-empty
     * @throws StorageException
     */
    public void trash(Path path, boolean force) throws StorageException;

    /**
     * Moves a file or folder to the Trash if it exists
     *
     * If path does not exist then simply return and DO NOT THROW {@link NoTreePathException}
     * If path is a folder and IS NOT EMPTY then throw {@link TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @throws StorageException
     */
    public void trashIfExists(Path path) throws StorageException;

    /**
     * Permanently deletes a file or folder.
     *
     * If path does not exist in Trash then throw {@link NoTreePathException}
     * If path is a folder the operation will delete both the folder and ALL children
     *
     * Successful completion of this method cannot be reversed.
     *
     * @param path A relative Path to the file or folder in trash
     * @throws StorageException
     */
    public void delete(Path path) throws StorageException;

    /**
     * Permanently deletes a file or folder.
     *
     * If path does not exist then simply return and DO NOT THROW {@link NoTreePathException}
     * If path is a folder the operation will delete both the folder and ALL children
     *
     * Successful completion of this method cannot be reversed.
     *
     * @param path A relative Path to the file or folder in trash
     * @throws StorageException
     */
    public void deleteIfExists(Path path) throws StorageException;

    /**
     * Restore a file or folder from the trash.  If path is a folder then restore all sub items.
     *
     * If path does not exist in Trash then throw {@link NoTreePathException}
     * If restore path exists then throw {@link TreePathExistsException} and abort restore operation
     *
     * @param from A relative Path to the file or folder in trash
     * @param to A relative Path to restore the file or folder to
     * @throws StorageException
     */
    public void restore(Path from, Path to) throws StorageException;

    /**
     * Restore a file or folder from the trash if it exists in trash.  If path is a folder then
     * restore all sub items.
     *
     * If path does not exist then simply return and DO NOT THROW {@link NoTreePathException}
     * If restore path exists then throw {@link TreePathExistsException} and abort restore operation
     *
     * @param from A relative Path to the file or folder in trash
     * @param to A relative Path to restore the file or folder to
     * @throws StorageException
     */
    public void restoreIfExists(Path from, Path to) throws StorageException;

    /**
     * Opens a folder in the trash and returns a {@link TreeFolder} instance that contains information
     * about the folder and ONE level of children {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not actually a folder then throw {@link NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @return folder as a {@link TreeFolder}
     * @throws StorageException
     */
    public TreeFolder openTrash(Path folder) throws StorageException;

    /**
     * Tests if a file or folder exists and returns true otherwise false
     *
     * DO NOT throw {@link NoTreePathException} if path does not exist, just return false
     *
     * @param path A relative Path to the file or folder to test for existence
     * @return true if folder exists otherwise false
     * @throws StorageException
     */
    public boolean exists(Path path) throws StorageException;

    /**
     * Test if a folder is empty and returns true otherwise false
     *
     * If folder does not exist then throw {@link NoTreeFolderException}
     * If folder is not a folder then throw {@link NotATreeFolderException}
     *
     * @param folder A relative Path to the folder to test for emptiness
     * @return true if folder exists otherwise false
     * @throws StorageException
     */
    public boolean isEmpty(Path folder) throws StorageException;

    /**
     * Test if a path is a valid Folder
     * @param path to test
     * @return true if path is Folder otherwise false
     * @throws StorageException
     */
    public boolean isFolder(Path path) throws StorageException;

    /**
     * Test if a path is a valid File
     * @param path to test
     * @return true if path is a File otherwise false
     * @throws StorageException
     */
    public boolean isFile(Path path) throws StorageException;

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
     * {@link TreePathExistsException} otherwise if should be replace the existing folder or file.
     *
     * If source path does not exist then throw {@link NoTreeFolderException}
     *
     * This method call should not result in the source being changed
     *
     * In case of a failed copy any target folders and files that were created should be removed as
     * if this method call was never made...it never happened, you didn't see anything, this is not
     * the operation your looking for.  And the source should be left in the state it was prior to
     * this method being called.
     *
     * @param source the source file or folder to copy from
     * @param target the target file or folder to copy to
     * @param options options that affect the copy
     *     {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} and {@link TreeCopyOption}
     * @throws StorageException
     */
    public void copy(Path source, Path target, CopyOption...options) throws StorageException;

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
     * {@link TreePathExistsException} otherwise it should replace the existing folder or file.
     *
     * If source path does not exist then throw {@link NoTreeFolderException}
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
     *                and {@link TreeCopyOption}
     * @throws StorageException
     */
    public void move(Path source, Path target, CopyOption...options) throws StorageException;

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @throws StorageException
     */
    public TreeFile createFile(Path path, InputStream stream) throws StorageException;
    public TreeFile createFile(String path, File file) throws StorageException;
    public TreeFile createFile(Path path, File file) throws StorageException;

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream and associates the given description with it.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @param description the description to associate with the new file
     * @throws StorageException
     */
    public TreeFile createFile(Path path, InputStream stream, String description)
        throws StorageException;

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream and associates the given metadata with it.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @param metadata the application specific metadata to associate with the new file
     * @throws StorageException
     */
    public TreeFile createFile(Path path, InputStream stream, Map<String, Object> metadata)
        throws StorageException;

    public TreeFile createFile(Path path, File file, Map<String, Object> metadata)
        throws StorageException;

    /**
     * Creates a new file on the path and with the name of the given path argument and writes data
     * into the new file from stream and associates the given description and metadata with it.
     *
     * If the file path does not exist it should be created
     * If the file already exists then throw {@link TreeFileExistsException}
     *
     * @param path the path and filename of the new file
     * @param stream the stream containing data for the file
     * @param description the description to associate with the new file
     * @param metadata the application specific metadata to associate with the new file
     * @throws StorageException
     */
    public TreeFile createFile(Path path, InputStream stream, String description,
        Map<String, Object> metadata) throws StorageException;

    /**
     * Opens an existing file for reading and returns an InputStream to read the file content from
     *
     * If the file path does not exist then {@link NoTreePathException} should be thrown
     * null should never be returned, only a viable InputStream or an exception subclass of
     * {@link StorageException}
     *
     * @param path the path to the file to read
     * @return a valid InputStream to read file content from
     * @throws StorageException
     */
    public InputStream read(Path path) throws StorageException;

    /**
     * Returns application specific metadata for a given file or folder
     *
     * If the path does not exist then {@link NoTreePathException} should be thrown
     * If the file or folder does not have metadata then null should be returned
     *
     * @param path the path to the file or folder from which metadata is requested
     * @return null if no metadata exists or a {@link java.util.Map} containing application
     *     specific metadata
     * @throws StorageException
     */
    public Map<String, Object> readMetadata(Path path) throws StorageException;

    /**
     * Compares the glob pattern against the file or folder name and returns only the first match
     * or null if no match is found.
     *
     * If the path does not exist then {@link NoTreePathException} should be thrown
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
     * @return the first {@link TreePath} matched
     */
    public TreePath findFirst(Path path, String glob) throws StorageException;

    /**
     * Compares the glob pattern against the file or folder name and returns first match or null
     * if no match is found.  Search implementation should only consider maxLevels deep.
     *
     * If the path does not exist then {@link NoTreePathException} should be thrown
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
     * @return the first {@link TreePath} matched
     */
    public TreePath findFirst(Path path, String glob, int maxLevels) throws StorageException;

    /**
     * Compares the glob pattern against the file or folder name and return a list of {@link TreePath}
     * instance that match the glob pattern or null if no match is found.
     *
     * If the path does not exist then {@link NoTreePathException} should be thrown
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
     * @return a list of matched {@link TreePath}
     */
    public List<TreePath> find(Path path, String glob) throws StorageException;

    /**
     * Compares the glob pattern against the file and/or folder name and returns all matches or null
     * if no match is found.  Search implementation should only consider maxLevels deep.
     *
     * If the path does not exist then {@link NoTreePathException} should be thrown
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
     * @return a list of matched {@link TreePath}
     */
    public List<TreePath> find(Path path, String glob, int maxLevels) throws StorageException;

}
