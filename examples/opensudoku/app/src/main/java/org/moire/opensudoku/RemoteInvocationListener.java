package org.moire.opensudoku;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;

import mobilecloud.lib.listener.RemoteExecutionListener;

public class RemoteInvocationListener implements RemoteExecutionListener{

    private static final String TAG = RemoteInvocationListener.class.getSimpleName();
    @Override
    public boolean onRemoteExecutionStart(Method method, Object o, Object[] objects) {
        Log.e(TAG, "Start invoking method " + method +
                " remotely, invoker is " + o + ", arguments are " +
                Arrays.toString(objects));
        return true;
    }
    @Override
    public void onRemoteExecutionComplete(Method method, Object o, Object[] objects, Object o1, boolean b, Throwable throwable) {
        Log.e(TAG, "Complete invoking method " + method + " remotely , status is " + (b? ("success, result is " + o1): ("failed, caused by ") + throwable));
    }
}
