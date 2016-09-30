package mobilecloud.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

/**
 * Utility functions about classes
 */
public class ClassUtils {
    
    private static Map<String, Class<?>> primitiveTypeClasses = new HashMap<>();
    private static Set<Class<?>> knownImmutableClasses = new HashSet<>();
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
        
        knownImmutableClasses.add(Void.class);
        knownImmutableClasses.add(Boolean.class);
        knownImmutableClasses.add(Byte.class);
        knownImmutableClasses.add(Character.class);
        knownImmutableClasses.add(Short.class);
        knownImmutableClasses.add(Integer.class);
        knownImmutableClasses.add(Long.class);
        knownImmutableClasses.add(Float.class);
        knownImmutableClasses.add(Double.class);
        knownImmutableClasses.add(BigInteger.class);
        knownImmutableClasses.add(BigDecimal.class);
        knownImmutableClasses.add(String.class);
        knownImmutableClasses.add(Object.class);
    }
    
    /**
     * Load a class from its name
     * @param name the name of class
     * @return the class
     * @throws ClassNotFoundException cannot find this class
     */
    public static Class<?> loadClass(@NonNull String name) throws ClassNotFoundException {
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
    public static Class<?> loadClass(@NonNull ClassLoader cl, @NonNull String name) throws ClassNotFoundException {
        try {
            return cl.loadClass(name);
        } catch(ClassNotFoundException e) {
            return loadClass(name);
        }
    }
    
    /**
     * Check if a string represents a primitive type
     * @param type the name of the type
     * @return true if it is primitive type
     */
    public static boolean isPrimitive (@NonNull String type) {
        return primitiveTypeClasses.containsKey(type);
    }
    
    /**
     * Check if a class is immutable for the best practice. This method may have false negatives
     * @param clazz the class to check
     * @return if it is immutable or not
     */
    public static boolean isImmutable(@NonNull Class<?> clazz) {
        if(clazz.isPrimitive() || clazz.isEnum()) {
            return true;
        } else {
            for(Field f: clazz.getDeclaredFields()) {
                if(Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
                    continue;
                }
                if(!Modifier.isFinal(f.getModifiers()) || !knownImmutableClasses.contains(f.getType())) {
                    return false;
                }
            }
            return true;
        }
    }

}
