package mobilecloud.api.deliverer;

import mobilecloud.api.response.GetAvailableServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class GetAvailableServerResponseDeliverer extends SimpleDeliverer<Response> {
    public GetAvailableServerResponseDeliverer() {
        super();
    }
    
    public GetAvailableServerResponseDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public void deliver(Response response, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(response instanceof GetAvailableServerResponse)) {
            throw new IllegalArgumentException(response.toString());
        }
        // Write request
        super.deliver(response, is, os);
    }
}
