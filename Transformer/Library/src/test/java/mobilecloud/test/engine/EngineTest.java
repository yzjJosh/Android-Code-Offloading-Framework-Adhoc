package mobilecloud.test.engine;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.Context;

import org.mockito.Matchers;

import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.api.UploadApplicationExecutableResponse;
import mobilecloud.client.Client;
import mobilecloud.engine.Engine;
import mobilecloud.engine.RemoteExecutionFailedException;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.schedular.Schedular;
import mobilecloud.lib.Remotable;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.Server;
import mobilecloud.utils.ByteProvider;
import mobilecloud.utils.ClassUtils;

public class EngineTest {
    
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
        
        public List multiply(Integer multiple) {
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
    
    @SuppressWarnings("unused")
    private static class TestSum implements Serializable {
        private static final long serialVersionUID = 1L;

        public int sum(int a, int b) {
            return a + b;
        }
        
        public static int sumStatic(int a, int b) {
            return a + b;
        }
    }
    
    private final String AppName = "test";
    private Engine engine;
    private Schedular schedular;
    private Client client;
    public Server server;
    
    private Method add;
    private Method remove;
    private Method multiply;
    private Method sum;
    private Method sumStatic;
    
    private List l;
    private ListNode l0;
    private ListNode l1;
    private ListNode l2;
    private ListNode l3;
    
    @Before
    public void setUp() throws Throwable {
        Host host = new Host("192.168.0.1", 0);
        
        schedular = Mockito.mock(Schedular.class);
        Mockito.when(schedular.schedule()).thenReturn(host);
        Mockito.when(schedular.trySchedule()).thenReturn(host);
        Mockito.when(schedular.haveAvailable()).thenReturn(true);
        Mockito.when(schedular.availableNum()).thenReturn(1);
        
        server = new Server(Mockito.mock(ExecutableLoader.class));
        
        client = Mockito.mock(Client.class);
        Mockito.when(client.request(Matchers.any(Request.class))).thenAnswer(new Answer<Response>() {
            @Override
            public Response answer(InvocationOnMock invocation) throws Throwable {
                Request req = (Request) invocation.getArguments()[0];
                if(req instanceof UploadApplicationExecutableRequest) {
                    UploadApplicationExecutableRequest up = (UploadApplicationExecutableRequest) req;
                    server.registerClassLoader(up.getApplicationId(), ClassLoader.getSystemClassLoader());
                    return new UploadApplicationExecutableResponse().setSuccess(true);
                } else if(req instanceof RemoteInvocationRequest) {
                    Response resp = server.serve(req);
                    if(resp instanceof RemoteInvocationResponse && resp.isSuccess()) {
                        RemoteInvocationResponse invocResp = (RemoteInvocationResponse) resp;
                        Object invoker = ClassUtils.readObject(invocResp.getInvokerData());
                        Object ret = ClassUtils.readObject(invocResp.getReturnValueData());
                        if(invoker instanceof List) {
                            setListMetaData((List)invoker);
                            invocResp.setInvokerData(ClassUtils.toBytesArray(invoker));
                        }
                        if(ret instanceof List) {
                            setListMetaData((List) ret);
                            invocResp.setReturnValueData(ClassUtils.toBytesArray(ret));
                        }
                    }
                    return resp;
                }
                return null;
            }
        });
        
        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getPackageName()).thenReturn(AppName);
        
        ByteProvider apk = new ByteProvider() {
            @Override
            public byte[] provide() {
                return null;
            }
        };

        Engine.localInit(context, null);
        engine = new Engine(context, client, schedular, apk);
        
        add = List.class.getMethod("add", int.class);
        remove = List.class.getMethod("remove", int.class);
        multiply = List.class.getMethod("multiply", Integer.class);
        sum = TestSum.class.getMethod("sum", int.class, int.class);
        sumStatic = TestSum.class.getMethod("sumStatic", int.class, int.class);
        
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

    @Test
    public void testAdd() {
        assertTrue(engine.shouldMigrate(add, l, 100));
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
        assertTrue(engine.shouldMigrate(remove, l, 0));
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
        assertTrue(engine.shouldMigrate(multiply, l, l0));
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
    
    @Test
    public void testSum() {
        TestSum obj = new TestSum();
        assertTrue(engine.shouldMigrate(sum, obj, 1, 2));
        int res = (Integer) engine.invokeRemotely(sum, obj, 1, 2);
        assertEquals(3, res);
    }
    
    @Test
    public void testStaticSum() {
        assertTrue(engine.shouldMigrate(sumStatic, null, 1, 2));
        int res = (Integer) engine.invokeRemotely(sumStatic, null, 1, 2);
        assertEquals(3, res);
    }
    
    @Test(expected = NullPointerException.class)
    public void testError() throws Throwable {
        assertTrue(engine.shouldMigrate(multiply, l, new Object[]{null}));
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
