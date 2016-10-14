package mobilecloud.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility functions for input/output
 *
 */
public class IOUtils {
    
    /**
     * Serialize an object to byte array
     * @param obj the object
     * @return the byte array
     * @throws IOException if cannot serialize it
     */
    public static byte[] toBytesArray(Object obj) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(obj);
        out.flush();
        out.close();
        return os.toByteArray();
    }
    
    /**
     * Convert an object to an input stream
     * @param obj the object
     * @return the input stream
     * @throws IOException if convertion fails
     */
    public static InputStream toInputStream(Object obj) throws IOException {
        return new ByteArrayInputStream(toBytesArray(obj));
    }
    
    /**
     * Read object from byte data
     * @param data the object data
     * @param cl the class loader to load class
     * @return the deserialized object
     * @throws SecurityException
     * @throws IOException
     * @throws ClassNotFoundException if cannot load the class
     */
    @SuppressWarnings("resource")
    public static Object readObject(byte[] data, ClassLoader cl) throws IOException, ClassNotFoundException {
        ObjectInputStream is = null;
        try {
            is = new AdvancedObjectInputStream(new ByteArrayInputStream(data)).setClassLoader(cl);
            return is.readObject();
        } finally {
            is.close();
        }
    }
    
    /**
     * Read object from byte data
     * @param data the object data
     * @return the deserialized data
     * @throws SecurityException
     * @throws ClassNotFoundException if cannot load the class
     * @throws IOException
     */
    public static Object readObject(byte[] data) throws ClassNotFoundException, IOException {
        return readObject(data, ClassLoader.getSystemClassLoader());
    }
    
    /**
     * Read bytes from an input stream
     * @param in the input stream
     * @return bytes
     * @throws IOException
     */
    public static byte[] inputStreamToByteArray(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream arrayOs = new ByteArrayOutputStream();
            byte[] buffer = new byte[1<<10];
            int count = 0;
            while((count = in.read(buffer)) != -1) {
                arrayOs.write(buffer, 0, count);
            }
            return arrayOs.toByteArray();
        } finally {
            in.close();
        }
    }
}
