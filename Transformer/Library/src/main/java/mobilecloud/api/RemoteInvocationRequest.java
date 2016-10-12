package mobilecloud.api;

public class RemoteInvocationRequest extends Request {
    private static final long serialVersionUID = 1L;
    
    private String applicationId;
    private String clazzName;
    private String methodName;
    private String[] argTypesName;
    private byte[] invocationData;

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

    public byte[] getInvocationData() {
        return invocationData;
    }

    public RemoteInvocationRequest setInvocationData(byte[] invocationData) {
        this.invocationData = invocationData;
        return this;
    }
    
    
}
