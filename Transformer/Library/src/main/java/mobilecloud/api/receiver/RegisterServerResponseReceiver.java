package mobilecloud.api.receiver;

import mobilecloud.api.response.RegisterServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RegisterServerResponseReceiver extends SimpleReceiver<Response> {
	public RegisterServerResponseReceiver() {
		super();
	}

	public RegisterServerResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (RegisterServerResponse) super.receive(is, os);
	}
}
