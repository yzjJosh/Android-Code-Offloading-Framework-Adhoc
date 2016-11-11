package mobilecloud.api.deliverer;

import mobilecloud.api.response.RemoteInvocationResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RemoteInvocationResponseDeliverer extends SimpleDeliverer<Response> {
	public RemoteInvocationResponseDeliverer() {
        super();
    }
    
    public RemoteInvocationResponseDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public void deliver(Response response, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(response instanceof RemoteInvocationResponse)) {
            throw new IllegalArgumentException(response.toString());
        }
        // Write request
        super.deliver(response, is, os);
    }
}
