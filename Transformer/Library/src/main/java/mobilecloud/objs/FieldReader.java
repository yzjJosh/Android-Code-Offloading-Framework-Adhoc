package mobilecloud.objs;

/**
 * Field reader is a reader that can read a FieldValue object
 */
public interface FieldReader {
    
    /**
     * Get the actual value of a field
     * @param field the FieldValue object to be read
     * @return the actual value of that field
     */
    public Object read(FieldValue field);

}
