package mobilecloud.api.deliverer;

import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

/**
 * A deliverer is responsible for delivering object to server
 * 
 * @param <T> the write data type
 */
public interface Deliverer<T> {

    /**
     * Deliver an object to server based on specific protocol
     * 
     * @param request
     *            the object to send
     * @param is
     *            the input stream wrapper to read data
     * @param os
     *            the output stream wrapper to send data
     * @throws Exception if error happens
     */
    public void deliver(T request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
            throws Exception;

}
