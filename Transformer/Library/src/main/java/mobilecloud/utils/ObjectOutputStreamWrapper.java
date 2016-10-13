package mobilecloud.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * A wrapper which wraps an output stream. It can construct an object output stream in on-demand manner
 */
public class ObjectOutputStreamWrapper {

    protected OutputStream os;
    protected ObjectOutputStream objOs;
    
    public ObjectOutputStreamWrapper(OutputStream os) {
        this.os = os;
    }
    
    public ObjectOutputStream get() throws IOException {
        if(objOs == null) {
            objOs = new ObjectOutputStream(os);
        }
        return objOs;
    }
    
}
