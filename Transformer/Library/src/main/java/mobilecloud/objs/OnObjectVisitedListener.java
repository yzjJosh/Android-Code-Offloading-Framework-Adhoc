package mobilecloud.objs;

import java.lang.reflect.Field;

/**
 * A listener which is called when an object is visited
 *
 */
public interface OnObjectVisitedListener {

    /**
     * Called when an object is source or is visited via a field
     * 
     * @param obj
     *            the object that is visited
     * 
     * @param from
     *            the object where this object is pointed from, null if this
     *            object is the source
     * @param field
     *            the field where this object is associated with, or null if
     *            this object is the source
     * @return should this object's sub-graph be searched
     */
    public boolean onObjectVisited(Object obj, Object from, Field field);
    
    /**
     * Called when an object is visited via an array
     * 
     * @param obj
     *            the object that is visited
     * @param array
     *            the array where this object is inside
     * @param index
     *            the index of this object inside array
     * @return should this object's sub-graph be searched
     */
    public boolean onObjectVisited(Object obj, Object array, int index);
    
}
