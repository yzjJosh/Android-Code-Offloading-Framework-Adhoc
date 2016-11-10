package mobilecloud.utils;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A wrapper which wraps an output stream. It can construct an advanced object output
 * stream in on-demand manner
 */
public class AdvancedObjectOutputStreamWrapper extends ObjectOutputStreamWrapper {

    public AdvancedObjectOutputStreamWrapper(OutputStream os) {
        super(os);
    }
    
    @Override
    public AdvancedObjectOutputStream get() throws IOException {
        if(objOs == null) {
            objOs = new AdvancedObjectOutputStream(os);
        }
        return (AdvancedObjectOutputStream) objOs;
    }

}
