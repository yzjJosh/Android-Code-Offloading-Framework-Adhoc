package mobilecloud.api.receiver;

import mobilecloud.api.response.InternalServerErrorResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class InternalServerErrorResponseReceiver extends SimpleReceiver<Response> {
	public InternalServerErrorResponseReceiver() {
		super();
	}

	public InternalServerErrorResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (InternalServerErrorResponse) super.receive(is, os);
	}
}
