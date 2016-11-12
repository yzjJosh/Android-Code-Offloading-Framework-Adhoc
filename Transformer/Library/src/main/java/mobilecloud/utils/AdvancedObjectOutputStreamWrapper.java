package mobilecloud.utils;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A wrapper which wraps an output stream. It can construct an advanced object output
 * stream in on-demand manner
 */
public class AdvancedObjectOutputStreamWrapper extends ObjectOutputStreamWrapper {

    private boolean reset;
    
    public AdvancedObjectOutputStreamWrapper(OutputStream os) {
        super(os);
        this.reset = false;
    }
    
    /**
     * Reset the statistics of the inner output stream
     */
    public void resetStat() {
        this.reset = true;
    }
    
    @Override
    public AdvancedObjectOutputStream get() throws IOException {
        if(objOs == null) {
            objOs = new AdvancedObjectOutputStream(os);
        }
        AdvancedObjectOutputStream res = (AdvancedObjectOutputStream) objOs;
        if(reset) {
            res.resetStat();
            reset = false;
        }
        return res;
    }

}
