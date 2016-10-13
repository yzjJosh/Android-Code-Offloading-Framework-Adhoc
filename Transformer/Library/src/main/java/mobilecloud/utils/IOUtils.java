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
    public static Object readObject(byte[] data, ClassLoader cl) throws SecurityException, IOException, ClassNotFoundException {
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
    public static Object readObject(byte[] data) throws SecurityException, ClassNotFoundException, IOException {
        return readObject(data, ClassLoader.getSystemClassLoader());
    }
}
