package mobilecloud.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * An advanced input stream that can resolve classes via a class loader. It also provides statistics.
 *
 */
public class AdvancedObjectInputStream extends ObjectInputStream {
    
    private ClassLoader cl;
    private int bytesRead;

    public AdvancedObjectInputStream(InputStream is)
            throws IOException {
        super(is);
        this.cl = ClassLoader.getSystemClassLoader();
        this.bytesRead = 0;
    }
    
    public AdvancedObjectInputStream setClassLoader(ClassLoader loader) {
        this.cl = loader;
        return this;
    }
    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return ClassUtils.loadClass(cl, desc.getName());
    }
    
    @Override
    public int read() throws IOException {
        int data = super.read();
        bytesRead ++;
        return data;
    }
    
    /**
     * Get the number of bytes read from this input stream
     * @return number of bytes read
     */
    public int getBytesRead() {
        return bytesRead;
    }
    
    /**
     * Reset the statistic information of this input stream
     */
    public void resetStat() {
        bytesRead = 0;
    }

}
