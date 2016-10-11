package mobilecloud.objs;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Object diff represents the difference of an object's fields
 */
public class ObjDiff implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, FieldValue> diffMap;

    public ObjDiff(Map<String, FieldValue> diffMap) {
        this.diffMap = diffMap;
    }

    /**
     * Apply this diff on a given object
     * @param obj the object which accepts this diff
     * @param reader a reader which reads field value
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    public void apply(Object obj, FieldReader reader)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        for (String fieldName : diffMap.keySet()) {
            if(clazz.isArray()) {
                int i = Integer.valueOf(fieldName);
                Array.set(obj, i, reader.read(diffMap.get(fieldName)));
            } else {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(obj, reader.read(diffMap.get(fieldName)));
            }
        }
    }
    
    @Override
    public String toString() {
        return diffMap.toString();
    }

}
