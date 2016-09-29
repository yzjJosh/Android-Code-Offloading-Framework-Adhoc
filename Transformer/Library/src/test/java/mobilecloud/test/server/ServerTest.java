package mobilecloud.test.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mobilecloud.invocation.RemoteInvocationHandler;
import mobilecloud.invocation.RemoteInvocationRequest;
import mobilecloud.invocation.RemoteInvocationResponse;
import mobilecloud.server.Handler;
import mobilecloud.server.IllegalRequestException;
import mobilecloud.server.IllegalRequestResponse;
import mobilecloud.server.InternalServerError;
import mobilecloud.server.InternalServerErrorResponse;
import mobilecloud.server.Server;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

public class ServerTest {
    
private RemoteInvocationRequest req;
    
    
    public int sum(String a, int b) {
        return Integer.parseInt(a) + b;
    }
    
    @Before
    public void setUp() {
        Server.getInstance().registerClassLoader(0, ClassLoader.getSystemClassLoader());
        req = new RemoteInvocationRequest().setApplicationId(0).setInvoker(this)
                .setClazzName(ServerTest.class.getName()).setMethodName("sum")
                .setArgTypesName(new String[] { String.class.getName(), int.class.getName() })
                .setArgs(new Object[] { "1", 5 });
    }
    
    @Test
    public void testServeInternalError() {
        Server.getInstance().registerHandler(RemoteInvocationRequest.class.getName(), new Handler() {
            @Override
            public Response handle(Request request) throws Exception {
                throw new NullPointerException();
            }
        });
        Response resp = Server.getInstance().serve(req);
        assertTrue(resp instanceof InternalServerErrorResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof InternalServerError);
    }
    
    @Test
    public void testServeIllegalRequest() {
        Response resp = Server.getInstance().serve(new Request() {});
        assertTrue(resp instanceof IllegalRequestResponse);
        assertFalse(resp.isSuccess());
        assertTrue(resp.getThrowable() instanceof IllegalRequestException);
    }
    
    @Test
    public void testServeLegalRequest() {
        Response resp = Server.getInstance().serve(req);
        assertTrue(resp instanceof RemoteInvocationResponse);
        assertTrue(resp.isSuccess());
        RemoteInvocationResponse res = (RemoteInvocationResponse) resp;
        assertEquals(res.getArgs()[0], "1");
        assertEquals(res.getArgs()[1], 5);
        assertEquals(res.getInvoker(), this);
        assertEquals(res.getReturnValue(), 6);
    }
    
    @After
    public void tearDown() {
        Server.getInstance().registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler());
    }
    
}
