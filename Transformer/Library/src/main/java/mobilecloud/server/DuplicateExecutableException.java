package mobilecloud.server;

/**
 * An exception that indicates receiving of duplicate executable
 */
public class DuplicateExecutableException extends Exception{

    private static final long serialVersionUID = 1L;
    
    public DuplicateExecutableException() {
        super();
    }
    
    public DuplicateExecutableException(String msg) {
        super(msg);
    }
    
}
