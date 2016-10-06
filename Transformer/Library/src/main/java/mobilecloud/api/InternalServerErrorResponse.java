package mobilecloud.api;

import lombok.NonNull;
import mobilecloud.server.InternalServerError;

/**
 * A response indicating that there is an error in server
 *
 */
public class InternalServerErrorResponse extends Response{

    private static final long serialVersionUID = 1L;

    public InternalServerErrorResponse(@NonNull InternalServerError e) {
        this.setSuccess(false);
        this.setThrowable(e);
    }
}
