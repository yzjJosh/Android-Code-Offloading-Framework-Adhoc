package mobilecloud.test.objs;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import mobilecloud.objs.FieldReader;
import mobilecloud.objs.ObjDiff;
import mobilecloud.objs.ObjMap;

public class ObjDiffTest {
    
    private static class TestClass {
        public Object o;
        public int i;
    }
    
    private ObjDiff diff;
    private Object[] objs;
    private FieldReader reader = new FieldReader() {

        @Override
        public Object read(ObjMap map, Field f) {
            if(map.isObjectId(f)) {
                return objs[map.getObjectId(f)];
            } else {
                return map.getValue(f);
            }
        }

        @Override
        public Object read(ObjMap map, int index) {
            if(map.isObjectId(index)) {
                return objs[map.getObjectId(index)];
            } else {
                return map.getValue(index);
            }
        }
        
    };
    
    @Before
    public void setUp() {
        objs = new Object[]{new TestClass(), new TestClass(), new TestClass()};
    }
    
    @Test
    public void test0 () throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ObjMap transform = new ObjMap(TestClass.class);
        transform.putValue(TestClass.class.getDeclaredField("o"), "Hello World");
        transform.putValue(TestClass.class.getDeclaredField("i"), 101);
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
        ObjMap transform = new ObjMap(TestClass.class);
        transform.putValue(TestClass.class.getDeclaredField("o"), null);
        diff = new ObjDiff(transform);
        diff.apply(t, reader);
        assertNull(t.o);
    }
    
    @Test
    public void test2()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ObjMap transform = new ObjMap(TestClass.class);
        transform.putObjectId(TestClass.class.getDeclaredField("o"), 2);
        diff = new ObjDiff(transform);
        diff.apply(objs[0], reader);
        assertEquals(objs[2], ((TestClass) objs[0]).o);
    }
    
    @Test
    public void test3() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ObjMap transform = new ObjMap(Object[].class);
        Object obj2 = objs[2];
        transform.putObjectId(0, 2);
        transform.putValue(1, "test");
        transform.putValue(2, null);
        diff = new ObjDiff(transform);
        diff.apply(objs, reader);
        assertEquals(obj2, objs[0]);
        assertEquals("test", objs[1]);
        assertNull(objs[2]);
    }
    

}
