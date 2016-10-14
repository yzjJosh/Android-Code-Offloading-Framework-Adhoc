package mobilecloud.engine;

/**
 * Exception that is thrown when remote execution is aborted
 *
 */
public class RemoteExecutionAbortedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public RemoteExecutionAbortedException() {
        super();
    }
    
    public RemoteExecutionAbortedException(String message) {
        super(message);
    }

}
