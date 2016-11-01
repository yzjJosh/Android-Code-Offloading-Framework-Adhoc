package mobilecloud.server;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import dalvik.system.DexClassLoader;
import mobilecloud.utils.FileUtils;

/**
 * Load apk to a class loader
 *
 */
public class ExecutableLoader {
    
    private Context context;
    private final Map<String, ClassLoader> classLoaders;
    
    public ExecutableLoader(Context context) {
        this.context = context;
        this.classLoaders = new ConcurrentHashMap<>();
    }
    
    /**
     * get the directory path of a specific application id
     * @param applicationId the application id
     * @return the path of directory
     */
    public String getAppDirectory(String applicationId) {
        return context.getCacheDir().getPath() + "/" + Config.APP_FOLDER + "/" + applicationId;
    }
    
    /**
     * Get the location of the executable files
     * @param applicationId the application id
     * @return the executable file location
     */
    public String getExecutableDirectory(String applicationId) {
         return getAppDirectory(applicationId) + "/" + Config.EXECUTABLE_FOLDER;
    }
    
    /**
     * Get the location of the optimized dex files
     * @param applicationId the app id
     * @return the location of optimized dex files
     */
    public String getOptimizedDexDirectory(String applicationId) {
        return getAppDirectory(applicationId) + "/" + Config.OPTIMIZED_DEX_FOLDER_NAME;
    }
    
    /**
     * Get the directory where temporary data are stored
     * @param applicationId the application id
     * @return the tmp folder path
     */
    public String getTmpDirectory(String applicationId) {
        return getAppDirectory(applicationId) + "/" + Config.TEMP_FOLDER;
    }
    
    /**
     * Get the location of temporary zip executable file
     * @param applicationId the application id
     * @return the temporary zip executable file
     */
    public String getTmpExecutablePackLocation(String applicationId) {
        return getTmpDirectory(applicationId) + "/" + Config.TEMP_ZIP_EXE_NAME;
    }
    
    /**
     * Load apk for a specified application. This method assumes that the apk
     * file is stored in application executable directory
     * 
     * @param applicationId
     *            the id of application to load
     * @return the classloader
     * @throws NoApplicationExecutableException if no application executable has been found
     */
    public ClassLoader loadExecutable(String applicationId) throws NoApplicationExecutableException {
        ClassLoader cl = classLoaders.get(applicationId);
        if(cl == null) {
            synchronized(this) {
                if((cl = classLoaders.get(applicationId)) == null) {
                    if (FileUtils.hasFiles(getExecutableDirectory(applicationId))) {
                        StringBuilder path = new StringBuilder();
                        for(File dex: new File(getExecutableDirectory(applicationId)).listFiles()) {
                            if(path.length() > 0) {
                                path.append(':');
                            }
                            path.append(dex.getPath());
                        }
                        FileUtils.createDirIfDoesNotExist(getOptimizedDexDirectory(applicationId));
                        cl = new DexClassLoader(path.toString(), getOptimizedDexDirectory(applicationId), null,
                                context.getClassLoader());
                        classLoaders.put(applicationId, cl);
                    }
                }
            }
        }
        if(cl == null) {
            throw new NoApplicationExecutableException();
        } else {
            return cl;
        }
    }

}
