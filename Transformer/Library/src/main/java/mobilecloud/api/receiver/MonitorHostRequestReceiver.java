package mobilecloud.api.receiver;

import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class MonitorHostRequestReceiver extends SimpleReceiver<Request> {
    
    public MonitorHostRequestReceiver() {
        super();
    }
    
    public MonitorHostRequestReceiver(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public Request receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        return (MonitorHostRequest) super.receive(is, os);
    }

}
