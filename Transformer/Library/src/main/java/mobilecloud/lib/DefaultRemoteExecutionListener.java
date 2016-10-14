package mobilecloud.lib;

import java.lang.reflect.Method;

import mobilecloud.engine.RemoteExecutionFailedException;

/**
 * Default listener, which does nothing 
 */
public class DefaultRemoteExecutionListener implements RemoteExecutionListener {

    //Allow any traffic to server
    @Override
    public boolean onRemoteExecutionStart(Method method, Object invoker, Object[] args) {
        return true;
    }

    // Print stacktrace of errors
    @Override
    public void onRemoteExecutionComplete(Method method, Object invoker, Object[] args, Object returnValue,
            boolean isSuccess, RemoteExecutionFailedException exception) {
        if(!isSuccess) {
            exception.printStackTrace();
        }
    }

}
