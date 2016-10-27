package mobilecloud.objs;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Object visitor can do a dfs on object graph
 *
 */
public class ObjectVisitor {
    
    private List<Object> objList = new LinkedList<>();
    private final OnObjectVisitedListener listener;
    
    public ObjectVisitor(OnObjectVisitedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Add object to this visitor
     * @param obj the object
     * @return this visitor
     */
    public ObjectVisitor withObject(Object obj) {
        objList.add(obj);
        return this;
    }
    
    /**
     * Add objects to this visitor
     * @param objs the objects
     * @return this visitor
     */
    public ObjectVisitor withObjects(Collection<Object> objs) {
        objList.addAll(objs);
        return this;
    }
    
    /**
     * Start visiting objects recusively
     */
    public void visitRecursively() {
        Set<Integer> visited = new HashSet<>();
        for(Object obj: objList) {
            visit(obj, visited, null, null, -1);
        }
    }
    
    private void visit(Object obj, Set<Integer> visited, Object from, Field field, int index) {
        if(obj == null) {
            return;
        }
        if(!visited.add(System.identityHashCode(obj))) {
            return;
        }
        if(index == -1) {
            if(!listener.onObjectVisited(obj, from, field)) {
                return;
            }
        } else {
            if(!listener.onObjectVisited(obj, from, index)) {
                return;
            }
        }
        Class<?> clazz = obj.getClass();
        if(clazz.isArray()) {
            int len = Array.getLength(obj);
            for(int i=0; i<len; i++) {
                visit(Array.get(obj, i), visited, obj, null, i);
            }
        } else {
            while(clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    try {
                        visit(f.get(obj), visited, obj, f, -1);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }
    
    /**
     * Visit fields of objects. Only 1 level.
     */
    public void visitFields() {
        for(Object obj: objList) {
            if(obj == null) {
                continue;
            }
            Class<?> clazz = obj.getClass();
            if(clazz.isArray()) {
                int len = Array.getLength(obj);
                for(int i=0; i<len; i++) {
                    listener.onObjectVisited(Array.get(obj, i), obj, i);
                }
            } else {
                while(clazz != null && clazz != Object.class) {
                    for (Field f : clazz.getDeclaredFields()) {
                        f.setAccessible(true);
                        try {
                            listener.onObjectVisited(f.get(obj), obj, f);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
        }
    }
}
