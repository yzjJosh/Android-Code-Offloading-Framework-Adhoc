package mobilecloud.server;

import java.io.File;

import android.content.Context;
import dalvik.system.PathClassLoader;

/**
 * Load apk to a class loader
 *
 */
public class ExecutableLoader {
    
    private Context context;
    
    public ExecutableLoader(Context context) {
        this.context = context;
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
     * @throws NoApplicationExecutableException if not application executable has been found
     */
    public ClassLoader loadExecutable(String applicationId) throws NoApplicationExecutableException {
        if (new File(getExecutableDirectory(applicationId)).exists()) {
            StringBuilder path = new StringBuilder();
            for(File dex: new File(getExecutableDirectory(applicationId)).listFiles()) {
                if(path.length() > 0) {
                    path.append(':');
                }
                path.append(dex.getPath());
            }
            if(path.length() > 0) {
                return new PathClassLoader(path.toString(), context.getClassLoader());
            }
        }
        throw new NoApplicationExecutableException();
    }

}
