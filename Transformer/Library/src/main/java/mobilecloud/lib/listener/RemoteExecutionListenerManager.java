package mobilecloud.lib.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mobilecloud.utils.ClassUtils;

/**
 * A class that maintains instances of remote execution listener instances
 */
public class RemoteExecutionListenerManager {

    private static Map<Class<? extends RemoteExecutionListener>, RemoteExecutionListener> listeners = new ConcurrentHashMap<>();

    public static RemoteExecutionListener getListener(Class<? extends RemoteExecutionListener> clazz)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (!listeners.containsKey(clazz)) {
            synchronized (RemoteExecutionListenerManager.class) {
                if (!listeners.containsKey(clazz)) {
                    listeners.put(clazz, ClassUtils.newInstance(clazz));
                }
            }
        }
        return listeners.get(clazz);
    }

}
