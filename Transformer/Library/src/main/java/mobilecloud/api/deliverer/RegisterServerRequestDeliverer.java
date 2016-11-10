package mobilecloud.api.deliverer;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RegisterServerRequestDeliverer extends SimpleDeliverer<Request> {
    
    public RegisterServerRequestDeliverer() {
        super();
    }
    
    public RegisterServerRequestDeliverer(MetricGenerator generator) {
        super(generator);
    }

    @Override
    public void deliver(Request request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(request instanceof RegisterServerRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        // Write request
        super.deliver(request, is, os);
    }

}
