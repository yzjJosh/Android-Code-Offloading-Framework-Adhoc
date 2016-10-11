package mobilecloud.objs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Field value represents a field
 */
public class FieldValue implements Serializable {
    private static final long serialVersionUID = 1L;
    private Wrapper val;

    private FieldValue(Wrapper val) {
        this.val = val;
    }
    
    /**
     * Create a new filed value which contains a value
     * @param val the value to store
     * @return the field value
     */
    public static FieldValue newValue(Object val) {
        return new FieldValue(new ValueWrapper(val));
    }
    
    /**
     * Create a new field value which contains an object id
     * @param objId the id to store
     * @return the field value
     */
    public static FieldValue newObjectId(int objId) {
        return new FieldValue(new IdWrapper(objId));
    }
    
    /**
     * Create a new field value which contains identity hash code of an object
     * @param hashCode the hash code
     * @return the field value
     */
    public static FieldValue newIdentityHashCode(int hashCode) {
        return new FieldValue(new IdentityHashCodeWrapper(hashCode));
    }
    
    public static FieldValue newArray(FieldValue[] array) {
        return new FieldValue(new ArrayWrapper(array));
    }

    /**
     * Check if current field value is actual value
     * 
     * @return if this field value is actual value
     */
    public boolean isValue() {
        return val instanceof ValueWrapper;
    }

    /**
     * Check if current field value is an object id
     * 
     * @return if this field value is object id
     */
    public boolean isObjectId() {
        return val instanceof IdWrapper;
    }

    /**
     * Check if current field value is identity hash code
     * 
     * @return if this field value is identity hash code
     */
    public boolean isIdentityHashCode() {
        return val instanceof IdentityHashCodeWrapper;
    }

    /**
     * Get the the value of this field, can be an id, identity hash code, or a
     * value
     * 
     * @return the value of this field
     */
    public Object get() {
        return val.obj;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != FieldValue.class) {
            return false;
        } else {
            return Objects.equals(val, ((FieldValue) o).val);
        }
    }

    @Override
    public String toString() {
        return val.toString();
    }

    /**
     * Wrapper object of a field value
     */
    private static class Wrapper implements Serializable {
        private static final long serialVersionUID = 1L;
        public final Object obj;

        public Wrapper(Object val) {
            this.obj = val;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != getClass()) {
                return false;
            } else {
                Wrapper that = (Wrapper) o;
                return Objects.equals(obj, that.obj);
            }
        }

        @Override
        public String toString() {
            return Objects.toString(obj);
        }
    }

    private static class ValueWrapper extends Wrapper {
        private static final long serialVersionUID = 1L;

        public ValueWrapper(Object val) {
            super(val);
        }

        @Override
        public String toString() {
            return "<" + super.toString() + ">";
        }
    }

    private static class IdentityHashCodeWrapper extends Wrapper {
        private static final long serialVersionUID = 1L;

        public IdentityHashCodeWrapper(int val) {
            super(val);
        }

        @Override
        public String toString() {
            return "@" + super.toString();
        }
    }

    private static class IdWrapper extends Wrapper {
        private static final long serialVersionUID = 1L;

        public IdWrapper(int val) {
            super(val);
        }

        @Override
        public String toString() {
            return "I" + super.toString();
        }
    }
    
    private static class ArrayWrapper extends Wrapper {
        private static final long serialVersionUID = 1L;
        
        public ArrayWrapper(FieldValue[] val) {
            super(val);
        }
        
        @Override
        public String toString() {
            return Arrays.toString((FieldValue[]) obj);
        }
        
        @Override
        public boolean equals(Object o) {
            if(o == null || o.getClass() != getClass()) {
                return false;
            } else {
                ArrayWrapper that = (ArrayWrapper) o;
                return Arrays.equals((FieldValue[]) obj, (FieldValue[]) that.obj);
            }
        }
        
    }
}