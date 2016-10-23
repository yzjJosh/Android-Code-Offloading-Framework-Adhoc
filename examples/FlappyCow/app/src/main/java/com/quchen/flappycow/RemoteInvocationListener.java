package com.quchen.flappycow;

import android.util.Log;

import com.quchen.flappycow.sprites.Sprite;

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
        Sprite s = (Sprite) o;
        Log.e(TAG, "Complete invoking method " + method + " remotely , status is " + (b? "success": "failed") + ", x = " + s.getX() + ", y = " + s.getY());
    }
}
