package mobilecloud.server;

import mobilecloud.api.request.Request;
import mobilecloud.api.response.Response;

/**
 * Server listener is a listener that can be used to monitor the server's activities
 *
 */
public interface ServerListener {
    
    /**
     * Called when starts receiving a request
     * @param requestType the type of request to be received
     */
    public void onRequestReceivingStarts(String requestType);
    
    /**
     * Called when a request has been received
     * @param req the request received
     */
    public void onRequestReceived(Request req);
    
    /**
     * Called when the response has been sent to client
     * @param req the request handled
     * @param resp the response sent to client
     */
    public void onResponseSent(Request req, Response resp);
}
