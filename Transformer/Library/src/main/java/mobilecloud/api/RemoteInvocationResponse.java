package mobilecloud.api;

import java.util.List;

public class RemoteInvocationResponse extends Response {
    private static final long serialVersionUID = 1L;
    private byte[] returnValueData;
    private byte[] invokerData;
    private List<byte[]> argsData;

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

    public List<byte[]> getArgsData() {
        return argsData;
    }

    public RemoteInvocationResponse setArgsData(List<byte[]> argsData) {
        this.argsData = argsData;
        return this;
    }
}
