package mobilecloud.server;

import java.io.File;

import android.content.Context;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import mobilecloud.utils.FileUtils;

/**
 * Load apk to a class loader
 *
 */
public class APKLoader {
    
    private Context context;
    
    public APKLoader(Context context) {
        this.context = context;
    }
    
    /**
     * get the directory path of a specific application id
     * @param applicationId the application id
     * @return the path of directory
     */
    public String getAppDirectory(String applicationId) {
        return context.getCacheDir().getPath() + "/" + Config.EXECUTABLE_DIRECTORY + "/" + applicationId;
    }
    
    /**
     * Get the location of the executable files
     * @param applicationId the application id
     * @return the executable file location
     */
    public String getExecutableLocation(String applicationId) {
         return getAppDirectory(applicationId) + "/" + Config.EXECUTABLE_FILE_NAME;
    }
    
    /**
     * Get the directory where optimized dex are stored
     * @param applicationId the application id
     * @return the directory of optimized dex files
     */
    public String getOptimizedDexDirectory(String applicationId) {
        return getAppDirectory(applicationId) + "/" + Config.OPTIMIZED_DEX_FOLDER_NAME;
    }
    
    /**
     * Load apk for a specified application. This method assumes that the apk
     * file is stored in application executable directory
     * 
     * @param applicationId
     *            the id of application to load
     * @return the classloader
     * @throws NoApplicationExecutableException if not application executable has been found
     */
    public ClassLoader loadAPK(String applicationId) throws NoApplicationExecutableException {
        if(new File(getExecutableLocation(applicationId)).exists()) {
         //   FileUtils.createDirIfDoesNotExist(getOptimizedDexDirectory(applicationId));
         //   return new DexClassLoader(getExecutableLocation(applicationId), getOptimizedDexDirectory(applicationId), null,
         //           context.getClassLoader());
            return new PathClassLoader(getExecutableLocation(applicationId), context.getClassLoader());
        } else {
            throw new NoApplicationExecutableException();
        }
    }

}
