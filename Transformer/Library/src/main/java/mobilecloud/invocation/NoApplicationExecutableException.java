package mobilecloud.invocation;

/**
 * Indicate that we cannot find a legal application executable
 */
public class NoApplicationExecutableException extends Exception{

    private static final long serialVersionUID = 1L;
    
    public NoApplicationExecutableException() {
        super();
    }
    
    public NoApplicationExecutableException(String message) {
        super(message);
    }

}
