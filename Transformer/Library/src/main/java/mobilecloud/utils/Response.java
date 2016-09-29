package mobilecloud.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Abstract response class
 */
@Accessors(chain = true)
@Setter
@Getter
public abstract class Response {
    private boolean success;
    private Throwable throwable;
}
