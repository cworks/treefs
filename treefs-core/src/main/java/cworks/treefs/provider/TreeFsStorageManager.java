package cworks.treefs.provider;

import cworks.json.JsonArray;
import cworks.json.JsonObject;
import cworks.treefs.*;
import cworks.treefs.common.IOUtils;
import cworks.treefs.common.StringUtils;
import cworks.treefs.domain.*;
import cworks.treefs.fs.FilterChain;
import cworks.treefs.spi.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/**
 * Client to StorageProvider mappings
 *
 * One purpose of this manager is to create and maintain the correct StorageProvider implementation
 * for a given TreeFsClient.
 *
 * client.id() to StorageProvider mapping
 *
 * When TreeFsStorageManager starts up it should read treefssp.json file to initialize the internal
 * map of clients to StorageProviders.  Once initialized this class will monitor the treefssp.json
 * for changes and load the new and/or changed mappings.  If an error occurs on initialization the
 * class will boot with only the 'sysuser' active.  If and error occurs on a re-load then only
 * those entries not re-initialized and the new ones that complete without error will be active.
 * Basically if an error occurs when trying to dynmaically load or re-load providers then that
 * client to provider mapping will not be activated.
 *
 * TODO This class should either implement StorageProvider interface OR contain a class that implements that
 * TODO interface.  The later is preferable.
 *
 * @author comartin
 */
public class TreeFsStorageManager {

    /**
     * client associated with this instance
     */
    private TreeFsClient client = null;

    /**
     * StorageProvider for this StorageManager instance
     */
    private StorageProvider provider = null;

    /**
     * create a cache of clients to StorageProvider(s)
     */
    private static Map<String, JsonObject> providerCache = _newStorageProviderCache(13);

    /**
     * Callers should use {@link #create(cworks.treefs.TreeFsClient)}
     * @param client
     */
    private TreeFsStorageManager(TreeFsClient client) {
        this.client = client;
        this.provider = StorageProviderFactory.createProvider(this.client);
    }

    /**
     * Create a TreeFsStorageManager instance for the given clientId
     * @param client
     * @return
     */
    public static TreeFsStorageManager create(TreeFsClient client) {
        if(client == null) {
            throw new IllegalArgumentException("TreeFsClient argument cannot be null silly monkey");
        }

        return new TreeFsStorageManager(client);
    }

    /**
     * Return the type of TreeFs Object for the given path.
     * @param path path to test
     * @return TreeFsType of Folder, File or NaT (Not a Type)
     */
    public TreeFsType typeOf(String path) {

        try {
            if(provider.isFolder(Paths.get(path))) {
                return TreeFsType.FOLDER;
            }
            if(provider.isFile(Paths.get(path))) {
                return TreeFsType.FILE;
            }
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        }

        // at this point we don't know
        return TreeFsType.NaT;
    }

    /**
     * Create a folder in TreeFs from the one given as an argument
     * @param folder
     * @return
     */
    public TreeFsFolder createFolder(final TreeFsFolder folder) {

        TreeFsFolder treefsFolder = folder;
        try {
            TreeFsValidation.validateFolder(folder);
            Path target = resolveTargetFolder(folder);
            if (!TreeFsValidation.isNullOrEmpty(folder.description()) && !TreeFsValidation.isNull(folder.metadata())) {
                provider.createFolder(target, folder.description(), folder.metadata());
            } else if (TreeFsValidation.isNullOrEmpty(folder.description()) && !TreeFsValidation.isNull(folder.metadata())) {
                provider.createFolder(target, folder.metadata());
            } else if (!TreeFsValidation.isNullOrEmpty(folder.description()) && TreeFsValidation.isNull(folder.metadata())) {
                provider.createFolder(target, folder.description());
            } else {
                provider.createFolder(target);
            }
        } catch(TreePathExistsException ex) {
            throw new TreeFsPathExistsException(ex, ex.path());
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        }

        return treefsFolder;
    }

    /**
     * Create a file in TreeFs from the one given as an argument
     * @param file
     * @return
     */
    public TreeFsFile createFile(final Path source, final TreeFsFile file) {

        TreeFsFile treefsFile = createFile(source, file, false);
        return treefsFile;
    }

    /**
     * Create a file in TreeFs, overwriting one if it already exists
     * @param source
     * @param file
     * @param overwrite
     * @return
     */
    public TreeFsFile createFile(final Path source, final TreeFsFile file, boolean overwrite) {

        TreeFsFile treefsFile = new TreeFsFile(file);
        InputStream in = null;
        try {
            TreeFsValidation.validateFile(file);
            final Path target = resolveTargetFile(file);

            if(overwrite) {
                provider.trashIfExists(target);
            }

            in = Files.newInputStream(source, StandardOpenOption.READ);
            if (!TreeFsValidation.isNullOrEmpty(file.description()) && !TreeFsValidation.isNull(file.metadata())) {
                provider.createFile(target, in, file.description(), file.metadata());
            } else if (TreeFsValidation.isNullOrEmpty(file.description()) && !TreeFsValidation.isNull(file.metadata())) {
                provider.createFile(target, in, file.metadata());
            } else if (!TreeFsValidation.isNullOrEmpty(file.description()) && TreeFsValidation.isNull(file.metadata())) {
                provider.createFile(target, in, file.description());
            } else {
                provider.createFile(target, in);
            }

        } catch (TreePathExistsException ex) {
            throw new TreeFsPathExistsException(ex, ex.path());
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        } catch (IOException ex) {
            throw new TreeFsException(ex);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return treefsFile;
    }

    /**
     * Obtains the target folder (Path) for the given TreeFsFolder based on the rules below
     *
     * folders have 2 critical properties: name & path
     * name | path | interpretation
     * --------------------------------
     * no   | no   | throw exception (should be handled before calling this method)
     * no   | yes  | proceed with the understanding that path will be used to make the new directory
     * yes  | no   | proceed with the understanding that name will be a folder at the clients root directory
     * yes  | yes  | proceed with the understanding that path will be created then a folder with name inside it
     * @param folder
     */
    static Path resolveTargetFolder(final TreeFsFolder folder) {
        String path = folder.path();
        String name = folder.name();
        Path target;
        if(!TreeFsValidation.isAcceptableFolderName(name) && TreeFsValidation.isAcceptablePathName(path)) {
            target = Paths.get(TreeFs.convertPath(path));
        } else if(TreeFsValidation.isAcceptableFolderName(name) && !TreeFsValidation.isAcceptablePathName(path)) {
            target = Paths.get(name);
        } else if(TreeFsValidation.isAcceptableFolderName(name) && TreeFsValidation.isAcceptablePathName(path)) {
            target = Paths.get(TreeFs.convertPath(path) + File.separator + folder.name());
        } else {
            throw new TreeFsValidationException(
                "folder.name or folder.path are NOT acceptable...BOOM!");
        }

        return target;
    }

    static Path resolveTargetFile(final TreeFsFile file) {
        Path target;
        String name = file.name();
        // if path ends with name then we remove the filename from path to separate dir and file
        String path = StringUtils.removeEnd(file.path(), name);

        if(!TreeFsValidation.isAcceptableFolderName(name) && TreeFsValidation.isAcceptablePathName(path)) {
            target = Paths.get(TreeFs.convertPath(path));
        } else if(TreeFsValidation.isAcceptableFolderName(name) && !TreeFsValidation.isAcceptablePathName(path)) {
            target = Paths.get(name);
        } else if(TreeFsValidation.isAcceptableFolderName(name) && TreeFsValidation.isAcceptablePathName(path)) {
            target = Paths.get(TreeFs.convertPath(path) + File.separator + file.name());
        } else {
            throw new TreeFsValidationException(
                "file.name or file.path are NOT acceptable...BOOM!");
        }

        return target;
    }

    /**
     * Retrieve the absolute file path within TreeFs
     * @param path
     * @return
     */
    public String retrieveFile(String path) {
        InputStream in = null;
        String location = null;
        try {
            Path target = Paths.get(path);
            if(!provider.exists(target)) {
                throw new TreeFsException("path: " + path + " does not exist in TreeFs");
            }
            in = provider.read(target);
            if(TreeFsValidation.isNull(in)) {
                throw new TreeFsException("cannot obtain stream to path: " + path);
            }

            Path copyTarget = Paths.get(TreeFs.downloadDir() + File.separator + target.getFileName().toString());
            Files.copy(in, copyTarget, StandardCopyOption.REPLACE_EXISTING);
            location = copyTarget.toFile().getPath();
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        } catch (IOException ex) {
            throw new TreeFsException("error retrieving path: " + path, ex);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return location;
    }

    /**
     * Retrieve the folder represent by path from TreeFs
     * @param path
     * @return
     */
    public TreeFsFolder retrieveFolder(String path) {
        TreeFsFolder folder = retrieveFolder(path, 0);
        return folder;
    }

    /**
     * Retrieve a folder and load all children depth levels deep
     * @param path
     * @param depth
     * @return
     */
    public TreeFsFolder retrieveFolder(String path, int depth) {
        TreeFsFolder folder = retrieveFolder(path, null, depth);
        return folder;
    }

    /**
     * Retrieve a folder and filter the children according to the filter and only consider
     * depth levels deep.
     *
     * @param path
     * @param filter
     * @param depth
     * @return
     */
    public TreeFsFolder retrieveFolder(String path, String filter, int depth) {
        TreeFsFolder folder = null;
        try {
            Path target = Paths.get(path);
            if(!provider.exists(target)) {
                throw new TreeFsException("path: " + path + " does not exist in TreeFs");
            }

            TreeFolder sFolder = provider.openFolder(target, filter, depth);
            if(TreeFsValidation.isNull(sFolder)) {
                throw new TreeFsException("unable to open folder: " + path);
            }

            folder = TreeFsFactory.folder(sFolder);
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        } catch (Exception ex) {
            throw new TreeFsException("error retrieving path: " + path, ex);
        }

        return folder;
    }

    /**
     * payload.putNumber("depth", Integer.parseInt(depth));
     * payload.putString("filter", filter);
     * payload.putBoolean("foldersOnly", Boolean.parseBoolean(foldersOnly));
     * payload.putBoolean("filesOnly", Boolean.parseBoolean(filesOnly));
     * payload.putBoolean("recursive", Boolean.parseBoolean(recursive));
     *
     * @param path
     * @param options
     * @return
     */
    public TreeFsFolder retrieveFolder(String path, JsonObject options) {

        Integer depth = options.getInteger("depth", -1);
        // TODO incorporate into pathFilter or use directly if both folderOnly and fileOnly are false
        String glob = options.getString("filter", null);
        boolean foldersOnly = options.getBoolean("foldersOnly", false);
        boolean filesOnly = options.getBoolean("filesOnly", false);
        boolean recursive = options.getBoolean("recursive", false);

        // user wants both files and folders so we given them both (i.e. we don't need a filter)
        if(foldersOnly && filesOnly) {
            // this will produce no filter below
            foldersOnly = false;
            filesOnly = false;
        }

        FilterChain filters = FilterChain.newChain();
        if(foldersOnly) {
           filters.includeFolders();
        }

        if(filesOnly) {
            filters.includeFiles();
        }

        if(!TreeFsValidation.isNullOrEmpty(glob)) {
            List<String> globs = _parseGlobs(glob);
            for(String g : globs) {
                filters.globFilter(g);
            }
        }

        // if depth is gt default depth then set to default depth
        if(depth > 100) {
            depth = 100;
        }

        // if depth is not given (i.e. is -1) then we always want to assume a depth of 1
        // unless recursive is given then assume max
        if(depth < 0) {
            if(recursive) {
                depth = 100;
            } else {
                depth = 1;
            }
        }

        TreeFsFolder folder = null;
        try {
            Path target = Paths.get(path);
            if(!provider.exists(target)) {
                throw new TreeFsException("path: " + path + " does not exist in TreeFs");
            }

            TreeFolder sFolder = provider.openFolder(target, filters, depth);
            if(TreeFsValidation.isNull(sFolder)) {
                throw new TreeFsException("unable to open folder: " + path);
            }

            folder = TreeFsFactory.folder(sFolder);
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        } catch (Exception ex) {
            throw new TreeFsException("error retrieving path: " + path, ex);
        }

        return folder;
    }

    /**
     * Copy source path to a target path
     * @param source
     * @param target
     * @param copyOptions
     * @return
     */
    public TreeFsPath copyPath(String source, String target, JsonArray copyOptions) {
        TreeFsPath path = null;
        try {
            Path sourcePath = Paths.get(source);
            Path targetPath = Paths.get(target);
            if(!provider.exists(sourcePath)) {
                throw new TreeFsException("source path: " + source + " does not exist in TreeFs");
            }

            CopyOption[] options = _toCopyOptions(copyOptions);
            provider.copy(sourcePath, targetPath, options);
            path = TreeFsFactory.path().withPath(source).make();
        } catch(Exception ex) {
            throw new TreeFsException(ex);
        }

        return path;
    }

    /**
     * Move source path to target path
     * @param source
     * @param target
     * @param moveOptions
     * @return
     */
    public TreeFsPath movePath(String source, String target, JsonArray moveOptions) {
        TreeFsPath path = null;
        try {
            Path sourcePath = Paths.get(source);
            Path targetPath = Paths.get(target);
            if(!provider.exists(sourcePath)) {
                throw new TreeFsException("source path: " + source + " does not exist in TreeFs");
            }
            CopyOption[] options = _toCopyOptions(moveOptions);
            provider.move(sourcePath, targetPath, options);
            path = TreeFsFactory.path().withPath(target).make();
        } catch(Exception ex) {
            throw new TreeFsException(ex);
        }

        return path;
    }

    /**
     * Retrieve the metadata for path
     * @param path
     * @return
     */
    public Map<String, Object> retrieveMetadata(String path) {

        Map<String, Object> metadata = null;
        try {
            Path target = Paths.get(path);
            if(!provider.exists(target)) {
                throw new TreeFsException("path: " + path + " does not exist in TreeFs");
            }
            metadata = provider.readMetadata(target);
        } catch (StorageException ex) {
            throw new TreeFsException(ex);
        } catch (Exception ex) {
            throw new TreeFsException("error retrieving metadata for: " + path, ex);
        }

        return metadata;
    }

    /**
     * Move a path to the trash
     * @param path
     */
    public void trashPath(String path) {

        trashPath(path, false);
    }

    /**
     * Move a path to trash, if forceDelete option is true then force move even
     * if path is non-empty
     * @param path path to move into trash
     * @param forceDelete if true will move even if path is non-empty
     */
    public void trashPath(String path, boolean forceDelete) {

        try {
            Path target = Paths.get(path);
            if(!provider.exists(target)) {
                throw new TreeFsException("path: " + path + " does not exist in TreeFs");
            }

            provider.trash(target, forceDelete);
        } catch(TreeFolderNotEmptyException ex) {
            throw new TreeFsFolderNotEmptyException("error trashing path: " + path, ex);
        } catch(StorageException ex) {
            throw new TreeFsException("error trashing path: " + path, ex);
        }
    }

    /**
     * Delete a path from the trash
     * @param path
     */
    public void deletePath(String path) {
        try {
            Path target = Paths.get(path);
            provider.deleteIfExists(target);
        } catch(Exception ex) {
            throw new TreeFsException("error deleting path: " + path, ex);
        }
    }

    /**
     * traverse the folder for fun
     * @param folder
     */
    private static void _traverse(TreeFolder folder) {
        if(folder.items() == null) {
            return;
        }
        List<TreePath> items = folder.items();
        for(TreePath item : items) {
            System.out.println(item.path().toString());
            if(item instanceof TreeFolder) {
                _traverse((TreeFolder)item);
            }
        }
    }

    TreeFsFolder _loadChildren(TreeFolder folder, TreeFsFolderMaker folderMaker) {
        if(!folder.hasItems()) {
            TreeFsFolder temp = folderMaker.make();
            System.out.println(temp);
            return temp;
        }

        List<TreePath> items = folder.items();
        for(TreePath item : items) {
            if(item instanceof TreeFile) {
                TreeFile fitem = (TreeFile)item;
                TreeFsFileMaker maker = TreeFsFactory.file(fitem.path());
                TreeFsFile f = maker.withDescription(fitem.description())
                    .withCreatedAt(fitem.creationTime())
                    .withSize(fitem.size()).make();
                folderMaker.addFile(f);
            } else if(item instanceof TreeFolder) {

                TreeFolder fitem = (TreeFolder)item;

                TreeFsFolderMaker maker = TreeFsFactory.folder(
                        fitem.path().getFileName().toString(), fitem.path());

                TreeFsFolder f = maker
                    .withDescription(fitem.description())
                    .withCreatedAt(fitem.creationTime())
                    .make();

                folderMaker.addFolder(f);

                _loadChildren(fitem, maker);

            }
        }

        return folderMaker.make();
    }

    /**
     * Given a string try to create the correct CopyOption enum
     * @param option
     * @return
     */
    private CopyOption _copyOption(String option) {
        CopyOption co = TreeCopyOption.valueOf(option.toUpperCase());
        if(TreeFsValidation.isNull(co)) {
            co = StandardCopyOption.valueOf(option.toUpperCase());
        }
        return co;
    }

    /**
     * Convert a JsonArray into CopyOption array
     * @param copyOptions
     * @return
     */
    private CopyOption[] _toCopyOptions(JsonArray copyOptions) {
        if(TreeFsValidation.isNull(copyOptions)) {
            return new CopyOption[0];
        }

        Iterator it = copyOptions.iterator();
        CopyOption[] options = new CopyOption[copyOptions.size()];
        int i = 0;
        while(it.hasNext()) {
            String option = (String)it.next();
            options[i++] = _copyOption(option);
        }
        return options;
    }

    /**
     * Parses a pipe delimited string into a list of individual strings each of which
     * represent a single glob pattern that will be applied.
     * @param globs
     * @return
     */
    private List<String> _parseGlobs(String globs) {

        String[] parts = globs.split("\\|");
        return Arrays.asList(parts);
    }

    /**
     * Return the TreeFsStorageManager for the given client
     * @param client
     * @return
     */
    public static TreeFsStorageManager storageManager(TreeFsClient client) {

        if(!providerCache.containsKey(client.id())) {
            return null;
        }


        return null;
    }

    /**
     * Create an LRU cache that will maintain a mapping of client => StorageProviders
     * @param capacity
     * @return
     */
    static Map<String, JsonObject> _newStorageProviderCache(final int capacity) {
        Map<String, JsonObject> spCache =
            new LinkedHashMap<String, JsonObject>(capacity + 1, .75F, true) {
                // This method is called just after a new entry has been added
                public boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > capacity;
                }
            };

        return spCache;
    }
}

