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
    
    private ClassLoader cl;

    public AdvancedObjectInputStream(InputStream is)
            throws IOException {
        super(is);
        this.cl = ClassLoader.getSystemClassLoader();
    }
    
    public AdvancedObjectInputStream setClassLoader(ClassLoader loader) {
        this.cl = loader;
        return this;
    }
    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return ClassUtils.loadClass(cl, desc.getName());
    }

}
