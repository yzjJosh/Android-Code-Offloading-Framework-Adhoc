package mobilecloud.utils;

import java.io.File;

/**
 * Utility functions for file manipulations
 */
public class FileUtils {
    
    /**
     * Create a directory if it does not exist
     * @param path the path of directory
     */
    public static void createDirIfDoesNotExist(String path) {
        File f = new File(path);
        if(!f.exists()) {
            f.mkdirs();
        }
    }

}
