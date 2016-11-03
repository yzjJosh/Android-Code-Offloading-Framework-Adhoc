package mobilecloud.api.response;

import java.io.Serializable;

/**
 * Abstract response class
 */
public abstract class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private Throwable throwable;

    public boolean isSuccess() {
        return success;
    }

    public Response setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Response setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }
}
