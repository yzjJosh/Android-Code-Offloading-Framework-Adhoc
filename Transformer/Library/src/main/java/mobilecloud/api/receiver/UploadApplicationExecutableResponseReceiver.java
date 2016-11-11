package mobilecloud.api.receiver;

import mobilecloud.api.response.Response;
import mobilecloud.api.response.UploadApplicationExecutableResponse;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class UploadApplicationExecutableResponseReceiver extends SimpleReceiver<Response> {
	public UploadApplicationExecutableResponseReceiver() {
		super();
	}

	public UploadApplicationExecutableResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (UploadApplicationExecutableResponse) super.receive(is, os);
	}
}
