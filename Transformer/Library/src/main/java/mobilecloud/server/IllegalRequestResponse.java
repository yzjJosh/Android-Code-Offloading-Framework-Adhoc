package mobilecloud.server;

import lombok.NonNull;
import mobilecloud.utils.Response;

/**
 * A response indicating that a request is illegal
 *
 */
public class IllegalRequestResponse extends Response{

    private static final long serialVersionUID = 1L;

    public IllegalRequestResponse(@NonNull IllegalRequestException e) {
        this.setSuccess(false);
        this.setThrowable(e);
    }
}
