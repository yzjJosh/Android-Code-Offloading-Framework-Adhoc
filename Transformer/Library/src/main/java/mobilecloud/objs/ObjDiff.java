package mobilecloud.objs;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;

import mobilecloud.objs.field.FieldReader;
import mobilecloud.objs.field.FieldValue;

/**
 * Object diff represents the difference of an object's fields
 */
public class ObjDiff implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<Object, FieldValue> diffMap;

    public ObjDiff(Map<Object, FieldValue> diffMap) {
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
        for (Object field : diffMap.keySet()) {
            if(clazz.isArray()) {
                Array.set(obj, (Integer) field, reader.read(diffMap.get(field)));
            } else {
                Field f = clazz.getDeclaredField((String) field);
                f.setAccessible(true);
                f.set(obj, reader.read(diffMap.get(field)));
            }
        }
    }
    
    @Override
    public String toString() {
        return diffMap.toString();
    }

}
