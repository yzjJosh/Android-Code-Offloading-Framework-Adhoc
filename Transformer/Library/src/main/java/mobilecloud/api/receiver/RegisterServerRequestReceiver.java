package mobilecloud.api.receiver;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RegisterServerRequestReceiver extends SimpleReceiver<Request> {
    
    public RegisterServerRequestReceiver() {
        super();
    }
    
    public RegisterServerRequestReceiver(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public Request receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        return (RegisterServerRequest) super.receive(is, os);
    }

}
