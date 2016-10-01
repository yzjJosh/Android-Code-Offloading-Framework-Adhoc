package mobilecloud.engine;

public class RemoteExecutionFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private Throwable t;
    
    public RemoteExecutionFailedException() {
        super();
    }
    
    public RemoteExecutionFailedException(String message) {
        super(message);
    }
    
    public RemoteExecutionFailedException withReaseon(Throwable t) {
        this.t = t;
        return this;
    }
    
    public Throwable getReason() {
        return t;
    }

}
