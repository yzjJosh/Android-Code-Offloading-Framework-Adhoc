package mobilecloud.invocation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mobilecloud.utils.Response;

@Accessors(chain = true)
@Getter
@Setter
public class RemoteInvocationResponse extends Response {
    private Object returnValue;
    private Object invoker;
    private Object[] args;
}
