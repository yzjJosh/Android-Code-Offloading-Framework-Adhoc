package mobilecloud.test.engine;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.Context;

import org.mockito.Matchers;

import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.api.UploadApplicationExecutableResponse;
import mobilecloud.client.Client;
import mobilecloud.engine.Engine;
import mobilecloud.engine.RemoteExecutionFailedException;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.schedular.Schedular;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.Server;
import mobilecloud.utils.ByteProvider;

public class EngineTest {
    
    private static class ListNode implements Serializable {
        
        private static final long serialVersionUID = 1L;
        public int val;
        public ListNode next;
        
        public ListNode(int val) {
            this.val = val;
        }
    }
    
    @SuppressWarnings("unused")
    private static class List implements Serializable {
        private static final long serialVersionUID = 1L;
        public ListNode head;
        public int size;
        
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
        
        public boolean contains(ListNode target) {
            ListNode node = head;
            while(node != null) {
                if(node == target) {
                    return true;
                }
                node = node.next;
            }
            return false;
        }
        
        public void concat(Collection<List> lists) {
            ListNode temp = new ListNode(0);
            temp.next = head;
            ListNode prev = temp;
            for(ListNode node = head; node != null; node = node.next) {
                prev = node;
            }
            for(List list: lists) {
                for(ListNode node = list.head; node != null; node = node.next) {
                    prev = prev.next = node;
                    size ++;
                }
            }
            prev.next = null;
            head = temp.next;
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
    private Method contains;
    private Method concat;
    
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
                if (req instanceof UploadApplicationExecutableRequest) {
                    UploadApplicationExecutableRequest up = (UploadApplicationExecutableRequest) req;
                    server.registerClassLoader(up.getApplicationId(), ClassLoader.getSystemClassLoader());
                    return new UploadApplicationExecutableResponse().setSuccess(true);
                } else if (req instanceof RemoteInvocationRequest) {
                    return server.serve(req);
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
        contains = List.class.getMethod("contains", ListNode.class);
        concat = List.class.getMethod("concat", Collection.class);
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

    @Test
    public void testAdd() {
        assertTrue(engine.shouldMigrate(add, l, 100));
        engine.invokeRemotely(add, l, 100);
        assertEquals(5, l.size);
        assertEquals(l0, l.head);
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(3, l3.val);
        assertNotNull(l3.next);
        assertEquals(100, l3.next.val);
        assertNull(l3.next.next);
    }
    
    @Test
    public void testRemove() {
        assertTrue(engine.shouldMigrate(remove, l, 0));
        engine.invokeRemotely(remove, l, 0);
        assertEquals(3, l.size);
        assertEquals(l1, l.head);
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(3, l3.val);
        assertNull(l3.next);
    }
    
    @Test
    public void testMultiply() {
        assertTrue(engine.shouldMigrate(multiply, l, l0));
        List list = (List) engine.invokeRemotely(multiply, l, 10);
        assertEquals(4, l.size);
        assertEquals(l0, l.head);
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(3, l3.val);
        assertNull(l3.next);
        assertEquals(4, list.size);
        assertNotNull(list.head);
        ListNode n0 = list.head;
        assertEquals(0, n0.val);
        assertNotNull(n0.next);
        ListNode n1 = n0.next;
        assertEquals(10, n1.val);
        assertNotNull(n1.next);
        ListNode n2 = n1.next;
        assertEquals(20, n2.val);
        assertNotNull(n2.next);
        ListNode n3 = n2.next;
        assertEquals(30, n3.val);
        assertNull(n3.next);
    }
    
    @Test
    public void testContains() {
        assertTrue(engine.shouldMigrate(contains, l, l2));
        boolean res = (Boolean) engine.invokeRemotely(contains, l, l2);
        assertTrue(res);
        assertEquals(4, l.size);
        assertEquals(l0, l.head);
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(3, l3.val);
        assertNull(l3.next);
    }
    
    @Test
    public void testConcat() {
        java.util.List<List> lists = new ArrayList<>();
        for(int i = 0; i<4; i++) {
            lists.add(new List());
        }
        lists.get(0).head = l0;
        l0.next = null;
        lists.get(1).head = l1;
        l1.next = null;
        lists.get(2).head = l2;
        l2.next = null;
        lists.get(3).head = l3;
        l3.next = null;
        l.head = null;
        l.size = 0;
        assertTrue(engine.shouldMigrate(concat, l, lists));
        engine.invokeRemotely(concat, l, lists);
        assertEquals(4, l.size);
        assertEquals(l0, l.head);
        assertEquals(l1, l0.next);
        assertEquals(l2, l1.next);
        assertEquals(l3, l2.next);
        assertNull(l3.next);
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
            assertEquals(4, l.size);
            assertEquals(l0, l.head);
            assertEquals(0, l0.val);
            assertEquals(l1, l0.next);
            assertEquals(1, l1.val);
            assertEquals(l2, l1.next);
            assertEquals(2, l2.val);
            assertEquals(l3, l2.next);
            assertEquals(3, l3.val);
            assertNull(l3.next);
            throw e.getCause();
        }
        fail();
    }
}
