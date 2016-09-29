package mobilecloud.test.utils;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Test;

import mobilecloud.utils.DataString;

public class DataStringTest {
    
    private static class Foo {
        int a;
        String c;
        
        @Override
        public boolean equals(Object o) {
            if(o == null || o.getClass() != Foo.class) {
                return false;
            } else {
                Foo f = (Foo) o;
                return f.a == a && Objects.equals(c, f.c);
            }
        }
    }
    
    
    @Test
    public void testDataStringWithClass1() throws ClassNotFoundException {
        Foo f = new Foo();
        DataString data = new DataString(f);
        assertNotNull(data.toString());
        assertEquals(data, new DataString(data.toString()));
        assertEquals(f, data.deserialize());
    }
    
    @Test
    public void testJsonWithClass2() throws ClassNotFoundException {
        Foo f = new Foo();
        f.a = Integer.MIN_VALUE;
        f.c = "%\"sdf";
        DataString data = new DataString(f);
        assertNotNull(data.toString());
        assertEquals(data, new DataString(data.toString()));
        assertEquals(f, data.deserialize());
    }
    
    @Test
    public void testJsonWithPrimitiveType() throws ClassNotFoundException {
        DataString data = new DataString(1);
        assertNotNull(data.toString());
        assertEquals(data, new DataString(data.toString()));
        assertEquals(1, data.deserialize());
    }

}
