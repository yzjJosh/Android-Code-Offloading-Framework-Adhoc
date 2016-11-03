package mobilecloud.api.deliverer;

import mobilecloud.api.request.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

/**
 * A deliverer is responsible for delivering requests to server
 */
public interface Deliverer {

    /**
     * Deliver a request to server based on specific protocol
     * 
     * @param request
     *            the request to send
     * @param is
     *            the input stream wrapper to read data
     * @param os
     *            the output stream wrapper to send data
     * @throws Exception if error happens
     */
    public void deliver(Request request, ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os)
            throws Exception;

}
