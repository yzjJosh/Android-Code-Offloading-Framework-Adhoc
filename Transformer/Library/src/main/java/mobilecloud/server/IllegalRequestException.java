package mobilecloud.server;

/**
 * Thrown when a request is illegal
 *
 */
public class IllegalRequestException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public IllegalRequestException() {
        super();
    }
    
    public IllegalRequestException(String message) {
        super(message);
    }

}
