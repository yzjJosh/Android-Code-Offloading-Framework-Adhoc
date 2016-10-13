package mobilecloud.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper which wraps a common input stream. It can construct an advanced object input
 * stream in on-demand manner
 *
 */
public class AdvancedObjectInputStreamWrapper extends ObjectInputStreamWrapper {

    public AdvancedObjectInputStreamWrapper(InputStream in) {
        super(in);
    }
    
    @Override
    public AdvancedObjectInputStream get() throws IOException {
        if(objIn == null) {
            objIn = new AdvancedObjectInputStream(in);
        }
        return (AdvancedObjectInputStream) objIn;
    }

}
