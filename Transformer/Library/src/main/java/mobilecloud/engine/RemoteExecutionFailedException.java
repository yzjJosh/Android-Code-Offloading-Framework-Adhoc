package mobilecloud.engine;

/**
 * A exception wrapper which contains a real exception
 *
 */
public class RemoteExecutionFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private Throwable t;
    
    public RemoteExecutionFailedException() {
        super();
    }
    
    public RemoteExecutionFailedException(String message) {
        super(message);
    }
    
    /**
     * Set the real exception wrapped inside this exception
     * @param t the throwable
     * @return this exception itself
     */
    public RemoteExecutionFailedException withReaseon(Throwable t) {
        this.t = t;
        return this;
    }
    
    /**
     * Get the exception inside
     * @return the real exception
     */
    public Throwable getReason() {
        return t;
    }

}
