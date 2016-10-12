package mobilecloud.test.client;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.Matchers;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.client.Client;
import mobilecloud.client.SocketBuilder;
import mobilecloud.utils.IOUtils;

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
        Mockito.when(builder.build(Matchers.anyString(), Matchers.anyInt(), Matchers.anyInt())).thenReturn(socket);
        
        client = new Client(builder, 100, 2000);
    }

    @Test
    public void testRequestSuccess() throws Exception {
        Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        Response resp = new TestResponse().setSuccess(true);
        InputStream is = IOUtils.toInputStream(resp);
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
        InputStream is = IOUtils.toInputStream(resp);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(socket.getInputStream()).thenReturn(is);
        Mockito.when(socket.getOutputStream()).thenReturn(os);
        
        Response res = client.request(req);
        assertFalse(res.isSuccess());
        assertTrue(res.getThrowable() instanceof NullPointerException);
    }
    
    @Test(timeout = 10000)
    public void testConcurrentRequest() throws Exception {
        final Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        final Response resp = new TestResponse().setSuccess(false).setThrowable(new NullPointerException());
        Mockito.when(socket.getInputStream()).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return IOUtils.toInputStream(resp);
            }
        });
        Mockito.when(socket.getOutputStream()).thenAnswer(new Answer<OutputStream>() {
            @Override
            public OutputStream answer(InvocationOnMock invocation) throws Throwable {
                return new ByteArrayOutputStream();
            }
        });
        
        ExecutorService es = Executors.newCachedThreadPool();
        List<Future<Response>> futures = new LinkedList<>();
        for(int i=0; i<10; i++) {
            futures.add(es.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return client.request(req);
                }
            }));
        }
        for(Future<Response> f: futures) {
            assertTrue(f.get() instanceof TestResponse);
        }
        es.shutdown();
    }
}
