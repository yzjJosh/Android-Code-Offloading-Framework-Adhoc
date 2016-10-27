package mobilecloud.api;

import mobilecloud.server.InternalServerError;

/**
 * A response indicating that there is an error in server
 *
 */
public class InternalServerErrorResponse extends Response{

    private static final long serialVersionUID = 1L;

    public InternalServerErrorResponse(InternalServerError e) {
        this.setSuccess(false);
        this.setThrowable(e);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InternalServerErrorResponse{\n");
        sb.append("    throwable: " + getThrowable() + "\n");
        sb.append("}\n");
        return sb.toString();
    }
}
