package mobilecloud.api.receiver;

import mobilecloud.api.response.MonitorHostResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class MonitorHostResponseReceiver extends SimpleReceiver<Response> {
	public MonitorHostResponseReceiver() {
		super();
	}

	public MonitorHostResponseReceiver(MetricGenerator metricGenerator) {
		super(metricGenerator);
	}

	@Override
	public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
			throws Exception {
		return (MonitorHostResponse) super.receive(is, os);
	}
}
