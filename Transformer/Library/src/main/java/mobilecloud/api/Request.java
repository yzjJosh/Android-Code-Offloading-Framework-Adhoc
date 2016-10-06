package mobilecloud.api;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Abstract request class
 */
@Accessors(chain = true)
@Getter
@Setter
public abstract class Request implements Serializable{
    private static final long serialVersionUID = 1L;
    private transient String ip;
    private transient int port;
}
