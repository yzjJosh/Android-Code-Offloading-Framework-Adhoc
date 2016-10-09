package mobilecloud.engine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mobilecloud.lib.Remotable;

/**
 * Object migrator is responsible to manager migrated objects in an application
 */
public class ObjectMigrator {

    private static ConcurrentHashMap<Integer, Remotable> remoteObjs = new ConcurrentHashMap<>();
    
    private List<Integer> migratedObjects = new LinkedList<>();
    private Map<Integer, Remotable> newObjects = new HashMap<>();
    
    /**
     * Mark an object as a remote object
     * 
     * @param obj
     *            the object to send out
     */
    public void moveOut(Object obj) {
        if(obj == null) {
            return;
        } else if(obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                moveOut(Array.get(obj, i));
            }
        } else if (obj instanceof Remotable) {
            Remotable r = (Remotable) obj;
            if(r.isOnServer()) {
                return;
            }
            r.setIsNew(false);
            r.setIsOnServer(true);
            r.setId(System.identityHashCode(r));
            remoteObjs.put(r.getId(), r);
            migratedObjects.add(r.getId());
            //Scan fields and keep marking them as in the server
            for(Field f: r.getClass().getDeclaredFields()) {
                int modifier = f.getModifiers();
                if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier)) {
                    // If this field is static or transient, we can ignore it
                    // because it cannot be migrate to server
                    continue;
                }
                f.setAccessible(true);
                Object val = null;
                try {
                    val = f.get(r);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                moveOut(val);
            }
        } 
    }
    
    
    /**
     * Synchronize corresponding local copies of a remote object
     * @param obj the object to sync
     * @return The local version of the synchronized object
     */
    public Object sync(Object obj) {
        if(obj == null || (!(obj instanceof Remotable) && !obj.getClass().isArray())) {
            return obj;
        } else {
            return sync(obj, new HashSet<Integer>());
        }
    }
    

    private Object sync(Object obj, Set<Integer> visited) {
        if (obj == null) {
            return obj;
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object item = sync(Array.get(obj, i), visited);
                Array.set(obj, i, item);
            }
            return obj;
        } else if(obj instanceof Remotable) {
            Remotable r = (Remotable) obj;
            Remotable local = null;
            if (r.isNew()) {
                if (newObjects.containsKey(r.getId())) {
                    local = newObjects.get(r.getId());
                } else {
                    local = r;
                    newObjects.put(local.getId(), local);
                }
            } else {
                local = remoteObjs.get(r.getId());
            }
            if(!visited.add(local.getId())) {
                return local;
            }
            for(Field f: local.getClass().getDeclaredFields()) {
                int modifier = f.getModifiers();
                if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier)) {
                    // If this field is static or transient, we can ignore it
                    // because it cannot be migrate to server
                    continue;
                }
                f.setAccessible(true);
                Object val = null;
                try {
                    val = f.get(r);
                    // the value of this field is remotable, we update it recursively
                    f.set(local, sync(val, visited));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return local;
        } else {
            return obj;
        }
    }
    
    /**
     * Join remote objects together with local ones. This method will do 2 things:
     * 1. Mark all old objects (migrated from this env) as local
     * 2. Mark all new objects (created by cloud) as local
     */
    public void joinObjects() {
        for(int oldObj: migratedObjects) {
            if(remoteObjs.containsKey(oldObj)) {
                Remotable r = remoteObjs.remove(oldObj);
                r.setIsNew(false);
                r.setIsOnServer(false);
            }
        }
        for(Remotable newObj: newObjects.values()) {
            newObj.setIsNew(false);
            newObj.setIsOnServer(false);
        }
    }
    
    /**
     * Get a string representation of local meta data
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("===============================================\n");
        res.append("Object Migrator id: " + System.identityHashCode(this) + "\n");
        res.append("Migrated Object Number: " + migratedObjects.size() + "\n");
        res.append("Migrated Objects: " + migratedObjects + "\n");
        res.append("New Object Number: " + newObjects.size() + "\n");
        res.append("New Objects: " + newObjects.keySet() + "\n");
        res.append("===============================================");
        return res.toString();
    }

    /**
     * Get a string representation of global meta data
     * @return the string representation
     */
    public static String dumpGlobalMetaData() {
        StringBuilder res = new StringBuilder();
        res.append("===============================================\n");
        res.append("Object Migrator Global Information\n");
        res.append("Migrated Object Number: " + remoteObjs.size() + "\n");
        res.append("Migrated Objects: " + remoteObjs.keySet() + "\n");
        res.append("===============================================");
        return res.toString();
    }
    
    /**
     * Clear global meta data, used for testing purpose. Caution: Application
     * should never call this method. It may cause crashing!
     */
    public static void purgeGlobalMetaData() {
        remoteObjs.clear();
    }
    
}
