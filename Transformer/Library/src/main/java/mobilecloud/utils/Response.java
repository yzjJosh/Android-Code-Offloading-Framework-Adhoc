package mobilecloud.utils;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Abstract response class
 */
@Accessors(chain = true)
@Setter
@Getter
public abstract class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private Throwable throwable;
}
