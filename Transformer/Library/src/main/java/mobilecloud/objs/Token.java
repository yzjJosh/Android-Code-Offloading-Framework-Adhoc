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

import mobilecloud.utils.ClassUtils;

/**
 * A token contains necessary information for migrated objects to synchronize with local objects.
 *  It can also provide snapshots on objects so that you can do dirty check
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Integer, Object> objects;
    private transient Map<Integer, Integer> idMap;
    
    private Token(Map<Integer, Object> objects, Map<Integer, Integer> idMap) {
        this.objects = objects;
        this.idMap = idMap;
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
     * @return the expanded token
     */
    public Token expand() {
        final Builder builder = new Builder(this);
        ObjectVisitor visitor = new ObjectVisitor(new OnObjectVisitedListener() {
            @Override
            public boolean onObjectVisited(Object obj, Object from, Field field) {
                if(obj == null || ClassUtils.isBasicType(obj.getClass())) {
                    return false;
                }
                if(field != null) {
                    int modifier = field.getModifiers();
                    if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier)) {
                        // ignore static and transient fields
                        return false;
                    }
                }
                if(!contains(obj)) {
                    builder.addObject(obj);
                }
                return true;
            }

            @Override
            public boolean onObjectVisited(Object obj, Object array, int index) {
                if(obj == null || ClassUtils.isBasicType(obj.getClass())) {
                    return false;
                }
                if(!contains(obj)) {
                    builder.addObject(obj);
                }
                return true;
            }
        }).withObjects(objects.values());
        visitor.visitRecursively();
        return builder.build();
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
        return getIdMap().get(System.identityHashCode(obj));
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
        private final Map<Integer, Map<String, FieldValue>> fieldsOfObjects;
        
        private SnapShot(final Token token) {
            this.fieldsOfObjects = new HashMap<>();
            
            // Build the fields graph
            for(int id: token.objects.keySet()) {
                final HashMap<String, FieldValue> fields = new HashMap<>();
                fieldsOfObjects.put(id, fields);
                Object obj = token.getObject(id);
                ObjectVisitor visitor = new ObjectVisitor(new OnObjectVisitedListener() {
                    @Override
                    public boolean onObjectVisited(Object obj, Object array, int index) {
                        if(obj != null) {
                            addField(fields, token, String.valueOf(index), obj);
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean onObjectVisited(Object obj, Object from, Field field) {
                        int modifier = field.getModifiers();
                        if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier) || Modifier.isFinal(modifier)) {
                            // ignore static, transient and final fields
                            // static and transient cannot be migrated
                            // final fields cannot be changed, thus no need to
                            // add them to snapshot, which speeds up diff
                            // operation
                            return false;
                        }
                        if(obj != null) {
                            addField(fields, token, field.getName(), obj);
                        }
                        return true;
                    }
                }).withObject(obj);
                visitor.visitFields();
            }
        }
        
        private void addField(Map<String, FieldValue> fields, Token token, String name, Object val) {
            if(ClassUtils.isBasicType(val.getClass())) {
                // val is basic type, record its value directly
                fields.put(name, FieldValue.newValue(val));
            } else if(token.contains(val)) {
                // id is available, store is an id
                fields.put(name, FieldValue.newObjectId(token.getId(val)));
            } else {
                // id is unavailable, store the identity hash code
                fields.put(name, FieldValue.newIdentityHashCode(System.identityHashCode(val)));
            }
        }
        
        /**
         * Get the diff between this snapshot and a given snap shot. The diff indicates how to transfer objects in given snapshot to objects in current snapshot 
         * @param snapshot the given snapshot. Which should have the same id mapping as current snapshot
         * @return the diffs, where key is object id, value is difference
         */
        public Map<Integer, ObjDiff> diff(SnapShot snapshot) {
            Map<Integer, ObjDiff> res = new HashMap<>();
            for(int id: fieldsOfObjects.keySet()) {
                Map<String, FieldValue> diff = diff(this.getFields(id), snapshot == null? null: snapshot.getFields(id));
                if(!diff.isEmpty()) {
                    res.put(id, new ObjDiff(diff));
                }
            }
            if(snapshot != null) {
                for(int id: snapshot.fieldsOfObjects.keySet()) {
                    if(getFields(id) == null) {
                        Map<String, FieldValue> diff = diff(null, snapshot.getFields(id));
                        res.put(id, new ObjDiff(diff));
                    }
                }
            }
            return res;
        }
        
        //Diff which can transfer b -> a
        private Map<String, FieldValue> diff(Map<String, FieldValue> a, Map<String, FieldValue> b) {
            Map<String, FieldValue> diff = new HashMap<>();
            if(a != null) {
                for(String fieldName: a.keySet()) {
                    FieldValue aVal = get(a, fieldName);
                    FieldValue bVal = get(b, fieldName);
                    if(!Objects.equals(aVal, bVal)) {
                        diff.put(fieldName, aVal);
                    }
                }
            }
            if(b != null) {
                for(String fieldName: b.keySet()) {
                    if(get(a, fieldName) == null) {
                        diff.put(fieldName, FieldValue.newValue(null));
                    }
                }
            }
            return diff;
        }
        
        private FieldValue get(Map<String, FieldValue> map, String key) {
            return map == null? null: map.get(key);
        }
        
        /**
         * Get fields of an object
         * @param i the id of this object
         * @return the fields, or null if snapshot does not contains i
         */
        public Map<String, FieldValue> getFields(int i) {
            return fieldsOfObjects.get(i);
        }
        
        /**
         * Get the field value of an object
         * @param i the id of this object
         * @param fieldName the name of a field
         * @return the field value, or null if the field does not exist or is a null pointer or this snapshot does not contains i
         */
        public FieldValue getField(int i, String fieldName) {
            Map<String, FieldValue> fields = getFields(i);
            if(fields == null) {
                return null;
            } else {
                return fields.get(fieldName);
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
            if(obj != null) {
                idMap.put(System.identityHashCode(obj), nextId);
                objects.put(nextId, obj);
            }
            nextId ++;
            return this;
        }
        
        public <T> Builder addObjects(Collection<T> objs) {
            for(T obj: objs) {
                addObject(obj);
            }
            return this;
        }
        
        public Builder addObject(int id, Object obj) {
            if(objects.containsKey(id)) {
                idMap.remove(System.identityHashCode(objects.remove(id)));
            }
            objects.put(id, obj);
            idMap.put(System.identityHashCode(obj), id);
            nextId = Math.max(nextId, id+1);
            return this;
        }
        
        public Token build() {
            return new Token(objects, idMap);
        }
    }
}
