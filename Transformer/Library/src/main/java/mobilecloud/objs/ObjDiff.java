package mobilecloud.objs;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import gnu.trove.iterator.TIntIterator;

/**
 * Object diff represents the difference of an object's fields
 */
public class ObjDiff implements Serializable {
    private static final long serialVersionUID = 1L;

    private ObjMap diffMap;

    public ObjDiff(ObjMap diffMap) {
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
        if(clazz.isArray()) {
            TIntIterator it = diffMap.keys();
            while(it.hasNext()) {
                int index = it.next();
                Array.set(obj, index, reader.read(diffMap, index));
            }
        } else {
            for(Field f: clazz.getDeclaredFields()) {
                int modifier = f.getModifiers();
                if(Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) {
                    continue;
                }
                if(diffMap.containsKey(f)) {
                    f.setAccessible(true);
                    f.set(obj, reader.read(diffMap, f));
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return diffMap.toString();
    }

}
