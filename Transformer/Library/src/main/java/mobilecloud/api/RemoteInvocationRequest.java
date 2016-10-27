package mobilecloud.api;

import java.util.Arrays;

import mobilecloud.objs.Token;

public class RemoteInvocationRequest extends Request {
    private static final long serialVersionUID = 1L;
    
    private String applicationId;
    private String clazzName;
    private String methodName;
    private String[] argTypesName;
    private Object invoker;
    private Object[] args;
    private Token token;

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

    public Object getInvoker() {
        return invoker;
    }

    public RemoteInvocationRequest setInvoker(Object invoker) {
        this.invoker = invoker;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    public RemoteInvocationRequest setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public RemoteInvocationRequest setToken(Token token) {
        this.token = token;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RemoteInvocationRequest{\n");
        sb.append("    applicationId: " + getApplicationId() + "\n");
        sb.append("    clazzName: " + getClazzName() + "\n");
        sb.append("    methodName: " + getMethodName() + "\n");
        sb.append("    argTypesName: " + (getArgTypesName() == null? null: Arrays.toString(getArgTypesName())) + "\n");
        sb.append("    invoker: " + getInvoker() + "\n");
        sb.append("    args: " + (getArgs() == null? null: Arrays.toString(getArgs())) + "\n");
        sb.append("    token: " + getToken() + "\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    
}
