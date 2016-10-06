package mobilecloud.api;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class RemoteInvocationResponse extends Response {
    private static final long serialVersionUID = 1L;
    private Serializable returnValue;
    private Serializable invoker;
    private Serializable[] args;
}
