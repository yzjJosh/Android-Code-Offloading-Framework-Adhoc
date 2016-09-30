package mobilecloud.utils;

import java.io.File;

/**
 * Utility functions for file manipulations
 */
public class FileUtils {
    
    private static final String EXECUTABLE_DIRECTORY = "executable";
    private static final String EXECUTABLE_FILE_NAME = "exe.apk";
    private static final String OPTIMIZED_DEX_FOLDER_NAME = "opt_dex";
    
    /**
     * get the directory path of a specific application id
     * @param applicationId the application id
     * @return the path of directory
     */
    public static String getAppDirectory(String applicationId) {
        return EXECUTABLE_DIRECTORY + "/" + applicationId;
    }
    
    /**
     * Get the location of the executable files
     * @param applicationId the application id
     * @return the executable file location
     */
    public static String getExecutableLocation(String applicationId) {
         return getAppDirectory(applicationId) + "/" + EXECUTABLE_FILE_NAME;
    }
    
    /**
     * Get the directory where optimized dex are stored
     * @param applicationId the application id
     * @return the directory of optimized dex files
     */
    public static String getOptimizedDexDirectory(String applicationId) {
        return getAppDirectory(applicationId) + "/" + OPTIMIZED_DEX_FOLDER_NAME;
    }
    
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
