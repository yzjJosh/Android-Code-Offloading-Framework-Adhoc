package mobilecloud.server.handler;

import mobilecloud.api.request.Request;
import mobilecloud.api.response.Response;

/**
 * A handler is used by server to handle a specific request
 */
public interface Handler {
    
    /**
     * Handle a request
     * @param request the request to be handled
     * @return the response
     * @throws Exception if there is an internal exception which should be hidden to client
     */
    public Response handle(Request request) throws Exception;
    
}
