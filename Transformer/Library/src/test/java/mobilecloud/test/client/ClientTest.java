package mobilecloud.test.client;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mobilecloud.client.Client;
import mobilecloud.client.DefaultSocketBuilder;
import mobilecloud.client.SocketBuilder;
import mobilecloud.test.Utils;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

public class ClientTest {
    
    private Socket socket;
    
    @Before
    public void setUp() {
        socket = Mockito.mock(Socket.class);
        Client.getInstance().setSocketBuilder(new MockSocketBuilder());
    }
    
    @After
    public void tearDown() {
        Client.getInstance().setSocketBuilder(new DefaultSocketBuilder());
    }
    
    private static class TestRequest extends Request {
        private static final long serialVersionUID = 1L;
    }
    
    private static class TestResponse extends Response {
        private static final long serialVersionUID = 1L;
    }
    
    private class MockSocketBuilder implements SocketBuilder {
        @Override
        public Socket build(String ip, int port) throws Exception {
            return socket;
        }
    }

    @Test
    public void testRequestSuccess() throws Exception {
        Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        Response resp = new TestResponse().setSuccess(true);
        InputStream is = Utils.toInputStream(resp);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(socket.getInputStream()).thenReturn(is);
        Mockito.when(socket.getOutputStream()).thenReturn(os);
        
        Response res = Client.getInstance().request(req);
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
        
        Response res = Client.getInstance().request(req);
        assertFalse(res.isSuccess());
        assertTrue(res.getThrowable() instanceof NullPointerException);
    }
}
