package mobilecloud.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Utility functions about classes
 */
public class ClassUtils {
    
    private static Map<String, Class<?>> primitiveTypeClasses = new HashMap<>();
    private static Set<Class<?>> knownBasicTypes = new HashSet<>();
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
        
        knownBasicTypes.add(Void.class);
        knownBasicTypes.add(Boolean.class);
        knownBasicTypes.add(Byte.class);
        knownBasicTypes.add(Character.class);
        knownBasicTypes.add(Short.class);
        knownBasicTypes.add(Integer.class);
        knownBasicTypes.add(Long.class);
        knownBasicTypes.add(Float.class);
        knownBasicTypes.add(Double.class);
        knownBasicTypes.add(BigInteger.class);
        knownBasicTypes.add(BigDecimal.class);
        knownBasicTypes.add(String.class);
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
    
    /**
     * Check if a string represents a primitive type
     * @param type the name of the type
     * @return true if it is primitive type
     */
    public static boolean isPrimitive (String type) {
        return primitiveTypeClasses.containsKey(type);
    }
    
    /**
     * Check if a class is a basic type. A basic type is a type that is safe to
     * keep multiple copies when offloading
     * 
     * @param clazz
     *            the class to check
     * @return if it is basic type or not
     */
    public static boolean isBasicType(Class<?> clazz) {
        return clazz.isEnum() || clazz.isPrimitive() || knownBasicTypes.contains(clazz);
    }
    
    /**
     * Serialize an object to byte array
     * @param obj the object
     * @return the byte array
     * @throws IOException if cannot serialize it
     */
    public static byte[] toBytesArray(Object obj) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(obj);
        out.flush();
        out.close();
        return os.toByteArray();
    }
    
    /**
     * Convert an object to an input stream
     * @param obj the object
     * @return the input stream
     * @throws IOException if convertion fails
     */
    public static InputStream toInputStream(Object obj) throws IOException {
        return new ByteArrayInputStream(toBytesArray(obj));
    }
    
    /**
     * Read object from byte data
     * @param data the object data
     * @param cl the class loader to load class
     * @return the deserialized object
     * @throws SecurityException
     * @throws IOException
     * @throws ClassNotFoundException if cannot load the class
     */
    public static Object readObject(byte[] data, ClassLoader cl) throws SecurityException, IOException, ClassNotFoundException {
        ObjectInputStream is = new AdvancedObjectInputStream(new ByteArrayInputStream(data), cl);
        try {
            return is.readObject();
        } finally {
            is.close();
        }
    }
    
    /**
     * Read object from byte data
     * @param data the object data
     * @return the deserialized data
     * @throws SecurityException
     * @throws ClassNotFoundException if cannot load the class
     * @throws IOException
     */
    public static Object readObject(byte[] data) throws SecurityException, ClassNotFoundException, IOException {
        return readObject(data, ClassLoader.getSystemClassLoader());
    }

}
