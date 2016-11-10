package mobilecloud.api.receiver;

import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

/**
 * A receiver is responsible for receiving objects from client based on specific protocol
 *
 *@param <T> the read data type
 */
public interface Receiver<T> {

    /**
     * Receive an object from stream
     * 
     * @param is
     *            the input stream wrapper to read data
     * @param os
     *            the output stream wrapper to write data
     * @return the object read from stream
     * @throws Exception
     *             if error occurs
     */
    public T receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception;

}
