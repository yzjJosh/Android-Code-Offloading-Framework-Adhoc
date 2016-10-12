package mobilecloud.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;

import mobilecloud.utils.IOUtils;

public class IOUtilsTest {
    
    @Test
    public void testSerialization0() throws IOException, SecurityException, ClassNotFoundException {
        SerializeClass s = new SerializeClass();
        s.a = 18;
        s.b = 3.1415926;
        s.s = "test";
        byte[] bytes = IOUtils.toBytesArray(s);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        SerializeClass decode = (SerializeClass) IOUtils.readObject(bytes);
        assertNotNull(decode);
        assertEquals(s.a, decode.a);
        assertEquals(s.b, decode.b, 0.0);
        assertEquals(s.s, decode.s);
    }
    
    @Test
    public void testSerialization1() throws SecurityException, ClassNotFoundException, IOException {
        assertNull(IOUtils.readObject(IOUtils.toBytesArray(null)));
    }
    
    private static class SerializeClass implements Serializable {
        private static final long serialVersionUID = 1L;
        public String s;
        public int a;
        public double b;
    }

}
