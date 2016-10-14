package mobilecloud.lib;

import java.lang.reflect.Method;

import mobilecloud.engine.RemoteExecutionFailedException;

/**
 * Default listener, which does nothing 
 */
public class EmptyRemoteExecutionListener implements RemoteExecutionListener {

    @Override
    public boolean onRemoteExecutionStart(Method method, Object invoker, Object[] args) {
        return true;
    }

    @Override
    public void onRemoteExecutionComplete(Method method, Object invoker, Object[] args, Object returnValue,
            boolean isSuccess, RemoteExecutionFailedException exception) {
    }

}
