package mobilecloud.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper which wraps a common input stream. It can construct an advanced object input
 * stream in on-demand manner
 *
 */
public class AdvancedObjectInputStreamWrapper extends ObjectInputStreamWrapper {

    private boolean reset;
    
    public AdvancedObjectInputStreamWrapper(InputStream in) {
        super(in);
        this.reset = false;
    }
    
    /**
     * Reset the statistic information of inner input stream
     */
    public void resetStat() {
        this.reset = true;
    }
    
    @Override
    public AdvancedObjectInputStream get() throws IOException {
        if(objIn == null) {
            objIn = new AdvancedObjectInputStream(in);
        }
        AdvancedObjectInputStream res = (AdvancedObjectInputStream) objIn;
        if(reset) {
            res.resetStat();
            reset = false;
        }
        return res;
    }

}
