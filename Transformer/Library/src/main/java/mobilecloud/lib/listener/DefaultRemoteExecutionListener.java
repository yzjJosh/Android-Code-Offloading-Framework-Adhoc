package mobilecloud.lib.listener;

import java.lang.reflect.Method;

/**
 * Default listener, which does nothing
 */
public class DefaultRemoteExecutionListener implements RemoteExecutionListener {

    // Allow any traffic to server
    @Override
    public boolean onRemoteExecutionStart(Method method, Object invoker, Object[] args) {
        return true;
    }

    // Do nothing
    @Override
    public void onRemoteExecutionComplete(Method method, Object invoker, Object[] args, Object returnValue,
            boolean isSuccess, Throwable exception) {
    }

}
