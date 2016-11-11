package mobilecloud.api.receiver;

import mobilecloud.api.response.GetAvailableServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class GetAvailableServerResponseReceiver extends SimpleReceiver<Response> {
	public GetAvailableServerResponseReceiver() {
		super();
	}

	public GetAvailableServerResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (GetAvailableServerResponse) super.receive(is, os);
	}
}
