package mobilecloud.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.handler.Handler;
import mobilecloud.server.handler.monitorhost.MonitorHostRequestHandler;

/**
 * A local client is a client which handles request locally
 */
public class LocalClient extends Client {
    
    private Map<String, Handler> handlers;
    
    public LocalClient(MetricGenerator metricGenerator) {
        super(null, 0, 0);
        
        this.handlers = new ConcurrentHashMap<>();
        this.handlers.put(MonitorHostRequest.class.getName(), new MonitorHostRequestHandler(metricGenerator));
    }
    
    @Override
    public Response request(Request request) throws Exception {
        Handler handler = handlers.get(request.getClass().getName());
        if(handler == null) {
            throw new IllegalArgumentException(request.getClass().getName());
        }
        return handler.handle(request);
    }

}
