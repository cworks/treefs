package net.cworks.treefs;

import net.cworks.treefs.domain.TreeFsFile;
import net.cworks.treefs.domain.TreeFsFolder;

import java.util.Collection;

public class TreeFsValidation {

    public static final int MAX_FILE_AND_FOLDER_NAME_LENGTH = 64;

    /**
     * Determines if the given folder name is acceptable for TreeFs
     * @param name
     * @return
     */
    public static boolean isAcceptableFolderName(String name) {
        if(isNullOrEmpty(name)) {
            return false;
        }
        if(name.length() > MAX_FILE_AND_FOLDER_NAME_LENGTH) {
            return false;
        }
        if(name.contains("\\") || name.contains("/")) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the given path is acceptable for TreeFs
     * TODO need to change this up to account for long paths.  The goal is to
     * verify each folder name in a long path is less than MAX_FILE_AND_FOLDER_NAME_LENGTH not that
     * the overall String is less than that.  We need to check the depth elsewhere.
     * @param path
     * @return
     */
    public static boolean isAcceptablePathName(String path) {
        if(isNullOrEmpty(path)) {
            return false;
        }

        String normalPath = path.replace("\\", "/");
        String[] parts = normalPath.split("/");
        for(int i = 0; i < parts.length; i++) {
            if(!isAcceptableFolderName(parts[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Perform the actions nessisary to determine if the input folder is legit for processing
     *
     * folders have 2 critical properties: name and path
     * name | path | interpretation
     * --------------------------------
     * no   | no   | throw exception
     * no   | yes  | proceed with the understanding that path will be used to make the new directory
     * yes  | no   | proceed with the understanding that name will be a folder at the clients root directory
     * yes  | yes  | proceed with the understanding that path will be created then a folder with name inside it
     *
     * @param folder
     */
    public static void validateFolder(TreeFsFolder folder) {
        throwIfNull("folder", folder);
        throwIfAllNullOrEmpty("folder.name and folder.path cannot both be null!", folder.name(), folder.path());
    }

    public static void validateFile(TreeFsFile file) {
        throwIfNull("file", file);
        throwIfAllNullOrEmpty("file.name and file.path cannot both be null!", file.name(), file.path());
    }

    public static void throwIfAllNullOrEmpty(String message, Object...things) {
        for(int i = 0; i < things.length; i++) {
            Object thing = things[i];
            if(isNull(thing) || isEmpty(thing.toString())) {
            } else {
                return; // thing was not null and not empty
            }
        }
        throw new TreeFsValidationException("All things are either null or empty which no workie: " + message);
    }

    public static boolean validFileSys(String path) {
        return true;
    }

    public static boolean isEmpty(String thing) {
        return "".equals(thing.trim()) ? true : false;
    }

    public static boolean isNull(Object thing) {
        return thing == null ? true : false;
    }

    public static boolean isNullOrEmpty(Object thing) {
        if(isNull(thing)) {
            return true;
        }
        if(thing instanceof Collection) {
            Collection collection = (Collection)thing;
            return collection.isEmpty();
        }
        if(thing instanceof String) {
            return isEmpty((String)thing);
        }

        return false;
    }

    public static void throwIfNull(String message, Object o) {
        if(o == null) {
            throw new TreeFsValidationException(message + " cannot be null silly rabbit.");
        }
    }

    public static boolean isInteger(String thing) {

        if(isNullOrEmpty(thing)) {
            return false;
        }

        try {
            Integer.parseInt(thing);
        } catch(NumberFormatException ex) {
            return false;
        }

        return true;
    }
}
