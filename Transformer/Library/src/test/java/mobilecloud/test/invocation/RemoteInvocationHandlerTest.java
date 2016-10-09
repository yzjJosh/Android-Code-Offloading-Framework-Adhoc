package mobilecloud.test.invocation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Response;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.Server;
import mobilecloud.server.handler.invocation.RemoteInvocationHandler;
import mobilecloud.utils.ClassUtils;

public class RemoteInvocationHandlerTest {
    
    private Server server;
    private RemoteInvocationRequest req;
    private Foo f;
    
    
    public static class Foo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private int data = 0;

        public int sum(String a, int b) {
            return Integer.parseInt(a) + b;
        }
        
        @SuppressWarnings("unused")
        private int sum(int a, int b) {
            return a + b;
        }
        
        @Override
        public boolean equals(Object o) {
            if(o == null || o.getClass() != Foo.class) {
                return false;
            } else {
                return data == ((Foo)o).data;
            }
        }
        
    }
    
    @Before
    public void setUp() throws IOException {
        server = new Server(Mockito.mock(ExecutableLoader.class));
        server.registerClassLoader("0", ClassLoader.getSystemClassLoader());
        f = new Foo();
        req = new RemoteInvocationRequest().setApplicationId("0").setInvokerData(ClassUtils.toBytesArray(f))
                .setClazzName(Foo.class.getName()).setMethodName("sum")
                .setArgTypesName(new String[] { String.class.getName(), int.class.getName() })
                .setArgsData(ClassUtils.toBytesArray(new Serializable[] { "1", 5 }));
    }
    
    @Test
    public void testHandleWithoutExecutable() throws Exception {
        req.setApplicationId("");
        RemoteInvocationHandler handler = new RemoteInvocationHandler(server);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NoApplicationExecutableException);
    }
    
    @Test
    public void testHandleWithNullInvoker() throws Exception {
        req.setInvokerData(ClassUtils.toBytesArray(null));
        RemoteInvocationHandler handler = new RemoteInvocationHandler(server);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NullPointerException);
    }
    
    @Test
    public void testHandleWithInvokeTargetException() throws Exception {
        req.setArgsData(ClassUtils.toBytesArray(new Serializable[]{"a", 1}));
        RemoteInvocationHandler handler = new RemoteInvocationHandler(server);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NumberFormatException);
    }
    
    @Test
    public void testHandleWithWrongArgumentType() throws Exception {
        req.setArgsData(ClassUtils.toBytesArray(new Serializable[]{1, 2}));
        RemoteInvocationHandler handler = new RemoteInvocationHandler(server);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof IllegalArgumentException);
    }
    
    @Test
    public void testHandleWithRightConfiguration() throws Exception {
        RemoteInvocationHandler handler = new RemoteInvocationHandler(server);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        Object[] args = (Object[]) ClassUtils.readObject(res.getArgsData());
        Object invoker = ClassUtils.readObject(res.getInvokerData());
        Object ret = ClassUtils.readObject(res.getReturnValueData());
        assertEquals(args[0], "1");
        assertEquals(args[1], 5);
        assertEquals(invoker, f);
        assertEquals(ret, 6);
    }
    
    @Test
    public void testHandlePrivateMethod() throws Exception {
        req.setArgTypesName(new String[] { int.class.getName(), int.class.getName() })
                .setArgsData(ClassUtils.toBytesArray(new Serializable[] { 1, 2 }));
        RemoteInvocationHandler handler = new RemoteInvocationHandler(server);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        Object[] args = (Object[]) ClassUtils.readObject(res.getArgsData());
        Object invoker = ClassUtils.readObject(res.getInvokerData());
        Object ret = ClassUtils.readObject(res.getReturnValueData());
        assertEquals(args[0], 1);
        assertEquals(args[1], 2);
        assertEquals(invoker, f);
        assertEquals(ret, 3);
    }
    
}
