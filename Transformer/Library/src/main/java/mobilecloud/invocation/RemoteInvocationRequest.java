package mobilecloud.invocation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mobilecloud.utils.Request;

@Getter
@Setter
@Accessors(chain = true)
public class RemoteInvocationRequest extends Request {
    private long applicationId;
    private String clazzName;
    private String methodName;
    private String[] argTypesName;
    private Object invoker;
    private Object[] args;
}
