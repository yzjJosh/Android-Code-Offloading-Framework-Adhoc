package mobilecloud.api;

public class RemoteInvocationResponse extends Response {
    private static final long serialVersionUID = 1L;
    private byte[] returnValueData;
    private byte[] invokerData;
    private byte[] argsData;

    public byte[] getReturnValueData() {
        return returnValueData;
    }

    public RemoteInvocationResponse setReturnValueData(byte[] returnValueData) {
        this.returnValueData = returnValueData;
        return this;
    }

    public byte[] getInvokerData() {
        return invokerData;
    }

    public RemoteInvocationResponse setInvokerData(byte[] invokerData) {
        this.invokerData = invokerData;
        return this;
    }

    public byte[] getArgsData() {
        return argsData;
    }

    public RemoteInvocationResponse setArgsData(byte[] argsData) {
        this.argsData = argsData;
        return this;
    }
}
