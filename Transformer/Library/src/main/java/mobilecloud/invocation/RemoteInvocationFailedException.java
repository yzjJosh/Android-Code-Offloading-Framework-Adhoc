package mobilecloud.invocation;

/**
 * 
 * Indicate that remote execution fails
 */
public class RemoteInvocationFailedException extends Exception{

    private static final long serialVersionUID = 1L;
    
    public RemoteInvocationFailedException() {
        super();
    }
    
    public RemoteInvocationFailedException(String message) {
        super(message);
    }
    
}
