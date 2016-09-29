package mobilecloud.server;

/**
 * Thrown when there is an internal server error
 *
 */
public class InternalServerError extends Error {
    
    private static final long serialVersionUID = 1L;
    
    public InternalServerError() {
        super();
    }
    
    public InternalServerError(String message) {
        super(message);
    }

}
