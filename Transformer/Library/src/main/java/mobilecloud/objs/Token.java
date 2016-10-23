package mobilecloud.objs;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Objects;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import mobilecloud.lib.Ignore;
import mobilecloud.utils.ClassUtils;

/**
 * A token contains necessary information for migrated objects to synchronize with local objects.
 *  It can also provide snapshots on objects so that you can do dirty check
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 1L;
    private final TIntObjectMap<Object> objects;
    private transient TIntIntMap idMap;
    private int nextId;
    
    private Token(TIntObjectMap<Object> objects, TIntIntMap idMap, int nextId) {
        this.objects = objects;
        this.idMap = idMap;
        this.nextId = nextId;
    }
    
    private TIntIntMap getIdMap() {
        if(idMap == null) {
            idMap = new TIntIntHashMap();
            TIntIterator it = objects.keySet().iterator();
            while(it.hasNext()) {
                int id = it.next();
                idMap.put(System.identityHashCode(objects.get(id)), id);
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
                if (obj == null || ClassUtils.isBasicType(obj.getClass())) {
                    // ignore basic types
                    return false;
                }
                if(field != null) {
                    int modifier = field.getModifiers();
                    if (Modifier.isStatic(modifier) || field.isAnnotationPresent(Ignore.class)) {
                        // ignore static fields and ignored fields
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
        }).withObjects(objects.valueCollection());
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
     * Get a iterator of ids inside this map
     * @return the iterator of ids
     */
    public TIntIterator ids() {
        return objects.keySet().iterator();
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
        private final TIntObjectMap<ObjMap> fieldsOfObjects;
        
        private SnapShot(final Token token) {
            this.fieldsOfObjects = new TIntObjectHashMap<>();
            
            // Build the fields graph
            TIntIterator it = token.objects.keySet().iterator();
            while(it.hasNext()) {
                int id = it.next();
                Object obj = token.getObject(id);
                final ObjMap fields = new ObjMap(obj.getClass());
                fieldsOfObjects.put(id, fields);
                
                //Create a visitor to visit scan all fields of this object
                ObjectVisitor visitor = new ObjectVisitor(new OnObjectVisitedListener() {
                    @Override
                    public boolean onObjectVisited(Object obj, Object array, int index) {
                        if(obj != null) {
                            //For array, we use index as the key
                            if(ClassUtils.isBasicType(obj.getClass())) {
                                fields.putValue(index, obj);
                            } else if(token.contains(obj)) {
                                fields.putObjectId(index, token.getId(obj));
                            } else {
                                fields.putIdentityHashCode(index, System.identityHashCode(obj));
                            }
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean onObjectVisited(Object obj, Object from, Field field) {
                        int modifier = field.getModifiers();
                        if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier) || field.isAnnotationPresent(Ignore.class)) {
                            // ignore static, final, and ignored fields
                            // static and ignored fields cannot be migrated
                            // final fields cannot be changed, thus no need to
                            // add them to snapshot, which speeds up diff
                            // operation
                            return false;
                        }
                        if(obj != null) {
                            if(ClassUtils.isBasicType(obj.getClass())) {
                                fields.putValue(field, obj);
                            } else if(token.contains(obj)) {
                                fields.putObjectId(field, token.getId(obj));
                            } else {
                                fields.putIdentityHashCode(field, System.identityHashCode(obj));
                            }
                        }
                        return true;
                    }
                }).withObject(obj);
                visitor.visitFields();
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
        public TIntObjectMap<ObjDiff> diff(SnapShot snapshot) {
            TIntObjectMap<ObjDiff> res = new TIntObjectHashMap<>();
            TIntIterator it = fieldsOfObjects.keySet().iterator();
            while(it.hasNext()) {
                int id = it.next();
                ObjMap cur = fieldsOfObjects.get(id);
                ObjMap diff = diff(cur, snapshot == null? null: snapshot.getFields(id), cur.getRepresentType());
                if(!diff.isEmpty()) {
                    res.put(id, new ObjDiff(diff));
                }
            }
            return res;
        }
        
        //Diff which can transfer b -> a
        private ObjMap diff(ObjMap a, ObjMap b, Class<?> type) {
            ObjMap diff = new ObjMap(type);
            if(a != null) {
                TIntIterator it = a.keys();
                while(it.hasNext()) {
                    int index = it.next();
                    if(!equals(a, b, index)) {
                        copyEntry(diff, a, index);
                    }
                }
            }
            if(b != null) {
                TIntIterator it = b.keys();
                while(it.hasNext()) {
                    int index = it.next();
                    if(a == null || !a.containsKey(index)) {
                        diff.putValue(index, null);
                    }
                }
            }
            return diff;
        }
        
        private boolean equals(ObjMap a, ObjMap b, int index) {
            if(a == b) {
                return true;
            } else if (a == null || b == null){
                return false;
            } else if (a.isIdentityHashCode(index) && b.isIdentityHashCode(index)) {
                return a.getIdentityHashCode(index) == b.getIdentityHashCode(index);
            } else if (a.isObjectId(index) && b.isObjectId(index)) {
                return a.getObjectId(index) == b.getObjectId(index);
            } else if(a.isValue(index) && b.isValue(index)) {
                return Objects.equals(a.getValue(index), b.getValue(index));
            } else {
                return false;
            }
        }
        
        private void copyEntry(ObjMap target, ObjMap source, int index) {
            if(source.isIdentityHashCode(index)) {
                target.putIdentityHashCode(index, source.getIdentityHashCode(index));
            } else if(source.isObjectId(index)) {
                target.putObjectId(index, source.getObjectId(index));
            } else if(source.isValue(index)) {
                target.putValue(index, source.getValue(index));
            }
        }
        
        /**
         * Get fields of an object
         * @param i the id of this object
         * @return the fields, or null if snapshot does not contains i
         */
        public ObjMap getFields(int i) {
            return fieldsOfObjects.get(i);
        }
        
        /**
         * Get an iterator of ids inside this snapshots
         * @return the iterator ids of objects
         */
        public TIntIterator ids() {
            return fieldsOfObjects.keySet().iterator();
        }
        
        /**
         * Size of snapshot
         * @return size
         */
        public int size() {
            return fieldsOfObjects.size();
        }
        
        /**
         * Check if an id is inside this snapshot
         * @param id the id
         * @return true if the id is inside this snapshot
         */
        public boolean contains(int id) {
            return fieldsOfObjects.containsKey(id);
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
        private TIntObjectMap<Object> objects;
        private TIntIntMap idMap;
        private int nextId = 0;
        
        public Builder() {
            this.objects = new TIntObjectHashMap<>();
            this.idMap = new TIntIntHashMap();
        }
        
        /**
         * Create a new builder which contains all objects inside a given token
         * @param token the token
         */
        public Builder(Token token) {
            this.objects = new TIntObjectHashMap<>(token.objects);
            this.idMap = new TIntIntHashMap(token.idMap);
            this.nextId = token.nextId;
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
