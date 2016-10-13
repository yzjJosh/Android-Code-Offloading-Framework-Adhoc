package mobilecloud.server.receiver;

import mobilecloud.api.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

/**
 * A receiver is responsible for receiving requests from client based on sepcified protocol
 *
 */
public interface Receiver {

    /**
     * Receive request from stream
     * 
     * @param is
     *            the input stream wrapper to read request
     * @param os
     *            the output stream wrapper to write data
     * @return the reqeust
     * @throws Exception if error occurs
     */
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception;

}
