package mobilecloud.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * An object output stream that supports statistic record
 *
 */
public class AdvancedObjectOutputStream extends ObjectOutputStream {

    private int bytesWritten;
    
    protected AdvancedObjectOutputStream(OutputStream os) throws IOException {
        super(os);
        this.bytesWritten = 0;
    }
    
    @Override
    public void write(int val) throws IOException {
        super.write(val);
        bytesWritten ++;
    }
    
    /**
     * Get the number of bytes written to this output stream
     * @return the number bytes written
     */
    public int getBytesWritten() {
        return bytesWritten;
    }
    
    /**
     * Reset the statistics of this output stream
     */
    public void resetStat() {
        bytesWritten = 0;
    }

}
