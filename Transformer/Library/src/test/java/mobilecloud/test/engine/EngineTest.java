package mobilecloud.test.engine;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import lombok.NonNull;
import mobilecloud.client.Client;
import mobilecloud.client.DefaultSocketBuilder;
import mobilecloud.client.SocketBuilder;
import mobilecloud.engine.Engine;
import mobilecloud.engine.Host;
import mobilecloud.engine.RemoteExecutionFailedException;
import mobilecloud.engine.Schedular;
import mobilecloud.invocation.RemoteInvocationRequest;
import mobilecloud.invocation.RemoteInvocationResponse;
import mobilecloud.lib.Remotable;
import mobilecloud.server.Server;
import mobilecloud.test.Utils;
import mobilecloud.utils.Response;

public class EngineTest {
    
    private class MockSocketBuilder implements SocketBuilder {
        @Override
        public Socket build(String ip, int port) throws Exception {
            return socket;
        }
    }
    
    private static class ListNode implements Remotable {
        
        private static final long serialVersionUID = 1L;
        private boolean isOnServer = false;
        private int id = System.identityHashCode(this);
        private boolean isNew = false;
        public int val;
        public ListNode next;
        
        public ListNode(int val) {
            this.val = val;
        }

        @Override
        public void setIsOnServer(boolean val) {
            this.isOnServer = val;
        }

        @Override
        public boolean isOnServer() {
            return this.isOnServer;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean isNew() {
            return this.isNew;
        }

        @Override
        public void setIsNew(boolean val) {
            this.isNew = val;
        }
        
    }
    
    @SuppressWarnings("unused")
    private static class List implements Remotable {
        private static final long serialVersionUID = 1L;
        public ListNode head;
        public int size;
        private boolean isOnServer = false;
        private int id = System.identityHashCode(this);
        private boolean isNew = false;
        
        public void add(int val) {
            ListNode temp = new ListNode(0);
            temp.next = head;
            ListNode prev = temp;
            for(ListNode node = head; node != null; node = node.next) {
                prev = node;
            }
            prev.next = new ListNode(val);
            head = temp.next;
            size ++;
        }
        
        public void remove(int val) {
            ListNode temp = new ListNode(0);
            temp.next = head;
            ListNode prev = temp;
            ListNode node = head;
            while(node != null) {
                if(node.val == val) {
                    break;
                }
                prev = node;
                node = node.next;
            }
            if(node != null) {
                prev.next = node.next;
                size --;
            }
            head = temp.next;
        }
        
        public List multiply(@NonNull Integer multiple) {
            List l = new List();
            ListNode node = head;
            while(node != null) {
                l.add(node.val * multiple);
                node = node.next;
            }
            return l;
        }

        @Override
        public void setIsOnServer(boolean val) {
            this.isOnServer = val;
        }

        @Override
        public boolean isOnServer() {
            return this.isOnServer;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean isNew() {
            return this.isNew;
        }

        @Override
        public void setIsNew(boolean val) {
            this.isNew = val;
        }
    }
    
    private ByteArrayInputStream buffer;
    
    private class MockInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return buffer.read();
        }
        
    }
    
    private class MockOutputStream extends OutputStream {
        
        private ByteArrayOutputStream os = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            os.write(b);
        }
        
        @Override
        public void flush() throws IOException {
            super.flush();
            byte[] requestBytes = os.toByteArray();
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(requestBytes));
            RemoteInvocationRequest req = null;
            try {
                req = (RemoteInvocationRequest) is.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Response resp = Server.getInstance().serve(req);
            RemoteInvocationResponse invocResp = (RemoteInvocationResponse) resp;
            setListMetaData((List)invocResp.getInvoker());
            if(invocResp.getReturnValue() instanceof List) {
                setListMetaData((List) invocResp.getReturnValue());
            }
            buffer = new ByteArrayInputStream(Utils.toBytesArray(resp));
        }
        
        private void setListMetaData(List list) {
            setNewObjectMetaData(list);
            ListNode node = list.head;
            while(node != null) {
                setNewObjectMetaData(node);
                node = node.next;
            }
        }
        
        private void setNewObjectMetaData(Object obj) {
            if(obj instanceof Remotable) {
                Remotable r = (Remotable) obj;
                if(!r.isOnServer()) {
                    r.setIsNew(true);
                    r.setIsOnServer(true);
                }
            }
        }
        
    }
    
    private final static String AppName = "TestApp";
    private Socket socket;
    private Host h = new Host("192.168.1.1", 0);
    private Engine engine;
    
    private Method add;
    private Method remove;
    private Method multiply;
    
    private List l;
    private ListNode l0;
    private ListNode l1;
    private ListNode l2;
    private ListNode l3;
    
    @Before
    public void setUp() throws Exception {
        Schedular.getInstance().addHost(h);
        Client.getInstance().setSocketBuilder(new MockSocketBuilder());
        Server.getInstance().registerClassLoader(AppName, ClassLoader.getSystemClassLoader());
        engine = new Engine();
        engine.setAppName(AppName);
        
        socket = Mockito.mock(Socket.class);
        Mockito.when(socket.getOutputStream()).thenReturn(new MockOutputStream());
        Mockito.when(socket.getInputStream()).thenReturn(new MockInputStream());
        
        add = List.class.getMethod("add", int.class);
        remove = List.class.getMethod("remove", int.class);
        multiply = List.class.getMethod("multiply", Integer.class);
        
        l0 = new ListNode(0);
        l1 = new ListNode(1);
        l2 = new ListNode(2);
        l3 = new ListNode(3);
        l0.next = l1;
        l1.next = l2;
        l2.next = l3;
        l3.next = null;
        l = new List();
        l.head = l0;
        l.size = 4;
    }
    
    @After
    public void tearDown() {
        Schedular.getInstance().removeHost(h);
        Client.getInstance().setSocketBuilder(new DefaultSocketBuilder());
    }

    @Test
    public void testAdd() {
        engine.invokeRemotely(add, l, 100);
        assertFalse(l.isNew());
        assertFalse(l.isOnServer());
        assertEquals(5, l.size);
        assertEquals(l0, l.head);
        assertFalse(l0.isNew());
        assertFalse(l0.isOnServer());
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertFalse(l1.isNew());
        assertFalse(l1.isOnServer());
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertFalse(l2.isNew());
        assertFalse(l2.isOnServer());
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertFalse(l3.isNew());
        assertFalse(l3.isOnServer());
        assertEquals(3, l3.val);
        assertNotNull(l3.next);
        assertFalse(l3.next.isNew());
        assertFalse(l3.next.isOnServer());
        assertEquals(100, l3.next.val);
        assertNull(l3.next.next);
    }
    
    @Test
    public void testRemove() {
        engine.invokeRemotely(remove, l, 0);
        assertFalse(l.isNew());
        assertFalse(l.isOnServer());
        assertEquals(3, l.size);
        assertEquals(l1, l.head);
        assertFalse(l0.isNew());
        assertFalse(l0.isOnServer());
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertFalse(l1.isNew());
        assertFalse(l1.isOnServer());
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertFalse(l2.isNew());
        assertFalse(l2.isOnServer());
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertFalse(l3.isNew());
        assertFalse(l3.isOnServer());
        assertEquals(3, l3.val);
        assertNull(l3.next);
    }
    
    @Test
    public void testMultiply() {
        List list = (List) engine.invokeRemotely(multiply, l, 10);
        assertFalse(l.isNew());
        assertFalse(l.isOnServer());
        assertEquals(4, l.size);
        assertEquals(l0, l.head);
        assertFalse(l0.isNew());
        assertFalse(l0.isOnServer());
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertFalse(l1.isNew());
        assertFalse(l1.isOnServer());
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertFalse(l2.isNew());
        assertFalse(l2.isOnServer());
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertFalse(l3.isNew());
        assertFalse(l3.isOnServer());
        assertEquals(3, l3.val);
        assertNull(l3.next);
        assertFalse(list.isNew());
        assertFalse(list.isOnServer());
        assertEquals(4, list.size);
        assertNotNull(list.head);
        ListNode n0 = list.head;
        assertFalse(n0.isNew());
        assertFalse(n0.isOnServer());
        assertEquals(0, n0.val);
        assertNotNull(n0.next);
        ListNode n1 = n0.next;
        assertFalse(n1.isNew());
        assertFalse(n1.isOnServer());
        assertEquals(10, n1.val);
        assertNotNull(n1.next);
        ListNode n2 = n1.next;
        assertFalse(n2.isNew());
        assertFalse(n2.isOnServer());
        assertEquals(20, n2.val);
        assertNotNull(n2.next);
        ListNode n3 = n2.next;
        assertFalse(n3.isNew());
        assertFalse(n3.isOnServer());
        assertEquals(30, n3.val);
        assertNull(n3.next);
    }
    
    @Test(expected = NullPointerException.class)
    public void testError() throws Throwable {
        try {
            engine.invokeRemotely(multiply, l, new Object[]{null});
        } catch(RemoteExecutionFailedException e) {
            assertFalse(l.isNew());
            assertFalse(l.isOnServer());
            assertEquals(4, l.size);
            assertEquals(l0, l.head);
            assertFalse(l0.isNew());
            assertFalse(l0.isOnServer());
            assertEquals(0, l0.val);
            assertEquals(l1, l0.next);
            assertFalse(l1.isNew());
            assertFalse(l1.isOnServer());
            assertEquals(1, l1.val);
            assertEquals(l2, l1.next);
            assertFalse(l2.isNew());
            assertFalse(l2.isOnServer());
            assertEquals(2, l2.val);
            assertEquals(l3, l2.next);
            assertFalse(l3.isNew());
            assertFalse(l3.isOnServer());
            assertEquals(3, l3.val);
            assertNull(l3.next);
            throw e.getCause();
        }
        fail();
    }
}
