package mobilecloud.test.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mobilecloud.api.IllegalRequestResponse;
import mobilecloud.api.InternalServerErrorResponse;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.IllegalRequestException;
import mobilecloud.server.InternalServerError;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.ClassUtils;

public class ServerTest {
    
    private Server server;
    private RemoteInvocationRequest req;
    private Foo f;
    private Object[] args = new Object[]{"1", 5};
    
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
    public void setUp() throws IOException {
        server = new Server(Mockito.mock(ExecutableLoader.class));
        server.registerClassLoader("0", ClassLoader.getSystemClassLoader());
        f = new Foo();
        req = new RemoteInvocationRequest().setApplicationId("0").setInvokerData(ClassUtils.toBytesArray(f))
                .setClazzName(Foo.class.getName()).setMethodName("sum")
                .setArgTypesName(new String[] { String.class.getName(), int.class.getName() }).setArgsData(Arrays
                        .asList(new byte[][] { ClassUtils.toBytesArray(args[0]), ClassUtils.toBytesArray(args[1]) }));
    }
    
    private Object decodeOrDefault(byte[] array, Object def) throws SecurityException, ClassNotFoundException, IOException {
        return array == null? def: ClassUtils.readObject(array);
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
        List<byte[]> argsData = res.getArgsData();
        Object invoker = decodeOrDefault(res.getInvokerData(), f);
        Object ret = ClassUtils.readObject(res.getReturnValueData());
        assertEquals(decodeOrDefault(argsData.get(0), args[0]), "1");
        assertEquals(decodeOrDefault(argsData.get(1), args[1]), 5);
        assertEquals(invoker, f);
        assertEquals(ret, 6);
    }
    
}
