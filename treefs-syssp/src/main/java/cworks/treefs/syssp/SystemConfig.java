package cworks.treefs.syssp;

import java.io.File;

public final class SystemConfig {

    /**
     * Return home directory for treefs-syssp
     * @return
     */
    public static String home() {
        String treefsHome = System.getProperty("treefs-syssp.home",
            System.getProperty("user.dir"));
        return treefsHome;
    }

    /**
     * Return root directory for treefs-syssp, this is root of tree where all files are saved
     * @return
     */
    public static String mount() {
        String root = home() + File.separator + "sysfs-data";
        return root;
    }

    public static String bucket() {
        return "default";
    }

    /**
     * Temp upload dir for TreeFs
     * @return
     */
    public static String uploadDir() {
        String uploads = mount() + File.separator + "uploads";
        return uploads;
    }

    /**
     * Root of TreeFs trash folder
     * @return
     */
    public static String trashDir() {
        String trash = mount() + File.separator + "trash";
        return trash;
    }

    /**
     * Temp download dir for TreeFs
     * @return
     */
    public static String downloadDir() {
        String downloads = mount() + File.separator + "downloads";
        return downloads;
    }
}
