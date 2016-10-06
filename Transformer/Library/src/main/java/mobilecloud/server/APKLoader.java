package mobilecloud.server;

import dalvik.system.DexClassLoader;
import lombok.NonNull;

public class APKLoader {
    
    /**
     * get the directory path of a specific application id
     * @param applicationId the application id
     * @return the path of directory
     */
    public static String getAppDirectory(String applicationId) {
        return Config.EXECUTABLE_DIRECTORY + "/" + applicationId;
    }
    
    /**
     * Get the location of the executable files
     * @param applicationId the application id
     * @return the executable file location
     */
    public static String getExecutableLocation(String applicationId) {
         return getAppDirectory(applicationId) + "/" + Config.EXECUTABLE_FILE_NAME;
    }
    
    /**
     * Get the directory where optimized dex are stored
     * @param applicationId the application id
     * @return the directory of optimized dex files
     */
    public static String getOptimizedDexDirectory(String applicationId) {
        return getAppDirectory(applicationId) + "/" + Config.OPTIMIZED_DEX_FOLDER_NAME;
    }
    
    /**
     * Load apk for a specified application. This method assumes that the apk
     * file is stored in application executable directory
     * 
     * @param applicationId
     *            the id of application to load
     * @return the classloader
     */
    public ClassLoader loadAPK(@NonNull String applicationId) {
        return new DexClassLoader(getExecutableLocation(applicationId), getOptimizedDexDirectory(applicationId), null,
                ClassLoader.getSystemClassLoader());
    }

}
