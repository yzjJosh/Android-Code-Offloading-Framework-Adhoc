package mobilecloud.objs;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.map.hash.TIntCharHashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TIntShortHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import mobilecloud.utils.ClassUtils;

public class ObjMap implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static Map<Class<?>, TObjectIntMap<Field>> clazzFieldIdMap = new ConcurrentHashMap<>();

    private TIntIntMap intMap;
    private TIntObjectMap<Object> objMap;
    private Object primitiveArrayMap;
    private final Class<?> clazz;
    
    public ObjMap(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    public boolean isObjectId(int index) {
        return intMap != null && intMap.containsKey(index) && intMap.get(index) < 0;
    }
    
    public boolean isObjectId(Field f) {
        return isObjectId(fieldId(f));
    }
    
    public boolean isIdentityHashCode(int index) {
        return intMap != null && intMap.containsKey(index) && intMap.get(index) >= 0;
    }
    
    public boolean isIdentityHashCode(Field f) {
        return isIdentityHashCode(fieldId(f));
    }
    
    public boolean isValue(int index) {
        if(ClassUtils.isPrimitiveArray(clazz)) {
            return primitiveArrayMapContainsKey(index);
        } else {
            return objMap != null && objMap.containsKey(index);
        }
    }
    
    public boolean isValue(Field f) {
        return isValue(fieldId(f));
    }
    
    public boolean containsKey(int index) {
        if (ClassUtils.isPrimitiveArray(clazz)) {
            return primitiveArrayMapContainsKey(index);
        } else {
            return (intMap != null && intMap.containsKey(index)) || (objMap != null && objMap.containsKey(index));
        }
    }
    
    public boolean containsKey(Field f) {
        return containsKey(fieldId(f));
    }
    
    public void putObjectId(int index, int id) {
        remove(index);
        intMap().put(index, -id-1);
    }
    
    public void putObjectId(Field f, int id) {
        putObjectId(fieldId(f), id);
    }
    
    public void putIdentityHashCode(int index, int hashCode) {
        remove(index);
        intMap().put(index, hashCode);
    }
    
    public void putIdentityHashCode(Field f, int hashCode) {
        putIdentityHashCode(fieldId(f), hashCode);
    }
    
    public void putValue(int index, Object val) {
        remove(index);
        if(ClassUtils.isPrimitiveArray(clazz)) {
            primitiveArrayMapPut(index, val);
        } else {
            objMap().put(index, val);
        }
    }
    
    public void putValue(Field f, Object val) {
        putValue(fieldId(f), val);
    }
    
    public int getObjectId(int index) {
        return - (intMap.get(index) + 1);
    }
    
    public int getObjectId(Field f) {
        return getObjectId(fieldId(f));
    }
    
    public int getIdentityHashCode(int index) {
        return intMap.get(index);
    }
    
    public int getIdentityHashCode(Field f) {
        return getIdentityHashCode(fieldId(f));
    }
    
    public Object getValue(int index) {
        if(ClassUtils.isPrimitiveArray(clazz)) {
            return primitiveArrayMapGet(index);
        } else {
            return objMap.get(index);
        }
    }
    
    public Object getValue(Field f) {
        return getValue(fieldId(f));
    }
    
    public void remove(int index) {
        if(ClassUtils.isPrimitiveArray(clazz)) {
            primitiveArrayMapRemove(index);
        } else {
            if(intMap != null && intMap.containsKey(index)) {
                intMap.remove(index);
            } else if(objMap != null && objMap.containsKey(index)) {
                objMap.remove(index);
            }
        }
    }
    
    public void remove(Field f) {
        remove(fieldId(f));
    }
    
    public TIntIterator keys() {
        return new KeyIterator();
    }
    
    public int size() {
        return (intMap == null ? 0 : intMap.size()) + (objMap == null ? 0 : objMap.size()) + primitiveArrayMapSize();
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public Class<?> getRepresentType() {
        return clazz;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        TIntIterator it = keys();
        int i = 0;
        while(it.hasNext()) {
            int index = it.next();
            if(i > 0) {
                sb.append(", ");
            }
            if(isObjectId(index)) {
                sb.append(index + "=" + getObjectId(index));
            } else if(isIdentityHashCode(index)) {
                sb.append(index + "=" + getIdentityHashCode(index));
            } else if(isValue(index)) {
                sb.append(index + "=" + getValue(index));
            }
            i ++;
        }
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != ObjMap.class) {
            return false;
        } else {
            ObjMap that = (ObjMap) o;
            return Objects.equals(this.intMap, that.intMap) && Objects.equals(this.objMap, that.objMap)
                    && Objects.equals(this.primitiveArrayMap, that.primitiveArrayMap);
        }
    }
    
    private TIntIntMap intMap() {
        if(intMap == null) {
            intMap = new TIntIntHashMap();
        }
        return intMap;
    }
    
    private TIntObjectMap<Object> objMap() {
        if(objMap == null) {
            objMap = new TIntObjectHashMap<>();
        }
        return objMap;
    }
    
    private Object primitiveArrayMap() {
        if (primitiveArrayMap == null) {
            Class<?> type = clazz.getComponentType();
            if (type == int.class) {
                primitiveArrayMap = new TIntIntHashMap();
            } else if (type == short.class) {
                primitiveArrayMap = new TIntShortHashMap();
            } else if (type == long.class) {
                primitiveArrayMap = new TIntLongHashMap();
            } else if (type == byte.class) {
                primitiveArrayMap = new TIntByteHashMap();
            } else if (type == char.class) {
                primitiveArrayMap = new TIntCharHashMap();
            } else if (type == boolean.class) {
                primitiveArrayMap = new TIntByteHashMap();
            } else if (type == float.class) {
                primitiveArrayMap = new TIntFloatHashMap();
            } else if (type == double.class) {
                primitiveArrayMap = new TIntDoubleHashMap();
            }
        }
        return primitiveArrayMap;
    }
    
    private boolean primitiveArrayMapContainsKey(int index) {
        if(primitiveArrayMap == null) {
            return false;
        }
        Class<?> type = clazz.getComponentType();
        if (type == int.class) {
            return ((TIntIntHashMap) primitiveArrayMap).contains(index);
        } else if (type == short.class) {
            return ((TIntShortHashMap) primitiveArrayMap).contains(index);
        } else if (type == long.class) {
            return ((TIntLongHashMap) primitiveArrayMap).contains(index); 
        } else if (type == byte.class) {
            return ((TIntByteHashMap) primitiveArrayMap).contains(index);
        } else if (type == char.class) {
            return ((TIntCharHashMap) primitiveArrayMap).contains(index);
        } else if (type == boolean.class) {
            return ((TIntByteHashMap) primitiveArrayMap).contains(index);
        } else if (type == float.class) {
            return ((TIntFloatHashMap) primitiveArrayMap).contains(index);
        } else if (type == double.class) {
            return ((TIntDoubleHashMap) primitiveArrayMap).contains(index);
        } else {
            return false;
        }
    }
    
    private Object primitiveArrayMapGet(int index) {
        Class<?> type = clazz.getComponentType();
        if(primitiveArrayMap == null) {
            return null;
        }
        if (type == int.class) {
            return ((TIntIntHashMap) primitiveArrayMap).get(index);
        } else if (type == short.class) {
            return ((TIntShortHashMap) primitiveArrayMap).get(index);
        } else if (type == long.class) {
            return ((TIntLongHashMap) primitiveArrayMap).get(index); 
        } else if (type == byte.class) {
            return ((TIntByteHashMap) primitiveArrayMap).get(index);
        } else if (type == char.class) {
            return ((TIntCharHashMap) primitiveArrayMap).get(index);
        } else if (type == boolean.class) {
            return ((TIntByteHashMap) primitiveArrayMap).get(index) != 0;
        } else if (type == float.class) {
            return ((TIntFloatHashMap) primitiveArrayMap).get(index);
        } else if (type == double.class) {
            return ((TIntDoubleHashMap) primitiveArrayMap).get(index);
        } else {
            return null;
        }
    }
    
    private void primitiveArrayMapPut(int index, Object val) {
        Class<?> type = clazz.getComponentType();
        if (type == int.class) {
            ((TIntIntHashMap) primitiveArrayMap()).put(index, (Integer) val);
        } else if (type == short.class) {
            ((TIntShortHashMap) primitiveArrayMap()).put(index, (Short) val);
        } else if (type == long.class) {
            ((TIntLongHashMap) primitiveArrayMap()).put(index, (Long) val); 
        } else if (type == byte.class) {
            ((TIntByteHashMap) primitiveArrayMap()).put(index, (Byte) val);
        } else if (type == char.class) {
            ((TIntCharHashMap) primitiveArrayMap()).put(index, (Character) val);
        } else if (type == boolean.class) {
            ((TIntByteHashMap) primitiveArrayMap()).put(index, (byte) (((Boolean) val) ? 1: 0));
        } else if (type == float.class) {
            ((TIntFloatHashMap) primitiveArrayMap()).put(index, (Float) val);
        } else if (type == double.class) {
            ((TIntDoubleHashMap) primitiveArrayMap()).put(index, (Double) val);
        }
    }
    
    private TIntSet primitiveArrayMapKeySet() {
        if(primitiveArrayMap == null) {
            return null;
        }
        Class<?> type = clazz.getComponentType();
        if (type == int.class) {
            return ((TIntIntHashMap) primitiveArrayMap).keySet();
        } else if (type == short.class) {
            return ((TIntShortHashMap) primitiveArrayMap).keySet();
        } else if (type == long.class) {
            return ((TIntLongHashMap) primitiveArrayMap).keySet(); 
        } else if (type == byte.class) {
            return ((TIntByteHashMap) primitiveArrayMap).keySet();
        } else if (type == char.class) {
            return ((TIntCharHashMap) primitiveArrayMap).keySet();
        } else if (type == boolean.class) {
            return ((TIntByteHashMap) primitiveArrayMap).keySet();
        } else if (type == float.class) {
            return ((TIntFloatHashMap) primitiveArrayMap).keySet();
        } else if (type == double.class) {
            return ((TIntDoubleHashMap) primitiveArrayMap).keySet();
        } else {
            return null;
        }
    }
    
    private int primitiveArrayMapSize() {
        if(primitiveArrayMap == null) {
            return 0;
        }
        Class<?> type = clazz.getComponentType();
        if (type == int.class) {
            return ((TIntIntHashMap) primitiveArrayMap).size();
        } else if (type == short.class) {
            return ((TIntShortHashMap) primitiveArrayMap).size();
        } else if (type == long.class) {
            return ((TIntLongHashMap) primitiveArrayMap).size(); 
        } else if (type == byte.class) {
            return ((TIntByteHashMap) primitiveArrayMap).size();
        } else if (type == char.class) {
            return ((TIntCharHashMap) primitiveArrayMap).size();
        } else if (type == boolean.class) {
            return ((TIntByteHashMap) primitiveArrayMap).size();
        } else if (type == float.class) {
            return ((TIntFloatHashMap) primitiveArrayMap).size();
        } else if (type == double.class) {
            return ((TIntDoubleHashMap) primitiveArrayMap).size();
        } else {
            return 0;
        }
    }
    
    private void primitiveArrayMapRemove(int index) {
        if (primitiveArrayMap == null) {
            return;
        }
        Class<?> type = clazz.getComponentType();
        if (type == int.class) {
            ((TIntIntHashMap) primitiveArrayMap).remove(index);
        } else if (type == short.class) {
            ((TIntShortHashMap) primitiveArrayMap).remove(index);
        } else if (type == long.class) {
            ((TIntLongHashMap) primitiveArrayMap).remove(index);
        } else if (type == byte.class) {
            ((TIntByteHashMap) primitiveArrayMap).remove(index);
        } else if (type == char.class) {
            ((TIntCharHashMap) primitiveArrayMap).remove(index);
        } else if (type == boolean.class) {
            ((TIntByteHashMap) primitiveArrayMap).remove(index);
        } else if (type == float.class) {
            ((TIntFloatHashMap) primitiveArrayMap).remove(index);
        } else if (type == double.class) {
            ((TIntDoubleHashMap) primitiveArrayMap).remove(index);
        }
    }
    
    private int fieldId(Field field) {
        return getFieldIdMap(field.getDeclaringClass()).get(field);
    }
    
    private static TObjectIntMap<Field> getFieldIdMap(Class<?> clazz) {
        TObjectIntMap<Field> idMap = clazzFieldIdMap.get(clazz);
        if(idMap == null) {
            synchronized(ObjMap.class) {
                if((idMap = clazzFieldIdMap.get(clazz)) == null) {
                    idMap = new TObjectIntHashMap<>();
                    Field[] fields = clazz.getDeclaredFields();
                    Arrays.sort(fields, new Comparator<Field>() {
                        @Override
                        public int compare(Field o1, Field o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    for(int i=0; i<fields.length; i++) {
                        idMap.put(fields[i], i);
                    }
                    clazzFieldIdMap.put(clazz, idMap);
                }
            }
        }
        return idMap;
    }
    
    private class KeyIterator implements TIntIterator {
        
        private LinkedList<TIntIterator> keys;
        
        public KeyIterator() {
            keys = new LinkedList<>();
            if(ClassUtils.isPrimitiveArray(clazz)) {
                if(primitiveArrayMap != null) {
                    TIntIterator it = primitiveArrayMapKeySet().iterator();
                    if(it.hasNext()) {
                        keys.add(it);
                    }
                }
            } else {
                if(intMap != null) {
                    TIntIterator it = intMap.keySet().iterator();
                    if(it.hasNext()) {
                        keys.add(it);
                    }
                }
                if(objMap != null) {
                    TIntIterator it = objMap.keySet().iterator();
                    if(it.hasNext()) {
                        keys.add(it);
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !keys.isEmpty();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int next() {
            TIntIterator it = keys.pollFirst();
            int res = it.next();
            if(it.hasNext()) {
                keys.addFirst(it);
            }
            return res;
        }
        
        
    }
    

}
