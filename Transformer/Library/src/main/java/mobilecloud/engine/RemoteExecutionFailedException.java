package mobilecloud.engine;

/**
 * A exception wrapper which contains a real exception
 *
 */
public class RemoteExecutionFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public RemoteExecutionFailedException() {
        super();
    }
    
    public RemoteExecutionFailedException(String message) {
        super(message);
    }
    
    public RemoteExecutionFailedException(String message, Throwable t) {
        super(message, t);
    }

}
