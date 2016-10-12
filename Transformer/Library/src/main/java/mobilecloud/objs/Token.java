package mobilecloud.objs;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mobilecloud.objs.field.FieldValue;
import mobilecloud.utils.ClassUtils;

/**
 * A token contains necessary information for migrated objects to synchronize with local objects.
 *  It can also provide snapshots on objects so that you can do dirty check
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Integer, Object> objects;
    private transient Map<Integer, Integer> idMap;
    private int nextId;
    
    private Token(Map<Integer, Object> objects, Map<Integer, Integer> idMap, int nextId) {
        this.objects = objects;
        this.idMap = idMap;
        this.nextId = nextId;
    }
    
    private Map<Integer, Integer> getIdMap() {
        if(idMap == null) {
            idMap = new HashMap<>();
            for(Map.Entry<Integer, Object> entry: objects.entrySet()) {
                idMap.put(System.identityHashCode(entry.getValue()), entry.getKey());
            }
        }
        return idMap;
    }
    
    /**
     * Take a snapshot for this token
     * @return the current snapshot
     */
    public SnapShot takeSnapShot() {
        return new SnapShot(this);
    }
    
    /**
     * Expand this token, add all connected objects into the token's object list
     * @return this token
     */
    public Token expand() {
        ObjectVisitor visitor = new ObjectVisitor(new OnObjectVisitedListener() {
            @Override
            public boolean onObjectVisited(Object obj, Object from, Field field) {
                if(obj == null || ClassUtils.isBasicType(obj.getClass())) {
                    // ignore basic types
                    return false;
                }
                if(field != null) {
                    int modifier = field.getModifiers();
                    if (Modifier.isStatic(modifier)) {
                        // ignore static fields
                        return false;
                    }
                }
                if(!contains(obj)) {
                    addObject(obj);
                }
                // If this is a primitive array, there is no need to explore it
                // because its elements must be basic types
                return !ClassUtils.isPrimitiveArray(obj.getClass());
            }

            @Override
            public boolean onObjectVisited(Object obj, Object array, int index) {
                if(obj == null || ClassUtils.isBasicType(obj.getClass())) {
                    return false;
                }
                if(!contains(obj)) {
                    addObject(obj);
                }
                return !ClassUtils.isPrimitiveArray(obj.getClass());
            }
        }).withObjects(objects.values());
        visitor.visitRecursively();
        return this;
    }
    
    // Add an object to this token
    private void addObject(Object obj) {
        objects.put(nextId, obj);
        if(idMap != null) {
            idMap.put(System.identityHashCode(obj), nextId);
        }
        nextId ++;
    }
    
    /**
     * Get an object from its id
     * @param id the id
     * @return the object, or null if it does not exist
     */
    public Object getObject(int id) {
        return objects.get(id);
    }
    
    /**
     * Check if an object is inside this token
     * @param obj the object
     * @return if the object exists
     */
    public boolean contains(Object obj) {
        return getIdMap().containsKey(System.identityHashCode(obj));
    }
    
    /**
     * Get the id of an object inside this token
     * @param obj the object
     * @return the id
     */
    public int getId(Object obj) {
        int hashCode = System.identityHashCode(obj);
        return getIdMap().containsKey(hashCode)? getIdMap().get(hashCode): -1;
    }
    
    /**
     * Get a list of ids inside this map
     * @return the ids
     */
    public List<Integer> ids() {
        return new LinkedList<>(objects.keySet());
    }
    
    /**
     * Size of token
     * @return size
     */
    public int size() {
        return objects.size();
    }
    
    public static class SnapShot implements Serializable {
        private static final long serialVersionUID = 1L;

        // Stores the graph of objects inside this token
        private final Map<Integer, Map<Object, FieldValue>> fieldsOfObjects;
        
        private SnapShot(final Token token) {
            this.fieldsOfObjects = new HashMap<>();
            
            // Build the fields graph
            for(int id: token.objects.keySet()) {
                final HashMap<Object, FieldValue> fields = new HashMap<>();
                fieldsOfObjects.put(id, fields);
                Object obj = token.getObject(id);
                
                //Create a visitor to visit scan all fields of this object
                ObjectVisitor visitor = new ObjectVisitor(new OnObjectVisitedListener() {
                    @Override
                    public boolean onObjectVisited(Object obj, Object array, int index) {
                        if(obj != null) {
                            //For array, we use index as the key
                            fields.put(index, createField(token, obj));
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean onObjectVisited(Object obj, Object from, Field field) {
                        int modifier = field.getModifiers();
                        if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) {
                            // ignore static and final fields
                            // static and fields cannot be migrated
                            // final fields cannot be changed, thus no need to
                            // add them to snapshot, which speeds up diff
                            // operation
                            return false;
                        }
                        if(obj != null) {
                            fields.put(field.getName(),createField(token, obj));
                        }
                        return true;
                    }
                }).withObject(obj);
                visitor.visitFields();
            }
        }
        
        private FieldValue createField(Token token, Object val) {
            if(ClassUtils.isBasicType(val.getClass())) {
                // val is basic type, record its value directly
                return FieldValue.newValue(val);
            } else if(token.contains(val)) {
                // id is available, store is an id
                return FieldValue.newObjectId(token.getId(val));
            } else {
                // id is unavailable, store the identity hash code
                return FieldValue.newIdentityHashCode(System.identityHashCode(val));
            }
        }
        
        /**
         * Get the diff between this snapshot and a given snapshot. The diff
         * indicates how to transfer objects in another snapshot to objects in
         * this snapshot
         * 
         * @param snapshot
         *            the given snapshot. Which should be subset of current
         *            snapshot's objects. If given snapshot is not a subset of
         *            current snapshot, only the common parts and new parts in
         *            this snapshot will be analyzed.
         * @return the diffs, where key is object id, value is difference
         */
        public Map<Integer, ObjDiff> diff(SnapShot snapshot) {
            Map<Integer, ObjDiff> res = new HashMap<>();
            for(int id: fieldsOfObjects.keySet()) {
                Map<Object, FieldValue> diff = diff(this.getFields(id), snapshot == null? null: snapshot.getFields(id));
                if(!diff.isEmpty()) {
                    res.put(id, new ObjDiff(diff));
                }
            }
            return res;
        }
        
        //Diff which can transfer b -> a
        private Map<Object, FieldValue> diff(Map<Object, FieldValue> a, Map<Object, FieldValue> b) {
            Map<Object, FieldValue> diff = new HashMap<>();
            if(a != null) {
                for(Object field: a.keySet()) {
                    FieldValue aVal = get(a, field);
                    FieldValue bVal = get(b, field);
                    if(!Objects.equals(aVal, bVal)) {
                        diff.put(field, aVal);
                    }
                }
            }
            if(b != null) {
                for(Object field: b.keySet()) {
                    if(get(a, field) == null) {
                        diff.put(field, FieldValue.newValue(null));
                    }
                }
            }
            return diff;
        }
        
        private FieldValue get(Map<Object, FieldValue> map, Object key) {
            return map == null? null: map.get(key);
        }
        
        /**
         * Get fields of an object
         * @param i the id of this object
         * @return the fields, or null if snapshot does not contains i
         */
        public Map<Object, FieldValue> getFields(int i) {
            return fieldsOfObjects.get(i);
        }
        
        /**
         * Get the field value of an object
         * @param i the id of this object
         * @param field the name of a field or index of an array
         * @return the field value, or null if the field does not exist or is a null pointer or this snapshot does not contains i
         */
        public FieldValue getField(int i, Object field) {
            Map<Object, FieldValue> fields = getFields(i);
            if(fields == null) {
                return null;
            } else {
                return fields.get(field);
            }
        }
        
        /**
         * Get a list of ids of objects
         * @return the ids of objects
         */
        public List<Integer> ids() {
            return new LinkedList<>(fieldsOfObjects.keySet());
        }
        
        /**
         * Size of snapshot
         * @return size
         */
        public int size() {
            return fieldsOfObjects.size();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != SnapShot.class) {
                return false;
            } else {
                SnapShot that = (SnapShot) o;
                return this.fieldsOfObjects.equals(that.fieldsOfObjects);
            }
        }
        
        @Override
        public String toString() {
            return fieldsOfObjects.toString();
        }
        
    }   
    
    public static class Builder {
        private Map<Integer, Object> objects = new HashMap<>();
        private Map<Integer, Integer> idMap = new HashMap<>();
        private int nextId = 0;
        
        public Builder() {}
        
        /**
         * Create a new builder which contains all objects inside a given token
         * @param token the token
         */
        public Builder(Token token) {
            this.objects = new HashMap<>(token.objects);
            this.idMap = new HashMap<>(token.idMap);
            if(!objects.isEmpty()) {
                this.nextId = Collections.max(objects.keySet()) + 1;
            }
        }
        
        public Builder addObject(Object obj) {
            if(obj != null && !ClassUtils.isBasicType(obj.getClass())) {
                idMap.put(System.identityHashCode(obj), nextId);
                objects.put(nextId, obj);
                nextId ++;
            }
            return this;
        }
        
        public <T> Builder addObjects(Collection<T> objs) {
            for(T obj: objs) {
                addObject(obj);
            }
            return this;
        }
        
        public Builder addObject(int id, Object obj) {
            if(obj != null && !ClassUtils.isBasicType(obj.getClass())) {
                if(objects.containsKey(id)) {
                    idMap.remove(System.identityHashCode(objects.remove(id)));
                }
                objects.put(id, obj);
                idMap.put(System.identityHashCode(obj), id);
                nextId = Math.max(nextId, id+1);
            }
            return this;
        }
        
        public Token build() {
            return new Token(objects, idMap, nextId);
        }
    }
}
