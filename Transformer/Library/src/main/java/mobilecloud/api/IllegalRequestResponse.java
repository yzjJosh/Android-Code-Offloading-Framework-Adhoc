package mobilecloud.api;

import mobilecloud.server.IllegalRequestException;

/**
 * A response indicating that a request is illegal
 *
 */
public class IllegalRequestResponse extends Response{

    private static final long serialVersionUID = 1L;

    public IllegalRequestResponse(IllegalRequestException e) {
        this.setSuccess(false);
        this.setThrowable(e);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IllegalRequestResponse{\n");
        sb.append("    throwable: " + getThrowable() + "\n");
        sb.append("}\n");
        return sb.toString();
    }
}
