package mobilecloud.test.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.IllegalRequestResponse;
import mobilecloud.api.response.InternalServerErrorResponse;
import mobilecloud.api.response.RemoteInvocationResponse;
import mobilecloud.api.response.Response;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.objs.ObjectMigrator;
import mobilecloud.objs.Token;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.IllegalRequestException;
import mobilecloud.server.InternalServerError;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;

public class ServerTest {
    
    private Server server;
    private RemoteInvocationRequest req;
    private Foo f;
    private Object[] args = new Object[]{"1", 5};
    private Method sum;
    private String appName = "test";
    
    public static class Foo implements Serializable {

        private int data = 0;
        
        private static final long serialVersionUID = 1L;

        public int sum(String a, int b) {
            return Integer.parseInt(a) + b;
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
        ExecutableLoader loader = Mockito.mock(ExecutableLoader.class);
        Mockito.when(loader.loadExecutable(Matchers.anyString())).thenReturn(ClassLoader.getSystemClassLoader());
        server = new Server(loader, new MetricGenerator());
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
    public void testServeInternalError() {
        server.registerHandler(RemoteInvocationRequest.class.getName(), new Handler() {
            @Override
            public Response handle(Request request) throws Exception {
                throw new NullPointerException();
            }
        });
        Response resp = server.serve(req);
        assertTrue(resp instanceof InternalServerErrorResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof InternalServerError);
    }
    
    @Test
    public void testServeIllegalRequest() {
        @SuppressWarnings("serial")
        Response resp = server.serve(new Request() {});
        assertTrue(resp instanceof IllegalRequestResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof IllegalRequestException);
    }
    
    @Test
    public void testServeLegalRequest() throws SecurityException, ClassNotFoundException, IOException {
        Response resp = server.serve(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        assertEquals(res.getReturnVal(), 6);
    }
    
}
