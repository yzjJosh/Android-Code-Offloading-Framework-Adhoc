package mobilecloud.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
    
    /**
     * Read a file into a byte array
     * @param path the file path
     * @return the byte array
     * @throws IOException if error happens
     */
    public static byte[] readBytes(String path) throws IOException {
        return IOUtils.inputStreamToByteArray(new FileInputStream(path));
    }
    
    /**
     * Recursively delete a folder
     * @param folder the folder path
     */
    public static void deleteFolder(String folder) {
        File f = new File(folder);
        if(!f.exists()) {
            return;
        }
        if(f.isDirectory()) {
            for(File next: f.listFiles()) {
                deleteFolder(next.getPath());
            }
        } else {
            f.delete();
        }
    }

}
