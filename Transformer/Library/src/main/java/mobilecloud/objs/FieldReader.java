package mobilecloud.objs;

import java.lang.reflect.Field;

/**
 * Field reader is a reader that can read actual value from ObjMap
 */
public interface FieldReader {
    
    /**
     * Get the actual value from ObjMap based on field
     * @param map the map to read
     * @param f the field to get
     * @return the actual value of that field
     */
    public Object read(ObjMap map, Field f);

    /**
     * Get the actual value from ObjMap based on index
     * @param map the map to read
     * @param index the index to get
     * @return the actual value on that index
     */
    public Object read(ObjMap map, int index);
    
}
