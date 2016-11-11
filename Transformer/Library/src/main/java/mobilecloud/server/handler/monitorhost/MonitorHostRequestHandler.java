package mobilecloud.server.handler.monitorhost;

import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.MonitorHostResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.handler.Handler;

/**
 * A handler which handles monitor host request
 */
public class MonitorHostRequestHandler implements Handler {
    
    private final MetricGenerator metricGenerator;
    
    public MonitorHostRequestHandler(MetricGenerator metricGenerator) {
        this.metricGenerator = metricGenerator;
    }

    @Override
    public Response handle(Request request) throws Exception {
        if(!(request instanceof MonitorHostRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        return new MonitorHostResponse().setMetric(metricGenerator.getMetric()).setSuccess(true);
    }

}
