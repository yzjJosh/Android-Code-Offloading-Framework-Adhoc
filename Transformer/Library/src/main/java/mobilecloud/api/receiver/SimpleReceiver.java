package mobilecloud.api.receiver;

import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

/**
 * A simple receiver simply read object from input stream
 *
 * @param <T> the read data type
 */
public class SimpleReceiver<T> implements Receiver<T> {
    
    private MetricGenerator metricGenerator;
    
    public SimpleReceiver() {
        this(null);
    }
    
    public SimpleReceiver(MetricGenerator metricGenerator) {
        this.metricGenerator = metricGenerator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        is.get().resetStat();
        T res = (T) is.get().readObject();
        if(metricGenerator != null) {
            metricGenerator.reportRead(is.get().getBytesRead());
        }
        return res;
    }

}
