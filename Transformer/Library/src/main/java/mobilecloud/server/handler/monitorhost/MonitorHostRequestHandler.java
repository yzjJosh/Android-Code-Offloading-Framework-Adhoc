package mobilecloud.server.handler.monitorhost;

import mobilecloud.api.MonitorHostRequest;
import mobilecloud.api.MonitorHostResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.handler.Handler;

/**
 * A handler which handles monitor host request
 */
public class MonitorHostRequestHandler implements Handler {

    @Override
    public Response handle(Request request) throws Exception {
        if(!(request instanceof MonitorHostRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        return new MonitorHostResponse().setSuccess(true);
    }

}
