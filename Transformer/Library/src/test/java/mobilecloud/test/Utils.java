package mobilecloud.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

/**
 * Utility functions for testing
 *
 */
public class Utils {
    
    public static byte[] toBytesArray(Object obj) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(obj);
        out.flush();
        out.close();
        return os.toByteArray();
    }
    
    public static InputStream toInputStream(Object obj) throws IOException {
        return new ByteArrayInputStream(toBytesArray(obj));
    }
    
}
