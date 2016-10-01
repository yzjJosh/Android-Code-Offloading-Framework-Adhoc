package mobilecloud.engine;

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
     * A wrapper of moveOut(Remotable obj) function. If obj is not remotable, do nothing
     * @param obj the object to send out
     */
    public void moveOut(Object obj) {
        if(obj instanceof Remotable) {
            moveOut((Remotable) obj);
        }
    }
    
    /**
     * Mark an object as a remote object
     * @param obj the object to send out
     */
    public void moveOut(Remotable obj) {
        if(obj == null || obj.isOnServer()) {
            return;
        }
        obj.setIsNew(false);
        obj.setIsOnServer(true);
        obj.setId(System.identityHashCode(obj));
        remoteObjs.put(obj.getId(), obj);
        migratedObjects.add(obj.getId());
        //Scan fields and keep marking them as in the server
        for(Field f: obj.getClass().getDeclaredFields()) {
            int modifier = f.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier)) {
                // If this field is static or transient, we can ignore it
                // because it cannot be migrate to server
                continue;
            }
            f.setAccessible(true);
            Object val = null;
            try {
                val = f.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if(val == null || !(val instanceof Remotable)) {
                // Value is not remotable, unnecessary to mark it
                continue;
            }
            moveOut((Remotable) val);
        }
    }
    
    /**
     * A wrapper of sync(Remotable remoteObj) function. If remoteObj is not remotable, do nothing
     * @param obj the object to synchronize
     * @return the local version of the synchronized object
     */
    public Object sync(Object obj) {
        if (obj instanceof Remotable) {
            return sync((Remotable) obj);
        } else {
            return obj;
        }
    }
    
    /**
     * Synchronize corresponding local copies of a remote object
     * @param obj the object to sync
     * @return The local version of the synchronized object
     */
    public Remotable sync(Remotable remoteObj) {
        return sync(remoteObj, new HashSet<Integer>());
    }

    private Remotable sync(Remotable obj, Set<Integer> visited) {
        if(obj == null) {
            return obj;
        }
        Remotable local = null;
        if (obj.isNew()) {
            if (newObjects.containsKey(obj.getId())) {
                local = newObjects.get(obj.getId());
            } else {
                local = obj;
                newObjects.put(local.getId(), local);
            }
        } else {
            local = remoteObjs.get(obj.getId());
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
                val = f.get(obj);
                if (val == null || !(val instanceof Remotable)) {
                    // value is not remotable, which means we do not have meta
                    // data of this object. We simply copy it.
                    f.set(local, val);
                } else {
                    // the value of this field is remotable, we update it recursively
                    f.set(local, sync((Remotable)val, visited));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return local;
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
        res.append("===============================================");
        res.append("Object Migrator id: " + System.identityHashCode(this));
        res.append("Migrated Object Number: " + migratedObjects.size());
        res.append("Migrated Objects: " + migratedObjects);
        res.append("New Object Number: " + newObjects.size());
        res.append("New Objects: " + newObjects.keySet());
        res.append("===============================================");
        return res.toString();
    }

    /**
     * Get a string representation of global meta data
     * @return the string representation
     */
    public static String dumpGlobalMetaData() {
        StringBuilder res = new StringBuilder();
        res.append("===============================================");
        res.append("Object Migrator Global Information");
        res.append("Migrated Object Number: " + remoteObjs.size());
        res.append("Migrated Objects: " + remoteObjs.keySet());
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
