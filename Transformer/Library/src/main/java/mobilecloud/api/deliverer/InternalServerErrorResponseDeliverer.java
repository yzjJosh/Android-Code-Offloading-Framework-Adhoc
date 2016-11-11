package mobilecloud.api.deliverer;

import mobilecloud.api.response.InternalServerErrorResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class InternalServerErrorResponseDeliverer extends SimpleDeliverer<Response> {
	public InternalServerErrorResponseDeliverer() {
        super();
    }
    
    public InternalServerErrorResponseDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public void deliver(Response response, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(response instanceof InternalServerErrorResponse)) {
            throw new IllegalArgumentException(response.toString());
        }
        // Write request
        super.deliver(response, is, os);
    }
}
