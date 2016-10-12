package mobilecloud.objs;

import java.util.Map;

import mobilecloud.objs.field.FieldReader;
import mobilecloud.objs.field.FieldValue;
import mobilecloud.utils.ClassUtils;

/**
 * Object migrator is responsible to manager migrated objects in an application
 */
public class ObjectMigrator {
    
    private Token.Builder tokenBuilder = new Token.Builder();
    private Token localToken;
    private Token remoteToken;
    
    /**
     * Record an object as to be migrated
     * 
     * @param obj
     *            the object to migrate
     */
    public void migrate(Object obj) {
        if(obj != null) {
            tokenBuilder.addObject(obj);
        }
    }
    
    /**
     * Get a token of migrated objects
     * @return the token which contains information of moved out objects
     */
    public Token takeToken() {
        return this.localToken = tokenBuilder.build().expand();
    }
    
    /**
     * Synchronize moved out objects based on received token and diffs
     * @param remoteToken the token which contains objects to synchronize 
     * @param diffs the diff between local objects and remote objects
     */
    public void sync(Token remoteToken, Map<Integer, ObjDiff> diffs) {
        this.remoteToken = remoteToken;
        TokenFieldReader reader = new TokenFieldReader();
        for(int id: diffs.keySet()) {
            Object obj = getObjectById(id);
            ObjDiff diff = diffs.get(id);
            try {
                diff.apply(obj, reader);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }
    
    /**
     * Get the local copy of an remote object
     * @param obj the remote object
     * @return the local copy of this object
     */
    public Object getObject(Object obj) {
        if(obj == null || ClassUtils.isBasicType(obj.getClass())) {
            return obj;
        } else if(remoteToken != null && remoteToken.contains(obj)) {
            return getObjectById(remoteToken.getId(obj));
        } else if(localToken != null) {
            return getObjectById(localToken.getId(obj));
        } else {
            return null;
        }
    }
    
    /**
     * Get an object based on an id
     * @param id the id of an object
     * @return the object
     */
    private Object getObjectById(int id) {
        if(localToken == null || id < 0) {
            return null;
        } else if(id < localToken.size()) {
            return localToken.getObject(id);
        } else if(remoteToken != null) {
            return remoteToken.getObject(id);
        } else {
            return null;
        }
    }
    
    // A reader which reads objects from tokens
    private class TokenFieldReader implements FieldReader {

        @Override
        public Object read(FieldValue field) {
            if(field.isValue()) {
                return field.get();
            } else if(field.isObjectId()) {
                return getObjectById((Integer) field.get());
            } else if(field.isIdentityHashCode()) {
                throw new IllegalArgumentException(field.toString());
            }
            return null;
        }
        
    }
}
