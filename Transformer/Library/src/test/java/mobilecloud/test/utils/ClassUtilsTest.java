package mobilecloud.test.utils;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;

import org.junit.Test;

import mobilecloud.utils.ClassUtils;

public class ClassUtilsTest {
    
    @Test
    public void testFindCLass() throws ClassNotFoundException {
        assertEquals(String.class, ClassUtils.loadClass(String.class.getName()));
        assertEquals(BigInteger.class,
                ClassUtils.loadClass(ClassLoader.getSystemClassLoader(), BigInteger.class.getName()));
    }
    
    @Test
    public void testFindUserDefinedClass() throws ClassNotFoundException {
        assertEquals(Foo.class, ClassUtils.loadClass(Foo.class.getName()));
        assertEquals(Foo.class, ClassUtils.loadClass(ClassLoader.getSystemClassLoader(), Foo.class.getName()));
    }
    
    @Test
    public void testFindPrimitiveType() throws ClassNotFoundException {
        assertEquals(int.class, ClassUtils.loadClass(int.class.getName()));
        assertEquals(int.class, ClassUtils.loadClass(ClassLoader.getSystemClassLoader(), int.class.getName()));
    }
    
    @Test
    public void testInmmutable() {
        assertFalse(ClassUtils.isImmutable(HashSet.class));
        assertFalse(ClassUtils.isImmutable(LinkedList.class));
        assertTrue(ClassUtils.isImmutable(Foo.class));
        assertTrue(ClassUtils.isImmutable(int.class));
        assertTrue(ClassUtils.isImmutable(Bar.class));
        assertTrue(ClassUtils.isImmutable(Bar.Foo.class));
    }
    
    private static class Foo {};
    
    private class Bar{
        public class Foo{}
    };

}
