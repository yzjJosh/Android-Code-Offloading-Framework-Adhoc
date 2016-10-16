package mobilecloud.api;

import gnu.trove.map.TIntObjectMap;
import mobilecloud.objs.ObjDiff;
import mobilecloud.objs.Token;

public class RemoteInvocationResponse extends Response {
    private static final long serialVersionUID = 1L;

    private TIntObjectMap<ObjDiff> diffs;
    private Token token;
    private Object returnVal;

    public TIntObjectMap<ObjDiff> getDiffs() {
        return diffs;
    }

    public RemoteInvocationResponse setDiffs(TIntObjectMap<ObjDiff> diffs) {
        this.diffs = diffs;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public RemoteInvocationResponse setToken(Token token) {
        this.token = token;
        return this;
    }

    public Object getReturnVal() {
        return returnVal;
    }

    public RemoteInvocationResponse setReturnVal(Object returnVal) {
        this.returnVal = returnVal;
        return this;
    }

}
