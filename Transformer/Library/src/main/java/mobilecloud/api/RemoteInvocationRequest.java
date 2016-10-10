package mobilecloud.api;

import java.util.List;

public class RemoteInvocationRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String applicationId;
    private String clazzName;
    private String methodName;
    private String[] argTypesName;
    private byte[] invokerData;
    private List<byte[]> argsData;

    public String getApplicationId() {
        return applicationId;
    }

    public RemoteInvocationRequest setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getClazzName() {
        return clazzName;
    }

    public RemoteInvocationRequest setClazzName(String clazzName) {
        this.clazzName = clazzName;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public RemoteInvocationRequest setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String[] getArgTypesName() {
        return argTypesName;
    }

    public RemoteInvocationRequest setArgTypesName(String[] argTypesName) {
        this.argTypesName = argTypesName;
        return this;
    }

    public byte[] getInvokerData() {
        return invokerData;
    }

    public RemoteInvocationRequest setInvokerData(byte[] invokerData) {
        this.invokerData = invokerData;
        return this;
    }

    public List<byte[]> getArgsData() {
        return argsData;
    }

    public RemoteInvocationRequest setArgsData(List<byte[]> argsData) {
        this.argsData = argsData;
        return this;
    }
}
