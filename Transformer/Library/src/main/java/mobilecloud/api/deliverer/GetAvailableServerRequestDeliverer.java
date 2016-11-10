package mobilecloud.api.deliverer;

import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class GetAvailableServerRequestDeliverer extends SimpleDeliverer<Request> {
    
    public GetAvailableServerRequestDeliverer() {
        super();
    }
    
    public GetAvailableServerRequestDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public void deliver(Request request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(request instanceof GetAvailableServerRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        // Write request
        super.deliver(request, is, os);
    }

}
