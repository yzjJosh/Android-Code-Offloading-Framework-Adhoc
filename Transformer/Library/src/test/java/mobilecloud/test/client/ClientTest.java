package mobilecloud.test.client;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
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

import mobilecloud.api.request.Request;
import mobilecloud.api.response.Response;
import mobilecloud.client.Client;
import mobilecloud.client.SocketBuilder;
import mobilecloud.api.deliverer.Deliverer;
import mobilecloud.api.receiver.Receiver;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class ClientTest {
    
    private static class TestRequest extends Request {
        private static final long serialVersionUID = 1L;
    }
    
    private static class TestResponse extends Response {
        private static final long serialVersionUID = 1L;
    }
    
    private static class TestRequestDeliverer implements Deliverer<Request> {

        @Override
        public void deliver(Request request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
                throws Exception {
            os.get().writeObject(request);
            os.get().flush();
        }
    }
    
    public static class TestResponseReceiver implements Receiver<Response> {

        @Override
        public Response receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
                throws Exception {
            return (Response) is.get().readObject();
        }
        
    }
    
    private Socket socket;
    private Client client;
    
    @Before
    public void setUp() throws Exception {
        socket = Mockito.mock(Socket.class);
        InetAddress addr = Mockito.mock(InetAddress.class);
        Mockito.when(socket.getLocalAddress()).thenReturn(addr);
        
        SocketBuilder builder = Mockito.mock(SocketBuilder.class);
        Mockito.when(builder.build(Matchers.anyString(), Matchers.anyInt(), Matchers.anyInt())).thenReturn(socket);
        
        client = new Client(builder, 100, 2000);
        client.registerDeliverer(TestRequest.class.getName(), new TestRequestDeliverer());
        client.registerReceiver(TestResponse.class.getName(), new TestResponseReceiver());
    }
    
    private InputStream createInputStream(Response resp) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(o);
        os.writeObject(resp.getClass().getName());
        os.writeObject(resp);
        byte[] data = o.toByteArray();
        return new ByteArrayInputStream(data);
    }

    @Test
    public void testRequestSuccess() throws Exception {
        Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        Response resp = new TestResponse().setSuccess(true);
        InputStream is = createInputStream(resp);
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
        InputStream is = createInputStream(resp);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(socket.getInputStream()).thenReturn(is);
        Mockito.when(socket.getOutputStream()).thenReturn(os);
        
        Response res = client.request(req);
        assertFalse(res.isSuccess());
        assertTrue(res.getThrowable() instanceof NullPointerException);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNoDevilerer() throws Exception {
        Request req = new Request() { private static final long serialVersionUID = 1L; };
        client.request(req);
    }
    
    @Test(timeout = 10000)
    public void testConcurrentRequest() throws Exception {
        final Request req = new TestRequest().setIp("192.168.0.1").setPort(80);
        final Response resp = new TestResponse().setSuccess(false).setThrowable(new NullPointerException());
        Mockito.when(socket.getInputStream()).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return createInputStream(resp);
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
