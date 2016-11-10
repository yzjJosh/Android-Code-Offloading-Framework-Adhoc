package mobilecloud.api.receiver;

import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class GetAvailableServerRequestReceiver extends SimpleReceiver<Request>{
    
    public GetAvailableServerRequestReceiver() {
        super();
    }
    
    public GetAvailableServerRequestReceiver(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public Request receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        return (GetAvailableServerRequest) super.receive(is, os);
    }

}
