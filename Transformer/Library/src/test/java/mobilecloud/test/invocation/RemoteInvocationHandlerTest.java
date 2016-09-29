package mobilecloud.test.invocation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import mobilecloud.invocation.NoApplicationExecutableException;
import mobilecloud.invocation.RemoteInvocationHandler;
import mobilecloud.invocation.RemoteInvocationRequest;
import mobilecloud.invocation.RemoteInvocationResponse;
import mobilecloud.server.Server;
import mobilecloud.utils.Response;

public class RemoteInvocationHandlerTest {
    
    private RemoteInvocationRequest req;
    
    
    public int sum(String a, int b) {
        return Integer.parseInt(a) + b;
    }
    
    @Before
    public void setUp() {
        Server.getInstance().registerClassLoader(0, ClassLoader.getSystemClassLoader());
        req = new RemoteInvocationRequest().setApplicationId(0).setInvoker(this)
                .setClazzName(RemoteInvocationHandlerTest.class.getName()).setMethodName("sum")
                .setArgTypesName(new String[] { String.class.getName(), int.class.getName() })
                .setArgs(new Object[] { "1", 5 });
    }
    
    @Test
    public void testHandleWithoutExecutable() throws Exception {
        req.setApplicationId(Long.MAX_VALUE);
        RemoteInvocationHandler handler = new RemoteInvocationHandler();
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NoApplicationExecutableException);
    }
    
    @Test
    public void testHandleWithNullInvoker() throws Exception {
        req.setInvoker(null);
        RemoteInvocationHandler handler = new RemoteInvocationHandler();
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NullPointerException);
    }
    
    @Test
    public void testHandleWithInvokeTargetException() throws Exception {
        req.setArgs(new Object[]{"a", 1});
        RemoteInvocationHandler handler = new RemoteInvocationHandler();
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NumberFormatException);
    }
    
    @Test
    public void testHandleWithWrongArgumentType() throws Exception {
        req.setArgs(new Object[]{1, 2});
        RemoteInvocationHandler handler = new RemoteInvocationHandler();
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof IllegalArgumentException);
    }
    
    @Test
    public void testHandleWithRightConfiguration() throws Exception {
        RemoteInvocationHandler handler = new RemoteInvocationHandler();
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        assertEquals(res.getArgs()[0], "1");
        assertEquals(res.getArgs()[1], 5);
        assertEquals(res.getInvoker(), this);
        assertEquals(res.getReturnValue(), 6);
    }
    
}
