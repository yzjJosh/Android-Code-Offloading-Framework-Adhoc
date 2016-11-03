package mobilecloud.test.invocation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Matchers;
import org.mockito.Mockito;

import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.response.RemoteInvocationResponse;
import mobilecloud.api.response.Response;
import mobilecloud.objs.ObjectMigrator;
import mobilecloud.objs.Token;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.handler.invocation.RemoteInvocationHandler;

public class RemoteInvocationHandlerTest {
    
    private String appName = "test";
    private ExecutableLoader loader;
    private RemoteInvocationRequest req;
    private Foo f;
    private Object[] args = new Object[]{"1", 5};
    private Method sum;
    
    
    public static class Foo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private int data = 0;

        public int sum(String a, int b) {
            return Integer.parseInt(a) + b;
        }
        
        public int[] multiply(int[] array, int x) {
            int[] res = new int[array.length];
            for(int i=0; i<array.length; i++) {
                res[i] = array[i] * x;
            }
            return res;
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
    public void setUp() throws IOException, NoSuchMethodException, SecurityException, NoApplicationExecutableException {
        loader = Mockito.mock(ExecutableLoader.class);
        Mockito.when(loader.loadExecutable(Matchers.eq(appName))).thenReturn(ClassLoader.getSystemClassLoader());
        Mockito.when(loader.loadExecutable(AdditionalMatchers.not(Matchers.eq(appName)))).thenThrow(new NoApplicationExecutableException());
        f = new Foo();
        sum = Foo.class.getMethod("sum", String.class, int.class);
        req = buildRequest(sum, f, args);
    }
    
    private RemoteInvocationRequest buildRequest(Method method, Object invoker, Object... args) throws IOException {
        RemoteInvocationRequest req = new RemoteInvocationRequest();
        ObjectMigrator migrator = new ObjectMigrator();
        migrator.migrate(invoker);
        migrator.migrate(Arrays.asList(args));
        Token token = migrator.takeToken();

        String[] argTypes = new String[args.length];
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = types[i].getName();
        }
        
        req.setApplicationId(appName).setClazzName(method.getDeclaringClass().getName()).setMethodName(method.getName())
                .setArgTypesName(argTypes).setToken(token).setInvoker(invoker).setArgs(args);
        return req;
    }
    
    @Test
    public void testHandleWithoutExecutable() throws Exception {
        req.setApplicationId("");
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NoApplicationExecutableException);
    }
    
    @Test
    public void testHandleWithNullInvoker() throws Exception {
        req = buildRequest(sum, null, args);
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NullPointerException);
    }
    
    @Test
    public void testHandleWithInvokeTargetException() throws Exception {
        Object[] args = new Object[]{"a", 1};
        req = buildRequest(sum, f, args);
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof NumberFormatException);
    }
    
    @Test
    public void testHandleWithWrongArgumentType() throws Exception {
        Object[] args = new Object[]{1, 2};
        req = buildRequest(sum, f, args);
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof IllegalArgumentException);
    }
    
    @Test
    public void testHandleWithRightConfiguration() throws Exception {
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        assertEquals(res.getReturnVal(), 6);
        assertEquals(0, res.getToken().size());
        assertTrue(res.getDiffs().isEmpty());
    }
    
    @Test
    public void testHandlePrivateMethod() throws Exception {
        Object[] args = new Object[]{1, 2};
        req = buildRequest(Foo.class.getDeclaredMethod("sum", int.class, int.class), f, args);
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        assertEquals(res.getReturnVal(), 3);
        assertEquals(0, res.getToken().size());
        assertTrue(res.getDiffs().isEmpty());
    }
    
    @Test
    public void testHandlePrimitiveArray() throws Exception {
        int[] array = new int[] {1, 2, 3, 4};
        req = buildRequest(Foo.class.getDeclaredMethod("multiply", int[].class, int.class), f, array, 2);
        RemoteInvocationHandler handler = new RemoteInvocationHandler(loader);
        Response resp = handler.handle(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        int[] ret = (int[]) res.getReturnVal();
        assertEquals(4, ret.length);
        for(int i=0; i<array.length; i++) {
            assertEquals(array[i]*2, ret[i]);
        }
    }
    
}
