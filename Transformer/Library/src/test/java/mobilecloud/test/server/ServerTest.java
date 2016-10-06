package mobilecloud.test.server;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

import mobilecloud.api.IllegalRequestResponse;
import mobilecloud.api.InternalServerErrorResponse;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.IllegalRequestException;
import mobilecloud.server.InternalServerError;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;

public class ServerTest {
    
    private Server server;
    private RemoteInvocationRequest req;
    private Foo f;
    
    public static class Foo implements Serializable {

        private static final long serialVersionUID = 1L;

        public int sum(String a, int b) {
            return Integer.parseInt(a) + b;
        }

    }
    
    @Before
    public void setUp() {
        server = new Server();
        server.registerClassLoader("0", ClassLoader.getSystemClassLoader());
        req = new RemoteInvocationRequest().setApplicationId("0").setInvoker(f = new Foo())
                .setClazzName(Foo.class.getName()).setMethodName("sum")
                .setArgTypesName(new String[] { String.class.getName(), int.class.getName() })
                .setArgs(new Serializable[] { "1", 5 });
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
    public void testServeLegalRequest() {
        Response resp = server.serve(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        assertEquals(res.getArgs()[0], "1");
        assertEquals(res.getArgs()[1], 5);
        assertEquals(res.getInvoker(), f);
        assertEquals(res.getReturnValue(), 6);
    }
    
}
