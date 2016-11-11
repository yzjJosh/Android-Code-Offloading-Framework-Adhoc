package mobilecloud.api.deliverer;

import mobilecloud.api.response.IllegalRequestResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class IllegalRequestResponseDeliverer extends SimpleDeliverer<Response> {
	public IllegalRequestResponseDeliverer() {
        super();
    }
    
    public IllegalRequestResponseDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public void deliver(Response response, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(response instanceof IllegalRequestResponse)) {
            throw new IllegalArgumentException(response.toString());
        }
        // Write request
        super.deliver(response, is, os);
    }
}
