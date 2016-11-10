package mobilecloud.api.deliverer;

import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class MonitorHostRequestDeliverer extends SimpleDeliverer<Request> {
    
    public MonitorHostRequestDeliverer() {
        super();
    }
    
    public MonitorHostRequestDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }
    
    @Override
    public void deliver(Request request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof MonitorHostRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        
        // Write request
        super.deliver(request, is, os);
    }

}
