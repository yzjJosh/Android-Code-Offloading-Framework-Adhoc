package mobilecloud.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * A wrapper which wraps a common input stream. It can construct an object input
 * stream in on-demand manner
 *
 */
public class ObjectInputStreamWrapper {
    
    protected InputStream in;
    protected ObjectInputStream objIn;
    
    public ObjectInputStreamWrapper(InputStream in) {
        this.in = in;
    }
    
    public ObjectInputStream get() throws IOException {
        if(objIn == null) {
            objIn = new ObjectInputStream(in);
        }
        return objIn;
    }
    
    

}
