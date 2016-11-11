package mobilecloud.api.deliverer;

import mobilecloud.api.response.Response;
import mobilecloud.api.response.UploadApplicationExecutableResponse;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class UploadApplicationExecutableResponseDeliverer extends SimpleDeliverer<Response> {
	public UploadApplicationExecutableResponseDeliverer() {
        super();
    }
    
    public UploadApplicationExecutableResponseDeliverer(MetricGenerator metricGenerator) {
        super(metricGenerator);
    }

    @Override
    public void deliver(Response response, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        if (!(response instanceof UploadApplicationExecutableResponse)) {
            throw new IllegalArgumentException(response.toString());
        }
        // Write request
        super.deliver(response, is, os);
    }
}
