package cworks.treefs.awssp;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.*;
import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.common.dt.ISO8601DateParser;
import cworks.treefs.spi.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static cworks.treefs.common.IOUtils.closeQuietly;
import static cworks.treefs.common.ObjectUtils.isNull;
import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;


/**
 * The AWS S3 (Simple Storage Service) StorageProvider implementation.  You may use this
 * StorageProvider implementation apart from TreeFs-Server but the intention was that
 * it would plug into TreeFs-Server so that we can use different storage providers to
 * persist content into.  You will need an AWS account and S3 credentials to sign S3
 * requests.
 *
 * AWS javadoc
 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/
 *
 * @author comartin
 */
public class S3StorageProvider implements StorageProvider {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(S3StorageProvider.class);

    /**
     * AmazonS3 instance this StorageProvider is delegating to
     */
    private AmazonS3 s3 = null;

    /**
     * Storage root in S3 that this StorageProvider is associated with
     */
    private String storageRoot = null;

    /**
     * Trash root in S3 for this StorageProvider
     */
    private String trashRoot = null;

    /**
     * Suffix appended onto storageRoot name to render the trash bucket
     */
    private static final String TRASH_SUFFIX = "-trash";

    /**
     * Default S3 Path Filter
     */
    private class S3PathFilter implements DirectoryStream.Filter<Path> {
        DirectoryStream.Filter<Path> delegate = null;
        S3PathFilter() {
            this.delegate = null;
        }
        S3PathFilter(DirectoryStream.Filter<Path> delegate) {
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
     * Create an instance by using create static method
     * @param s3 - the AmazonS3 instance we delegate to
     * @param storageRoot storage root that this instance is associated with
     */
    private S3StorageProvider(AmazonS3 s3, String storageRoot) {
        this.s3 = s3;
        this.storageRoot = storageRoot;
        this.trashRoot = storageRoot + TRASH_SUFFIX;
    }

    /**
     * Use this to create instance with custom root
     * @param storageRoot top-level bucket in S3 under which all content is stored
     * @return the S3 StorageProvider
     */
    public static S3StorageProvider create(String storageRoot) {

        // AWS_ACCESS_KEY, AWS_SECRET_KEY
        EnvironmentVariableCredentialsProvider envProvider
            = new EnvironmentVariableCredentialsProvider();
        // aws.accessKeyId, aws.secretKey
        SystemPropertiesCredentialsProvider sysProvider
            = new SystemPropertiesCredentialsProvider();
        // accessKey, secretKey
        PropertiesFileCredentialsProvider propProvider
            = new PropertiesFileCredentialsProvider(
                System.getProperty("aws.config", System.getProperty("user.dir")));

        // AwsCredentials.properties
        ClasspathPropertiesFileCredentialsProvider cpProvider
            = new ClasspathPropertiesFileCredentialsProvider();

        AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(
            envProvider,
            sysProvider,
            propProvider,
            cpProvider
        );

        AmazonS3 s3 = new AmazonS3Client(chain);
        // TODO make region configurable
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);
        return new S3StorageProvider(s3, storageRoot);
    }

    /**
     * Creates a folder by creating all nonexistent parent folders first then creates the target
     * folder.  If parent folders already exist then no exception should be thrown and the
     * implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw
     * {@link cworks.treefs.spi.TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     */
    @Override
    public TreeFolder createFolder(final Path folder) throws StorageException {
        TreeFolder sFolder = createFolder(folder, new HashMap<String, Object>());
        return sFolder;
    }

    /**
     * Creates a folder by creating all nonexistent parent folders first and associates the
     * metadata with the new folder.  If parent folders already exist then no exception should be
     * thrown and the implementation should cause no side-effects on existing parent folders.
     *
     * If the target folder already exists then throw
     * {@link cworks.treefs.spi.TreeFolderExistsException}
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
     * {@link cworks.treefs.spi.TreeFolderExistsException}
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
     * {@link cworks.treefs.spi.TreeFolderExistsException}
     *
     * @param folder A relative Path that has the folder to create at the end
     * @param description A description that should be associated with the given folder
     * @param metadata Map that contains application specific metadata
     * @throws StorageException
     */
    @Override
    public TreeFolder createFolder(final Path folder, String description, Map<String, Object> metadata)
        throws StorageException {

        TreeFolder sfolder = null;
        try {
            // creates a pathKey that S3 will use to create a bucket
            String pathKey = _s3FolderKey(folder);

            if(!isNullOrEmpty(description)) {
                if(isNull(metadata)) {
                    metadata = new HashMap<String, Object>();
                }
                metadata.put("description", description);
            }
            // create folder in S3
            _s3CreateFolders(pathKey, metadata);
            // read it back to create TreeFolder instance to return
            sfolder = _s3ReadFolder(pathKey, 0);

        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
        } catch(Exception ex) {
            throw new StorageException("Exception creating folder: " + folder.toString(), ex);
        }

        return sfolder;
    }

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @return folder as a {@link TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder) throws StorageException {
        TreeFolder sFolder = openFolder(folder, new S3PathFilter());
        return sFolder;
    }

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link TreeFolder#items()} loaded.
     *
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder, int maxLevels) throws StorageException {
        TreeFolder sFolder = openFolder(folder, new S3PathFilter() , maxLevels);
        return sFolder;
    }

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and ONE level of children {@link TreeFolder#items()}.  The filter should be used
     * to determine what child items will be loaded into {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param filter A filter to apply to the children in this folder
     * @return folder as a {@link TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder, DirectoryStream.Filter<Path> filter) throws StorageException {
        TreeFolder sFolder = openFolder(folder, filter, 0);
        return sFolder;
    }

    /**
     * Opens a folder and returns a {@link TreeFolder} instance that contains information about the
     * folder and at most maxLevel of children {@link TreeFolder#items()}.  The filter should be used
     * to determine what child items will be loaded into {@link TreeFolder#items()}
     *
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param filter A filter to apply to the children in this folder
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link TreeFolder}
     */
    @Override
    public TreeFolder openFolder(Path folder, DirectoryStream.Filter<Path> filter, int maxLevels)
        throws StorageException {

        if(!exists(folder)) {
            throw new NoTreeFolderException(folder);
        }

        TreeFolder sFolder = null;
        try {
            // creates a pathKey that S3 will use to create a bucket
            String pathKey = _s3FolderKey(folder);
            sFolder = _s3ReadFolder(pathKey, maxLevels);
        } catch(Exception ex) {
            throw new StorageException(ex);
        }

        return sFolder;
    }

    /**
     * TODO this is going to be a little tricky because S3 doesn't support Unix style file filtering
     * TODO see this link: http://docs.aws.amazon.com/cli/latest/reference/s3/#use-of-exclude-and-include-filters
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
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     * @param folder A relative Path to the folder to open
     * @param glob A glob pattern according 1-5 above
     * @return folder as a {@link TreeFolder}
     * @throws StorageException
     */
    @Override
    public TreeFolder openFolder(Path folder, String glob) throws StorageException {
        TreeFolder sFolder = openFolder(folder, glob, 0);
        return sFolder;
    }

    /**
     * TODO this is going to be a little tricky because S3 doesn't support Unix style file filtering
     * TODO see this link: http://docs.aws.amazon.com/cli/latest/reference/s3/#use-of-exclude-and-include-filters
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
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreeFolderException}
     * If folder is not actually a folder then throw
     * {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * null should never be returned from this method, it should return a {@link TreeFolder} or throw
     *
     * @param folder A relative Path to the folder to open
     * @param glob A glob pattern according 1-5 above
     * @param maxLevels the number of sub-directory levels to load
     * @return folder as a {@link TreeFolder}
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
     * If path does not exist then throw {@link NoTreePathException}
     * If path is a folder and IS NOT EMPTY then throw {@link cworks.treefs.spi.TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @throws StorageException
     */
    @Override
    public void trash(Path path) throws StorageException {
        trash(path, false);
    }

    /**
     * Moves a file or folder to the Trash.
     *
     * If path does not exist then throw {@link NoTreePathException}
     *
     * If force is true then trashing NON EMPTY folders is allowed otherwise...
     * If path is a folder and IS NOT EMPTY then throw {@link cworks.treefs.spi.TreeFolderNotEmptyException}
     *
     * After successful execution of this method subsequent calls on this interface for same
     * path should raise {@link NoTreePathException}
     *
     * @param path A relative Path to the file or folder to trash
     * @param force if true move path into trash even if its non-empty
     * @throws StorageException
     */
    public void trash(Path path, boolean force) throws StorageException {
        try {
            // this checks for existence and emptiness
            if(!force) {
                if(!isEmpty(path)) {
                    throw new TreeFolderNotEmptyException("folder " + path.toString() + " not empty", path);
                }
            }
        } catch(NotATreeFolderException ex) {
            logger.info("eating NotATreeFolderException because of why????, Don't remember why this is here.");
        }

        try {
            String pathKey = _s3PathKey(path);
            for(S3ObjectSummary summary : S3Objects.withPrefix(s3, storageRoot, pathKey)) {
                String trashPath = _s3ToTrash(summary.getKey());
                if(isNullOrEmpty(trashPath)) {
                    logger.warn("Move: " + summary.getBucketName() + ":" + summary.getKey()
                        + " into Trash returned null.  Item might not of made it to trash.");
                } else {
                    logger.debug("Moved: " + summary.getBucketName() + ":" + summary.getKey()
                        + " into Trash");
                }
            }


        } catch(Exception ex) {
            throw new StorageException(ex);
        }
    }

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
     * If path does not exist in Trash then throw {@link NoTreePathException}
     * If path is a folder the operation will delete both the folder and ALL children
     *
     * Successful completion of this method cannot be reversed.
     *
     * @param path A relative Path to the file or folder in trash
     * @throws StorageException
     */
    @Override
    public void delete(Path path) throws StorageException {
        if(!_s3ExistsInTrash(path)) {
            throw new NoTreePathException("path " + path.toString() + " not in trash.");
        }

        try {
            String pathKey = _s3PathKey(path);
            for(S3ObjectSummary summary : S3Objects.withPrefix(s3, trashRoot, pathKey)) {
                String deletedPath = _s3Delete(summary.getKey());
                if(isNullOrEmpty(deletedPath)) {
                    logger.warn("Delete: " + summary.getBucketName() + ":" + summary.getKey()
                        + " might not of been deleted, null was returned from _s3Delete.");
                } else {
                    logger.debug("Deleted: " + summary.getBucketName() + ":" + summary.getKey());
                }
            }
        } catch(Exception ex) {
            throw new StorageException(ex);
        }
    }

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
    @Override
    public void deleteIfExists(Path path) throws StorageException {
        if(!_s3ExistsInTrash(path)) {
            return;
        }
        delete(path);
    }

    @Override
    public void restore(Path from, Path to) throws StorageException {

    }

    @Override
    public void restoreIfExists(Path from, Path to) throws StorageException {

    }

    @Override
    public TreeFolder openTrash(Path folder) throws StorageException {
        return null;
    }

    /**
     * Tests if a file or folder exists and returns true otherwise false
     *
     * DO NOT throw {@link cworks.treefs.spi.NoTreePathException} if path does not exist, just return false
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

        String pathKey = _s3FolderKey(path);
        JsonObject object = _s3MetadataRequest(pathKey, true);
        if(!isNull(object)) {
            return true;
        }

        pathKey = _s3FileKey(path);
        object = _s3MetadataRequest(pathKey, true);
        return !isNull(object);

    }

    /**
     * Test if a folder is empty and returns true otherwise false
     *
     * If folder does not exist then throw {@link cworks.treefs.spi.NoTreePathException}
     * If folder is not a folder then throw {@link cworks.treefs.spi.NotATreeFolderException}
     *
     * @param folder A relative Path to the folder to test for emptiness
     * @return true if folder exists otherwise false
     * @throws StorageException
     */
    @Override
    public boolean isEmpty(Path folder) throws StorageException {

        // test if path exists and is managed by this StorageProvider
        if(!exists(folder)) {
            throw new NoTreePathException(folder);
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

        if(isNull(path)) {
            throw new IllegalArgumentException("path argument can't be null");
        }

        String pathKey = _s3FolderKey(path);
        JsonObject object = _s3MetadataRequest(pathKey);
        return !isNull(object) && "folder".equalsIgnoreCase(object.getString("type"));

    }

    /**
     * Test if a path is a valid File
     * @param path to test
     * @return true if path is File otherwise false
     * @throws StorageException
     */
    @Override
    public boolean isFile(Path path) throws StorageException {

        if(isNull(path)) {
            throw new IllegalArgumentException("path argument can't be null");
        }

        String pathKey = _s3FileKey(path);
        JsonObject object = _s3MetadataRequest(pathKey);
        return !isNull(object) && "file".equalsIgnoreCase(object.getString("type"));

    }

    /**
     * Copy a file or folder from source to a target.
     *
     * If source is a folder and TreeCopyOption.RECURSIVE is passed then copy source and all children
     * to target
     *
     * If source is a folder and TreeCopyOption.RECURSIVE IS NOT passed then just copy the source
     * folder to target
     *
     * If the path to the target folder exists then NO exceptions should be raised
     *
     * If the path to the target folder does not exist then it should be created
     *
     * However if the target folder exists and
     * {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} is not passed then throw
     * {@link cworks.treefs.spi.TreePathExistsException} otherwise if should be replace the existing folder or file.
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
     *     {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} and {@link cworks.treefs.spi.TreeCopyOption}
     * @throws StorageException
     */
    public void copy(Path source, Path target, CopyOption... options) throws StorageException {
        // 1) dir  to dir    cp -R source target,                     target/source
        // 2) dir  to file   error
        // 3) file to dir    cp someFile.txt target,                  target/someFile.txt
        // 4) file to file   cp someFile.txt target/copySomeFile.txt, target/copySomeFile.txt

        if(!exists(source)) {
            throw new NoTreePathException("source " + source + " does not exist");
        }

        try {
            String sourcePathKey = _s3PathKey(source);
            String targetPathKey = _s3PathKey(target);
            boolean recursive = _hasCopyRecursive(options);
            boolean into      = _hasCopyIntoOption(options);

            if(isFolder(source)) {
                if(isNull(targetPathKey)) {
                    //throw new NoTreePathException("target " + target + " does not exist");
                    createFolder(target);
                    targetPathKey = _s3FolderKey(target);
                }
                if(recursive) {
                    if(into) {
                        // recursive-into
                        // cp source target/source, copy source folder into target folder
                        _s3CopyFolderToFolder(sourcePathKey, targetPathKey);
                    } else {
                        // recursive
                        // cp source/* target, copies the content of source/* folder
                        // within the target folder
                        _s3CopyFolderContentToFolder(sourcePathKey, targetPathKey);
                    }
                }
            } else if(isFile(source)) {
                if(isNull(targetPathKey)) {
                    createFolder(target.getParent());
                    targetPathKey = _s3FolderKey(target.getParent());
                }
                if(target.getNameCount() == targetPathKey.split("/").length) {
                    // copy File into Folder
                    _s3CopyToFolder(sourcePathKey, targetPathKey);
                } else if(target.getNameCount() == (targetPathKey.split("/").length + 1)){
                    // copy File to File
                    _s3CopyFileToFile(sourcePathKey, targetPathKey + target.getFileName());
                }
            }
        } catch(Exception ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws StorageException {
        copy(source, target, options);
        trash(source, true);
    }

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

    @Override
    public TreeFile createFile(Path path, InputStream stream, String description)
        throws StorageException {
        TreeFile sFile = createFile(path, stream, description, null);
        return sFile;
    }

    @Override
    public TreeFile createFile(final Path path, InputStream stream, final Map<String, Object> metadata)
        throws StorageException {
        TreeFile sFile = createFile(path, stream, null, metadata);
        return sFile;
    }

    @Override
    public TreeFile createFile(Path path, File file, Map<String, Object> metadata)
        throws StorageException {

        InputStream stream = null;
        S3File sFile = null;
        try {
            Path source = file.toPath();
            stream = Files.newInputStream(source, StandardOpenOption.READ);
            sFile = (S3File)createFile(path, stream, metadata);
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

    @Override
    public TreeFile createFile(final Path path, InputStream stream, String description,
        final Map<String, Object> metadata) throws StorageException {

        if(isNull(path) || isNull(stream)) {
            throw new IllegalArgumentException(
                "Both path and stream are required arguments and cannot be null.");
        }

        if(exists(path)) {
            throw new TreeFileExistsException("file: " + path.toString() + " exists", path);
        }

        TreeFile file = null;
        try {
            // get folderKey used to identify this parent folder in s3
            String folderKey = _s3FolderKeyFromFile(path);
            // creates parent folders in s3
            _s3CreateFolders(folderKey, metadata);
            // get fileKey used to identify this file in s3
            String fileKey = _s3FileKey(path);
            // create file in S3
            _s3CreateFile(fileKey, stream, metadata);
            // get an TreeFile instance to return
            file = _s3ReadFile(fileKey);
        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
        } catch(Exception ex) {
            throw new StorageException("exception creating file: " + path.toString(), ex);
        }

        return file;
    }

    /**
     * Opens an existing file for reading and returns an InputStream to read the file content from
     *
     * If the file path does not exist then {@link cworks.treefs.spi.NoTreePathException} should be thrown
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
        try {
            String fileKey = _s3FileKey(path);
            S3Object fileObject = s3.getObject(new GetObjectRequest(storageRoot, fileKey));
            if(isNull(fileObject)) {
                throw new StorageException("Unabled to get file: " + path);
            }

            in = fileObject.getObjectContent();
            if(isNull(in)) {
                throw new StorageException("Unable to open file: " + path + " for reading.");
            }
        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
            throw new StorageException(ex);
        } catch(Exception ex) {
            throw new StorageException(ex);
        }

        return in;
    }

    @Override
    public Map<String, Object> readMetadata(Path path) throws StorageException {

        JsonObject metadata = null;
        if(isFolder(path)) {
            metadata = _s3MetadataRequest(_s3FolderKey(path));
        } else if(isFile(path)) {
            metadata = _s3MetadataRequest(_s3FileKey(path));
        }

        if(isNull(metadata) || isNull(metadata.getObject("metadata"))) {
            return null;
        }

        // just return user's metadata
        return metadata.getObject("metadata").toMap();
    }

    @Override
    public TreePath findFirst(Path path, String glob) throws StorageException {
        return null;
    }

    @Override
    public TreePath findFirst(Path path, String glob, int maxLevels) throws StorageException {
        return null;
    }

    @Override
    public List<TreePath> find(Path path, String glob) throws StorageException {
        return null;
    }

    @Override
    public List<TreePath> find(Path path, String glob, int maxLevels) throws StorageException {
        return null;
    }

    /**
     * Convert a path to an s3 key, which represents a folder, the trick is to place a '/' at the end
     * so that S3 sees this as a folder/container.
     * @param folder path to render a folderKey for
     * @return folderKey
     */
    private String _s3FolderKey(Path folder) {
        String path = folder.toString().replace("\\", "/");
        if(!path.endsWith("/")) {
            path = path + "/";
        }
        if(path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    /**
     * Convert a file path to an s3 folder key
     * input file:    a/path/to/some/file.pdf
     * output folder: a/path/to/some
     * @param file render a folderKey from the given file Path
     * @return folderKey
     */
    private String _s3FolderKeyFromFile(Path file) {

        String parentFolder = file.getParent().toString().replace("\\", "/");
        if(!parentFolder.endsWith("/")) {
            parentFolder = parentFolder + "/";
        }
        if(parentFolder.startsWith("/")) {
            return parentFolder.substring(1);
        }
        return parentFolder;
    }

    /**
     * Convert a file to an s3 key, which represents a file in S3
     * @param file render a fileKey from the given file Path
     * @return fileKey
     */
    private String _s3FileKey(Path file) {
        String path = file.toString().replace("\\", "/");

        return path;
    }

    /**
     * return an InputStream for the given folder so it can be uploaded into S3
     * @param folder folder to upload
     * @return InputStream containing content
     * @throws IOException
     */
    private InputStream _s3FolderStream(Path folder) throws IOException {

        JsonObject jsonObject = new JsonObject();
        jsonObject.setString("path", _s3FolderKey(folder));

        InputStream is = new ByteArrayInputStream(
            jsonObject.asString().getBytes("UTF-8"));

        return is;
    }

    /**
     * Create directories required from top to bottom for a path
     * @param path path to create
     * @param metadata metadata to apply to bottom most (target) folder
     */
    private void _s3CreateFolders(String path, Map<String, Object> metadata)
        throws StorageException {

        String pathKey = "";
        String[] parts = path.split("/");
        int end = parts.length - 1;
        for(int i = 0; i < parts.length; i++) {
            pathKey = _s3FolderKey(Paths.get(pathKey + "/" + parts[i]));
            if(!exists(Paths.get(pathKey))) {
                // create folder and folder.meta file
                try {
                    if(i == end) {
                        // user metadata is only added for last folder because that is target
                        _s3CreateFolder(pathKey, metadata);
                    } else {
                        _s3CreateFolder(pathKey, null);
                    }
                } catch (IOException ex) {
                    throw new StorageException(ex);
                }
            }
        }

    }

    /**
     * Create a TreeFs file in S3.  This includes creating the Bucket and metadata file
     * @param fileKey file object to create
     * @param metadata apply to the file object
     * @throws IOException
     */
    private void _s3CreateFile(String fileKey, InputStream stream, Map metadata) throws IOException {
        logger.info("s3 creating file: " + fileKey);

        JsonObject treefsMetadata = _treefsNewMetadata(metadata);

        String[] parts = fileKey.split("/");
        String fileName = parts[parts.length-1];

        treefsMetadata.setString("path", fileKey);
        treefsMetadata.setString("type", "file");
        treefsMetadata.setString("name", fileName);

        ObjectMetadata s3Metadata = new ObjectMetadata();
        _treefsAddMetadata(treefsMetadata, s3Metadata);

        PutObjectResult result = s3.putObject(
            new PutObjectRequest(
                storageRoot,
                fileKey,
                stream, s3Metadata));

        _s3LogResult(result);
    }

    /**
     * Create a TreeFs folder in S3.  This includes creating the Bucket and metadata file
     *
     * @param pathKey path to create
     * @param metadata metadata to associate with folder
     */
    private void _s3CreateFolder(String pathKey, Map metadata) throws IOException {
        logger.info("s3 creating folder: " + pathKey);

        // create new s3 put object request
        PutObjectRequest folderRequest = new PutObjectRequest(
            storageRoot,
            pathKey,
            _s3FolderStream(Paths.get(pathKey)), null); // null means no metadata

        String[] parts = pathKey.split("/");
        String folderName = parts[parts.length-1];

        JsonObject treefsMetadata = _treefsNewMetadata(metadata);
        treefsMetadata.setString("path", pathKey);
        treefsMetadata.setString("type", "folder");
        treefsMetadata.setString("name", folderName);

        ObjectMetadata s3Metadata = new ObjectMetadata();
        s3Metadata.addUserMetadata("treefs-meta", treefsMetadata.asString());
        folderRequest.withMetadata(s3Metadata);

        PutObjectResult result = s3.putObject(folderRequest);
        _s3LogResult(result);
    }

    private void _s3LogResult(CopyObjectResult result) {
        logger.debug("etag:                 " + result.getETag());
        logger.debug("expirationTime:       " + result.getExpirationTime());
        logger.debug("versionId:            " + result.getVersionId());
        logger.debug("expirationTimeRuleId: " + result.getExpirationTimeRuleId());
    }

    private void _s3LogResult(PutObjectResult result) {
        logger.debug("contentMd5:           " + result.getContentMd5());
        logger.debug("etag:                 " + result.getETag());
        logger.debug("expirationTime:       " + result.getExpirationTime());
        logger.debug("versionId:            " + result.getVersionId());
        logger.debug("expirationTimeRuleId: " + result.getExpirationTimeRuleId());
    }

    private void _s3LogError(AmazonServiceException ex) {
        logger.error("Error Message:    " + ex.getMessage());
        logger.error("HTTP Status Code: " + ex.getStatusCode());
        logger.error("AWS Error Code:   " + ex.getErrorCode());
        logger.error("Error Type:       " + ex.getErrorType());
        logger.error("Request ID:       " + ex.getRequestId());
        logger.error("StackTrace:       ", ex);
    }

    private ObjectMetadata _s3NewMetadata() {
        ObjectMetadata metadata = new ObjectMetadata();
        String now = ISO8601DateParser.toString(new Date());
        JsonObject treefsMetadata = Json.object()
            .string("treefs-version", "1.0.0")
            .string("lastModifiedTime", now)
            .string("lastAccessedTime", now)
            .string("creationTime", now)
            .build();
        metadata.addUserMetadata("treefs-meta", treefsMetadata.asString());
        return metadata;
    }

    private JsonObject _treefsNewMetadata(Map data) {

        JsonObject meta = null;
        if(!isNull(data)) {
            // TODO old code
            // meta = Json.object().object("metadata", Json.asObject(data)).build();
            meta = Json.object().value("metadata", data).build();
        } else {
            meta = Json.object().build();
        }
        String now = ISO8601DateParser.toString(new Date());

        // TODO need to pull treefs-version from system, not hard coded
        meta.setString("treefs-version", "1.0.0");
        meta.setString("lastModifiedTime", now);
        meta.setString("lastAccessedTime", now);
        meta.setString("creationTime", now);

        return meta;
    }

    /**
     * Read file information from S3, create S3File instance and return
     * @param fileKey fileKey to read metadata for
     * @return TreeFile instance
     */
    TreeFile _s3ReadFile(String fileKey) {
        JsonObject data = _s3MetadataRequest(fileKey);
        TreeFile sFile = new S3File(data);
        return sFile;
    }

    /**
     * Given an S3 folderKey read the contents of the folder and hydrate the folder tree
     * up to and including all sub-items that are maxLevels deep.
     *
     * S3 stores objects in a flat structure and the purpose of this method is to convert the
     * flattened structure into a tree based structure and load that tree of paths into the returned
     * TreeFolder instance.  Every TreeFolder instance has an items() list which contains a list of items
     * (TreeFolder or TreeFile) in the target TreeFolder.  When maxLevels is > 0 the tree of TreeFolder(s) and TreeFile(s)
     * are hydrated and linked with the target TreeFolder items() list.
     *
     * @param folderKey
     * @param maxLevels
     * @return
     */
    TreeFolder _s3ReadFolder(String folderKey, int maxLevels) {

        // Create local cache for this method
        Map<String, JsonObject> cache = _newLruCache(100);

        JsonObject folderMeta = _s3ObjectRequest(folderKey);
        S3Folder topFolder = new S3Folder(folderMeta);

        if(maxLevels > 0) {
            String[] targetPath = folderKey.split("/");
            S3Folder currentPath = topFolder;
            // folderKey is name of folder in S3 and we filter all s3 objects that don't start with it
            for (S3ObjectSummary summary : S3Objects.withPrefix(s3, storageRoot, folderKey)) {
                // don't include folder represented by folderKey
                // in list of items this folder contains
                if (folderKey.equals(summary.getKey())) {
                    continue;
                }

                String[] path = summary.getKey().split("/");
                // don't include sub-paths deeper than this path (targetPath) + maxLevels
                if (path.length > (targetPath.length + maxLevels)) {
                    continue;
                }

                boolean isFolder = _s3IsFolder(summary);
                // remove targetPath from beginning of path so that we get relative subpath
                String[] subpath = (String[])ArrayUtils.subarray(
                    path, // subarray
                    targetPath.length, // array
                    path.length);

                S3Folder tempRoot  = currentPath;
                // reset working pathKey to top folderKey
                String pathKey = folderKey;
                for(int i = 0; i < subpath.length; i++) {
                    pathKey += subpath[i];
                    if(isFolder) {
                        pathKey += "/";
                    } else {
                        // is file
                        if(i < subpath.length-1) {
                            pathKey += "/";
                        }
                    }

                    JsonObject treefsMetadata = null;
                    if(cache.containsKey(pathKey)) {
                        logger.debug("GETTING " + pathKey + " from cache.");
                        // already made a request to AWS in this method call so reuse data
                        // and forego making a request to AWS
                        treefsMetadata = cache.get(pathKey);
                    } else {
                        logger.debug("MAKING AWS CALL FOR " + pathKey);
                        // actually make the request to AWS and put into cache
                        treefsMetadata = _s3MetadataRequest(pathKey);
                        cache.put(pathKey, treefsMetadata);
                    }
                    // set the child item on the current folder
                    if(isFolder) {
                        currentPath = (S3Folder)currentPath.addItem(new S3Folder(treefsMetadata));
                    } else {
                        // is file
                        if(i < subpath.length-1) {
                            // this is a folder leading up to file at end of path
                            currentPath = (S3Folder)currentPath.addItem(new S3Folder(treefsMetadata));
                        } else {
                            // this is file at end of path
                            currentPath.addItem(new S3File(treefsMetadata));
                        }
                    }
                }

                currentPath = tempRoot;
            }
        }

        return topFolder;
    }

    /**
     * Make a request for metadata
     * @param pathKey path to get metadata for
     * @return JsonObject containing metadata for pathKey
     */
    private JsonObject _s3MetadataRequest(String pathKey) {
        JsonObject data = _s3MetadataRequest(pathKey, false);
        return data;
    }

    /**
     * Make a request for metadata
     * @param pathKey path to get metadata for
     * @param quiet don't log AWS errors if true
     * @return JsonObject containing metadata for pathKey
     */
    private JsonObject _s3MetadataRequest(String pathKey, boolean quiet) {

        JsonObject data = _s3MetadataRequest(storageRoot, pathKey, quiet);
        return data;
    }

    /**
     * Make a request for metadata
     * @param bucketName (storageRoot or trash)
     * @param pathKey path to get metadata for
     * @param quiet don't log AWS errors if true
     * @return JsonObject containing metadata for pathKey
     */
    private JsonObject _s3MetadataRequest(String bucketName, String pathKey, boolean quiet) {

        JsonObject data = null;
        try {
            // actually make the request to AWS
            GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(
                bucketName,
                pathKey);

            ObjectMetadata om = s3.getObjectMetadata(metadataRequest);
            data = _toTreeFsMetadata(om);
        } catch(AmazonServiceException ex) {
            if(!quiet) {
                _s3LogError(ex);
            }
        }

        return data;
    }

    private JsonObject _s3ObjectRequest(String folderKey) {
        S3Object s3o = null;
        JsonObject data = null;
        try {
            GetObjectRequest getRequest = new GetObjectRequest(
                storageRoot,
                folderKey);

            s3o = s3.getObject(getRequest);
            data = _toTreeFsMetadata(s3o.getObjectMetadata());
        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
        } finally {
            closeQuietly(s3o);
        }

        return data;
    }

    /**
     * Move the source into the target, such that mv source target ends up looking like
     * target/source
     *
     * @param sourceKey the sourceKey to copy
     * @param targetKey the targetKey to copy into
     * @return
     */
    private void _s3CopyFolderToFolder(String sourceKey, String targetKey)
        throws StorageException {
        String folder = _pathName(sourceKey);
        try {
            for(S3ObjectSummary summary : S3Objects.withPrefix(s3, storageRoot, sourceKey)) {
                String[] parts = summary.getKey().split(folder, 2);
                String copyToKey;
                if(parts.length == 1 || (parts.length == 2 && "/".equals(parts[1]))) {
                    copyToKey = targetKey + folder + "/";
                } else {
                    copyToKey = targetKey + folder + parts[1];
                }

                JsonObject treefsMetadata = _s3MetadataRequest(summary.getKey(), true);
                CopyObjectResult result = _s3Copy(sourceKey, copyToKey, treefsMetadata);

                if(!isNull(result)) {
                    logger.debug("copyFolder: " + summary.getKey() + " " + copyToKey);
                    _s3LogResult(result);
                }
            }

        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
            throw new StorageException(ex);
        }

    }

    /**
     * Copy folder content to another folder
     * @param sourceKey
     * @param targetKey
     * @return
     * @throws StorageException
     */
    private void _s3CopyFolderContentToFolder(String sourceKey, String targetKey)
        throws StorageException {

        try {
            for(S3ObjectSummary summary : S3Objects.withPrefix(s3, storageRoot, sourceKey)) {

                String source = summary.getKey().replace(sourceKey, "");
                if(isNullOrEmpty(source)) {
                    continue;
                }

                if(!targetKey.endsWith("/")) {
                    targetKey = targetKey + "/";
                }
                String copyToKey = targetKey + source;
                JsonObject treefsMetadata = _s3MetadataRequest(summary.getKey(), true);
                CopyObjectResult result = _s3Copy(sourceKey, copyToKey, treefsMetadata);

                if(!isNull(result)) {
                    System.out.println("_s3CopyFolderContentToFolder: " + source + " " + copyToKey);
                    logger.debug("_s3CopyFolderContentToFolder: " + source + " " + copyToKey);
                    _s3LogResult(result);
                }
            }
        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
            throw new StorageException(ex);
        }
    }

    private String _pathName(String pathKey) {

        if(isNullOrEmpty(pathKey)) {
            return null;
        }

        if(pathKey.split("/").length < 1) {
            return pathKey;
        }

        return pathKey.split("/")[pathKey.split("/").length-1];
    }

    /**
     * Move the path given into the Trash
     * @param pathKey the pathKey to move into the Trash
     * @return
     */
    private String _s3ToTrash(String pathKey) throws StorageException {

        try {
            CopyObjectResult result = s3.copyObject(storageRoot, pathKey, trashRoot, pathKey);
            if(!isNull(result)) {
                s3.deleteObject(storageRoot, pathKey);
                return pathKey;
            }
        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
            throw new StorageException(ex);
        }

        return null;
    }

    /**
     * Returns true if path exists in trash bucket otherwise false
     * @param path path to see if its in the trash
     * @return true if the path is in trash otherwise false
     */
    boolean _s3ExistsInTrash(Path path) {
        if(isNull(path)) {
            return false;
        }

        String pathKey = _s3FolderKey(path);
        JsonObject object = _s3MetadataRequest(trashRoot, pathKey, true);
        if(!isNull(object)) {
            return true;
        }

        pathKey = _s3FileKey(path);
        object = _s3MetadataRequest(trashRoot, pathKey, true);
        if(!isNull(object)) {
            return true;
        }

        return false;
    }

    /**
     * Actually perform the delete operation in S3
     * @param pathKey
     * @return
     */
    String _s3Delete(String pathKey) {

        try {
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(trashRoot, pathKey);
            s3.deleteObject(deleteRequest);
            logger.debug("Permanently deleted: " + pathKey + " from trash.");
        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
        }

        return pathKey;
    }

    /**
     * Copy sourceKey to copyToKey and update with treefsMetadata
     * @param sourceKey
     * @param copyToKey
     * @param treefsMetadata
     * @return
     */
    private CopyObjectResult _s3Copy(String sourceKey, String copyToKey, JsonObject treefsMetadata) {

        Date now = new Date();
        // update creationTime, lastAccessedTime and path
        treefsMetadata.setString("creationTime", ISO8601DateParser.toString(now))
            .setString("lastAccessedTime", ISO8601DateParser.toString(now))
            .setString("path", copyToKey)
            .setString("name", _pathName(copyToKey));
        ObjectMetadata s3Metadata = new ObjectMetadata();
        // brands the metadata in s3 as being from us
        _treefsAddMetadata(treefsMetadata, s3Metadata);
        CopyObjectRequest request = new CopyObjectRequest(storageRoot, sourceKey, storageRoot, copyToKey)
            .withNewObjectMetadata(s3Metadata);
        CopyObjectResult result = s3.copyObject(request);

        return result;
    }

    /**
     * Get a TreeFs JsonObject which contains Metadata important to TreeFs from an S3 ObjectMetadata
     * instance.
     *
     * @param s3Metadata
     * @return
     */
    private JsonObject _toTreeFsMetadata(ObjectMetadata s3Metadata) {

        Map map = s3Metadata.getUserMetadata();
        String treefsMeta = (String)map.get("treefs-meta");
        if(isNullOrEmpty(treefsMeta)) {
            return null;
        }
        JsonObject jo = Json.asObject(treefsMeta);
        return jo;
    }

    /**
     * The main reason this is its own method is to ensure the treefsMetadata
     * gets serialized into S3 Metadata under a consistent tag.
     * @param treefsMetadata
     * @param s3Metadata
     */
    private void _treefsAddMetadata(JsonObject treefsMetadata, ObjectMetadata s3Metadata) {
        s3Metadata.addUserMetadata("treefs-meta", treefsMetadata.asString());
    }

    /**
     * private version of {@link S3StorageProvider#exists(java.nio.file.Path)}
     * that catches {@link StorageException} and handles fullPath instead of relative path argument.
     * @param path
     * @return
     */
    boolean _exists(Path path) {
        boolean exists = false;
        try {
            exists = exists(path);
        } catch(StorageException ex) {
            exists = false;
        }
        return exists;
    }

    /**
     * Does this summary item represent a folder?
     * @param summary
     * @return
     */
    boolean _s3IsFolder(S3ObjectSummary summary) {
        return summary.getKey().endsWith("/") ? true : false;
    }

    /**
     * Given a path render an S3 pathKey
     * @param path
     * @return
     * @throws StorageException
     */
    public String _s3PathKey(Path path) throws StorageException {
        if(isNull(path)) {
            return null;
        }

        String pathKey = _s3FolderKey(path);
        JsonObject object = _s3MetadataRequest(pathKey);
        if(!isNull(object)) {
            return pathKey;
        }

        pathKey = _s3FileKey(path);
        object = _s3MetadataRequest(pathKey);
        if(!isNull(object)) {
            return pathKey;
        }

        return null;
    }

    /**
     * Utility for creating a LRU cache, used to cut down some on the number of
     * AWS calls we make
     * @param capacity
     * @return
     */
    Map<String, JsonObject> _newLruCache(final int capacity) {
        Map<String, JsonObject> s3Cache =
            new LinkedHashMap<String, JsonObject>(capacity + 1, .75F, true) {
                // This method is called just after a new entry has been added
                public boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > capacity;
                }
            };
        return s3Cache;
    }

    /**
     * Returns true if one of the options is TreeCopyOption.RECURSIVE
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
     * Attempt to copy a file (sourcePath) into a directory (targetPath) and return true if
     * operation is successful otherwise return false.
     *
     * Assumptions: sourcePath is a confirmed managed file
     *
     * @param sourceKey
     * @param folderKey
     * @return
     */
    private void _s3CopyToFolder(String sourceKey, String folderKey)
        throws StorageException {

        String filename = _pathName(sourceKey);
        try {

            if(!folderKey.endsWith("/")) {
                folderKey = folderKey + "/";
            }
            String copyToKey = folderKey + filename;
            JsonObject treefsMetadata = _s3MetadataRequest(sourceKey, true);
            CopyObjectResult result = _s3Copy(sourceKey, copyToKey, treefsMetadata);

            if(!isNull(result)) {
                logger.debug("_s3CopyToFolder: " + sourceKey + " " + copyToKey);
                _s3LogResult(result);
            }

        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
            throw new StorageException(ex);
        }
    }

    /**
     * Attempt to copy a file (sourcePath) to a file with the same name but different path
     * (targetPath)
     *
     * Assumptions: sourcePath is a confirmed managed file
     *
     * @param sourceFileKey
     * @param targetFileKey
     */
    private void _s3CopyFileToFile(String sourceFileKey, String targetFileKey)
        throws StorageException {

        try {
            JsonObject treefsMetadata = _s3MetadataRequest(sourceFileKey, true);
            CopyObjectResult result = _s3Copy(sourceFileKey, targetFileKey, treefsMetadata);

            if(!isNull(result)) {
                logger.debug("_s3CopyFileToFile: " + sourceFileKey + " " + targetFileKey);
                _s3LogResult(result);
            }

        } catch(AmazonServiceException ex) {
            _s3LogError(ex);
            throw new StorageException(ex);
        }
    }

}
