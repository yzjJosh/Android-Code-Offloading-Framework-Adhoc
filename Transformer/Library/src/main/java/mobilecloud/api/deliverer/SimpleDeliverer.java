package mobilecloud.api.deliverer;

import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

/**
 * A simple deliverer just simply sends the object via output stream
 * 
 * @param <T> the write data type
 */
public class SimpleDeliverer<T> implements Deliverer<T> {
    
private final MetricGenerator metricGenerator;
    
    public SimpleDeliverer() {
        this(null);
    }
    
    public SimpleDeliverer(MetricGenerator metricGenerator) {
        this.metricGenerator = metricGenerator;
    }

    @Override
    public void deliver(T obj, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
            throws Exception {
        // Write request
        os.get().resetStat();
        os.get().writeObject(obj);
        os.get().flush();
        if(metricGenerator != null) {
            metricGenerator.reportWrite(os.get().getBytesWritten());
        }
    }

}
