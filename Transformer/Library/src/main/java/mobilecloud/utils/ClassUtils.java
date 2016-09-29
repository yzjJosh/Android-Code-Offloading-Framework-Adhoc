package mobilecloud.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions about classes
 */
public class ClassUtils {
    
    private static Map<String, Class<?>> primitiveTypeClasses = new HashMap<>();
    static {
        primitiveTypeClasses.put(void.class.getName(), void.class);
        primitiveTypeClasses.put(boolean.class.getName(), boolean.class);
        primitiveTypeClasses.put(byte.class.getName(), byte.class);
        primitiveTypeClasses.put(char.class.getName(), char.class);
        primitiveTypeClasses.put(short.class.getName(), short.class);
        primitiveTypeClasses.put(int.class.getName(), int.class);
        primitiveTypeClasses.put(long.class.getName(), long.class);
        primitiveTypeClasses.put(float.class.getName(), float.class);
        primitiveTypeClasses.put(double.class.getName(), double.class);
    }
    
    /**
     * Load a class from its name
     * @param name the name of class
     * @return the class
     * @throws ClassNotFoundException cannot find this class
     */
    public static Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = primitiveTypeClasses.get(name);
        if(clazz != null) {
            return clazz;
        } else {
            return Class.forName(name);
        }
    }
    
    /**
     * Load a class from class loader. If it fails, try to load it from system.
     * @param cl the class loader.
     * @param name the name of class
     * @return the class
     * @throws ClassNotFoundException cannot find this class
     */
    public static Class<?> loadClass(ClassLoader cl, String name) throws ClassNotFoundException {
        try {
            return cl.loadClass(name);
        } catch(ClassNotFoundException e) {
            return loadClass(name);
        }
    }

}
