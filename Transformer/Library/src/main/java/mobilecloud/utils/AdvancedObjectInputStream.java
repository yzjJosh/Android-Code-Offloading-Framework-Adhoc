package mobilecloud.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * An advanced input stream that can resolve classes via a class loader
 *
 */
public class AdvancedObjectInputStream extends ObjectInputStream {
    
    private final ClassLoader cl;

    protected AdvancedObjectInputStream(InputStream is, ClassLoader cl)
            throws IOException, SecurityException {
        super(is);
        this.cl = cl;
    }
    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return ClassUtils.loadClass(cl, desc.getName());
    }

}
