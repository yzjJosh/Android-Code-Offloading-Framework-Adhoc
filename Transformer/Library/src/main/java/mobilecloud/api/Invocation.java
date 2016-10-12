package mobilecloud.api;

import java.io.Serializable;

import mobilecloud.objs.Token;

public class Invocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object invoker;
    private Object[] args;
    private Token token;

    public Object getInvoker() {
        return invoker;
    }

    public Invocation setInvoker(Object invoker) {
        this.invoker = invoker;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    public Invocation setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public Invocation setToken(Token token) {
        this.token = token;
        return this;
    }
}
