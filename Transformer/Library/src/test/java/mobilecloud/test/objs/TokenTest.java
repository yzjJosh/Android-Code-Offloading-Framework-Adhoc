package mobilecloud.test.objs;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import gnu.trove.map.TIntObjectMap;
import mobilecloud.lib.Ignore;
import mobilecloud.objs.FieldReader;
import mobilecloud.objs.ObjDiff;
import mobilecloud.objs.ObjMap;
import mobilecloud.objs.Token;
import mobilecloud.objs.Token.SnapShot;

public class TokenTest {

    @SuppressWarnings("unused")
    private static class TestObj {
        public Object o;
        public int i;
        @Ignore
        public Object c;
    }
    
    private TestObj[] objs;
    private Token token;
    
    @Before
    public void setUp() {
        objs = new TestObj[]{new TestObj(), new TestObj(), new TestObj()};
        objs[0].o = objs[2];
        objs[0].i = 10;
        objs[0].c = new Object();
        objs[1].o = objs[2];
        objs[1].c = new Object();
        objs[2].o = "hello World";
        objs[2].i = Integer.MIN_VALUE;
        objs[2].c = new Object();
        token = new Token.Builder().addObjects(Arrays.asList(objs)).build();
    }
    
    @Test
    public void testTakeSnapShot() {
        SnapShot s0 = token.takeSnapShot();
        assertEquals(s0, token.takeSnapShot());
        assertEquals(3, s0.size());
        objs[1].o = null;
        SnapShot s1 = token.takeSnapShot();
        assertEquals(3, s1.size());
        assertNotEquals(s0, s1);
        objs[2].i = 0;
        SnapShot s2 = token.takeSnapShot();
        assertEquals(3, s2.size());
        assertNotEquals(s1, s2);
        assertNotEquals(s0, s2);
    }
    
    @Test
    public void testTakeSnapShot1() {
        token = new Token.Builder().addObject(objs).build();
        SnapShot s0 = token.takeSnapShot();
        assertEquals(s0, token.takeSnapShot());
        assertEquals(1, s0.size());
        objs[1] = null;
        assertNotEquals(s0, token.takeSnapShot());
    }
    
    @Test
    public void testExpand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        objs[2].o = new TestObj();
        SnapShot s = token.takeSnapShot();
        final Token newToken = token.expand();
        assertEquals(4, newToken.size());
        assertEquals(objs[2].o, newToken.getObject(3));
        SnapShot s1 = newToken.takeSnapShot();
        TIntObjectMap<ObjDiff> diff = s1.diff(s);
        assertEquals(2, diff.size());
        assertNotNull(diff.get(2));
        assertNotNull(diff.get(3));
        objs[2].o = null;
        FieldReader reader = new FieldReader() {
            @Override
            public Object read(ObjMap map, Field f) {
                if(map.isIdentityHashCode(f)) {
                    fail();
                } else if(map.isObjectId(f)) {
                    return newToken.getObject(map.getObjectId(f));
                } else if(map.isValue(f)) {
                    return map.getValue(f);
                }
                return null;
            }

            @Override
            public Object read(ObjMap map, int index) {
                if(map.isIdentityHashCode(index)) {
                    fail();
                } else if(map.isObjectId(index)) {
                    return newToken.getObject(map.getObjectId(index));
                } else if(map.isValue(index)) {
                    return map.getValue(index);
                }
                return null;
            }
        };
        diff.get(2).apply(objs[2], reader);
        assertEquals(newToken.getObject(3), objs[2].o);
    }
    
    @Test
    public void testExpand1() {
        token = new Token.Builder().addObject(objs).build();
        SnapShot s = token.takeSnapShot();
        Token newToken = token.expand();
        assertEquals(4, newToken.size());
        assertTrue(newToken.contains(objs[0]));
        assertTrue(newToken.contains(objs[1]));
        assertTrue(newToken.contains(objs[2]));
        SnapShot s1 = newToken.takeSnapShot();
        assertEquals(4, s1.size());
        assertNotEquals(s, s1);
    }
    
    @Test
    public void testGetObjects() {
        assertEquals(objs[0], token.getObject(0));
        assertEquals(0, token.getId(objs[0]));
        assertEquals(objs[1], token.getObject(1));
        assertEquals(1, token.getId(objs[1]));
        assertEquals(objs[2], token.getObject(2));
        assertEquals(2, token.getId(objs[2]));
    }
    
    @Test
    public void testContains() {
        assertTrue(token.contains(objs[0]));
        assertTrue(token.contains(objs[1]));
        assertTrue(token.contains(objs[2]));
    }
    
    @Test
    public void testSize() {
        assertEquals(3, token.size());
    }
    

}
