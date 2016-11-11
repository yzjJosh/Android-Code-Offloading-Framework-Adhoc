package mobilecloud.api.receiver;

import mobilecloud.api.response.IllegalRequestResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class IllegalRequestResponseReceiver extends SimpleReceiver<Response> {
	public IllegalRequestResponseReceiver() {
		super();
	}

	public IllegalRequestResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (IllegalRequestResponse) super.receive(is, os);
	}
}
