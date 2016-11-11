package mobilecloud.api.receiver;

import mobilecloud.api.response.RemoteInvocationResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RemoteInvocationResponseReceiver extends SimpleReceiver<Response> {
	public RemoteInvocationResponseReceiver() {
		super();
	}

	public RemoteInvocationResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (RemoteInvocationResponse) super.receive(is, os);
	}
}
