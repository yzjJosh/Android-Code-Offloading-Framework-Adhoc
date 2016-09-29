package mobilecloud.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Abstract request class
 *
 */
@Accessors(chain = true)
@Getter
@Setter
public abstract class Request {
    private transient String ip;
    private transient int port;
}
