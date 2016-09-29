package mobilecloud.utils;

import java.lang.reflect.Modifier;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.NonNull;

/**
 * A serialized string of arbitrary object
 */
public class DataString {

    public final String type;
    public final String content;
    
    private transient String serializedData;

    public DataString(@NonNull Object data) {
        this.type = data.getClass().getName();
        this.content = serialize(data);
    }
    
    public DataString(@NonNull String dataString) {
        DataString temp = (DataString) deserialize(dataString, DataString.class);
        this.type = temp.type;
        this.content = temp.content;
        
    }

    /**
     * Deserialize the content inside this data string
     * @return the actual data
     * @throws ClassNotFoundException Cannot load the type of this data
     */
    public Object deserialize() throws ClassNotFoundException {
        Class<?> returnType = ClassUtils.loadClass(type);
        return deserialize(content, returnType);
    }
    
    @Override
    public String toString() {
        if(serializedData == null) {
            serializedData = serialize(this);
        }
        return serializedData;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != DataString.class) {
            return false;
        } else {
            DataString that = (DataString) o;
            return Objects.equals(this.type, that.type) && Objects.equals(this.content, that.content);
        }
    }
    
    private String serialize(Object obj) {
        if(obj == null) return null;
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .excludeFieldsWithModifiers(Modifier.STATIC).create();
        return gson.toJson(obj);
    }
    
    private Object deserialize(String str, Class<?> type) {
        if(str == null) return null;
        Gson gson = new Gson();
        return gson.fromJson(str, type);
    }

}
