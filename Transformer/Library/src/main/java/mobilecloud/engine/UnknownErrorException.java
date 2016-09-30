package mobilecloud.engine;

/**
 * An exception which is thrown if there is an unknown error
 *
 */
public class UnknownErrorException extends Exception{
    static final long serialVersionUID = 1L;
    
    public UnknownErrorException() {
        super();
    }
    
    public UnknownErrorException(String message) {
        super(message);
    }

}
