package mobilecloud.objs.field;

import java.io.Serializable;
import java.util.Objects;

/**
 * Field value represents a field
 */
public abstract class FieldValue implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new filed value which contains a value
     * @param val the value to store
     * @return the field value
     */
    public static FieldValue newValue(Object val) {
        return new ValueField(val);
    }
    
    /**
     * Create a new field value which contains an object id
     * @param objId the id to store
     * @return the field value
     */
    public static FieldValue newObjectId(int objId) {
        return new IdField(objId);
    }
    
    /**
     * Create a new field value which contains identity hash code of an object
     * @param hashCode the hash code
     * @return the field value
     */
    public static FieldValue newIdentityHashCode(int hashCode) {
        return new HashCodeField(hashCode);
    }

    /**
     * Check if current field value is actual value
     * 
     * @return if this field value is actual value
     */
    public abstract boolean isValue();

    /**
     * Check if current field value is an object id
     * 
     * @return if this field value is object id
     */
    public abstract boolean isObjectId();

    /**
     * Check if current field value is identity hash code
     * 
     * @return if this field value is identity hash code
     */
    public abstract boolean isIdentityHashCode();

    /**
     * Get the the value of this field
     * 
     * @return the value of this field
     */
    public abstract Object get();

}


class ValueField extends FieldValue {
    private static final long serialVersionUID = 1L;

    private final Object val;

    public ValueField(Object val) {
        this.val = val;
    }

    @Override
    public boolean isValue() {
        return true;
    }

    @Override
    public boolean isObjectId() {
        return false;
    }

    @Override
    public boolean isIdentityHashCode() {
        return false;
    }

    @Override
    public Object get() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != ValueField.class) {
            return false;
        } else {
            return Objects.equals(val, ((ValueField) o).val);
        }
    }

    @Override
    public String toString() {
        return "<" + val + ">";
    }

}

class IdField extends FieldValue {
    private static final long serialVersionUID = 1L;
    
    private final int id;
    
    public IdField(int id) {
        this.id = id;
    }
    

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isObjectId() {
        return true;
    }

    @Override
    public boolean isIdentityHashCode() {
        return false;
    }

    @Override
    public Integer get() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != IdField.class) {
            return false;
        } else {
            return id == ((IdField)o).id;
        }
    }
    
    @Override
    public String toString() {
        return "I" + id;
    }
}

class HashCodeField extends FieldValue {
    private static final long serialVersionUID = 1L;
    
    private final int hashCode;
    
    public HashCodeField(int hashCode) {
        this.hashCode = hashCode;
    }
    
    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isObjectId() {
        return false;
    }

    @Override
    public boolean isIdentityHashCode() {
        return true;
    }

    @Override
    public Integer get() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != HashCodeField.class) {
            return false;
        } else {
            return hashCode == ((HashCodeField)o).hashCode;
        }
    }
    
    @Override
    public String toString() {
        return "@" + super.toString();
    }
    
    
}