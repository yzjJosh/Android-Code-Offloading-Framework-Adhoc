package mobilecloud.test.client;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Matchers;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.client.Client;
import mobilecloud.client.SocketBuilder;
import mobilecloud.test.Utils;

public class ClientTest {
    
    private static class TestRequest extends Request {
        private static final long serialVersionUID = 1L;
    }
    
    private static class TestResponse extends Response {
        private static final long serialVersionUID = 1L;
    }
    
    private Socket socket;
    private Client client;
    
    @Before
    public void setUp() throws Exception {
        socket = Mockito.mock(Socket.class);
        
        SocketBuilder builder = Mockito.mock(SocketBuilder.class);
        Mockito.when(builder.build(Matchers.anyString(), Matchers.anyInt())).thenReturn(socket);
        
        client = new Client(builder);
    }

    @Test
    public void testRequestSuccess() throws Exception {
        Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        Response resp = new TestResponse().setSuccess(true);
        InputStream is = Utils.toInputStream(resp);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(socket.getInputStream()).thenReturn(is);
        Mockito.when(socket.getOutputStream()).thenReturn(os);
        
        Response res = client.request(req);
        assertTrue(res.isSuccess());
    }
    
    @Test
    public void testRequestFails() throws Exception {
        Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        Response resp = new TestResponse().setSuccess(false).setThrowable(new NullPointerException());
        InputStream is = Utils.toInputStream(resp);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(socket.getInputStream()).thenReturn(is);
        Mockito.when(socket.getOutputStream()).thenReturn(os);
        
        Response res = client.request(req);
        assertFalse(res.isSuccess());
        assertTrue(res.getThrowable() instanceof NullPointerException);
    }
}
