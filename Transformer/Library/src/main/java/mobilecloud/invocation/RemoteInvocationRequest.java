package mobilecloud.invocation;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mobilecloud.utils.Request;

@Getter
@Setter
@Accessors(chain = true)
public class RemoteInvocationRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String applicationId;
    private String clazzName;
    private String methodName;
    private String[] argTypesName;
    private Serializable invoker;
    private Serializable[] args;
}
