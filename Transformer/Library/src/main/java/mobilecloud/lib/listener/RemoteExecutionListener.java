package mobilecloud.lib.listener;

import java.lang.reflect.Method;

/**
 * An interface that allows users to listen to remote execution events
 *
 */
public interface RemoteExecutionListener {

    /**
     * Called when a remote execution starts
     * 
     * @param method
     *            the method which will be executed remotely
     * @param invoker
     *            the invoker to be invoked
     * @param args
     *            the args to be passed to method
     * @return true if this execution should run remotely, false if it should be
     *         run locally
     */
    public boolean onRemoteExecutionStart(Method method, Object invoker, Object[] args);

    /**
     * Called when a remote execution completes
     * 
     * @param method
     *            the method which is executed remotely
     * @param invoker
     *            the invoker to be invoked
     * @param args
     *            the args to be passed to the method
     * @param returnValue
     *            the value which will be returned, or null if execution fails
     * @param isSuccess
     *            if the execution is successful
     * @param exception
     *            the exception that is thrown if execution fails
     */
    public void onRemoteExecutionComplete(Method method, Object invoker, Object[] args, Object returnValue,
            boolean isSuccess, Throwable exception);

}
