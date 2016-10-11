package mobilecloud.test.objs;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import mobilecloud.objs.Config;
import mobilecloud.objs.FieldReader;
import mobilecloud.objs.FieldValue;
import mobilecloud.objs.ObjDiff;

public class ObjDiffTest {
    
    private static class TestClass {
        public Object o;
        public int i;
    }
    
    private ObjDiff diff;
    private Object[] objs;
    private FieldReader reader = new FieldReader() {

        @Override
        public Object read(FieldValue field) {
            if(field.isObjectId()) {
                return objs[(Integer) field.get()];
            } else {
                return field.get();
            }
        }
        
    };
    
    @Before
    public void setUp() {
        objs = new Object[]{new TestClass(), new TestClass(), new TestClass()};
    }
    
    @Test
    public void test0 () throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Map<String, FieldValue> transform = new HashMap<>();
        transform.put("o", FieldValue.newValue("Hello World"));
        transform.put("i", FieldValue.newValue(101));
        diff = new ObjDiff(transform);
        diff.apply(objs[0], reader);
        TestClass res = (TestClass) objs[0];
        assertEquals("Hello World", res.o);
        assertEquals(101, res.i);
    }
    
    @Test
    public void test1() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        TestClass t = new TestClass();
        t.o = new Object();
        Map<String, FieldValue> transform = new HashMap<>();
        transform.put("o", FieldValue.newValue(null));
        diff = new ObjDiff(transform);
        diff.apply(t, reader);
        assertNull(t.o);
    }
    
    @Test
    public void test2()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Map<String, FieldValue> transform = new HashMap<>();
        transform.put("o", FieldValue.newObjectId(2));
        diff = new ObjDiff(transform);
        diff.apply(objs[0], reader);
        assertEquals(objs[2], ((TestClass) objs[0]).o);
    }
    
    @Test
    public void test3() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Map<String, FieldValue> transform = new HashMap<>();
        Object obj2 = objs[2];
        transform.put(Config.ARRAY_ENTRY_NAME, FieldValue.newArray(new FieldValue[] { FieldValue.newObjectId(2),
                FieldValue.newValue("test"), FieldValue.newValue(null) }));
        diff = new ObjDiff(transform);
        diff.apply(objs, reader);
        assertEquals(obj2, objs[0]);
        assertEquals("test", objs[1]);
        assertNull(objs[2]);
    }
    

}
