package mobilecloud.test.objs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import mobilecloud.objs.ObjDiff;
import mobilecloud.objs.ObjectMigrator;
import mobilecloud.objs.ObjectVisitor;
import mobilecloud.objs.OnObjectVisitedListener;
import mobilecloud.objs.Token;
import mobilecloud.objs.Token.SnapShot;
import mobilecloud.utils.ClassUtils;
import mobilecloud.utils.IOUtils;

@SuppressWarnings("unchecked")
public class ObjectMigratorTest {
    
    private static class ListNode implements Serializable {
        private static final long serialVersionUID = 1L;
        public int val;
        public ListNode next;
        
        public ListNode(int val) {
            this.val = val;
        }
        
    }
    
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
        
        public void addNodes(ListNode[] nodes) {
            ListNode temp = new ListNode(0);
            temp.next = head;
            ListNode prev = temp;
            for(ListNode node = head; node != null; node = node.next) {
                prev = node;
            }
            for(ListNode node: nodes) {
                prev.next = node;
                prev = node;
                size ++;
            }
            prev.next = null;
            head = temp.next;
        }
    }
    
    private static class TestArray implements Serializable{
        private static final long serialVersionUID = 1L;

        public ListNode[] n;
    }
    
    private List l;
    private ListNode l0;
    private ListNode l1;
    private ListNode l2;
    private ListNode l3;
    private ObjectMigrator migrator;
    
    @Before
    public void setUp() {
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
        migrator = new ObjectMigrator();
    }
    
    private Object[] sendViaNetWork(Object... objs) throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(IOUtils.toInputStream(objs));
        return (Object[]) is.readObject();
    }
    
    private Map<Integer, ObjDiff> getDiffs(Token token, SnapShot snapshot) {
        return token.takeSnapShot().diff(snapshot);
    }
    
    private Token buildBackToken(Token token, Map<Integer, ObjDiff> diffs) {
        Token.Builder builder = new Token.Builder();
        ObjectVisitor visitor = new ObjectVisitor(new OnObjectVisitedListener() {
            @Override
            public boolean onObjectVisited(Object obj, Object array, int index) {
                if(ClassUtils.isPrimitiveArray(array.getClass())) {
                    return false;
                }
                Array.set(array, index, null);
                return true;
            }
            @Override
            public boolean onObjectVisited(Object obj, Object from, Field field) {
                int modifier = field.getModifiers();
                if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) {
                    // ignore static fields
                    return false;
                }
                if(field.getType().isPrimitive()) {
                    return false;
                }
                try {
                    field.set(from, null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        for(int id: diffs.keySet()) {
            builder.addObject(id, token.getObject(id));
            visitor.withObject(token.getObject(id));
        }
        Token backToken = builder.build();
        visitor.visitFields();
        return backToken;
    }
    

    @Test
    public void testToken() {
        migrator.migrate(l);
        Token token = migrator.takeToken();
        assertNotNull(token);
        assertEquals(5, token.size());
        assertTrue(token.contains(l));
        assertTrue(token.contains(l0));
        assertTrue(token.contains(l1));
        assertTrue(token.contains(l2));
        assertTrue(token.contains(l3));
    }
    
    @Test
    public void testToken1() {
        ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        migrator.migrate(nodes);
        Token token = migrator.takeToken();
        assertNotNull(token);
        assertEquals(5, token.size());
        assertTrue(token.contains(nodes));
        assertTrue(token.contains(l0));
        assertTrue(token.contains(l1));
        assertTrue(token.contains(l2));
        assertTrue(token.contains(l3));
    }
    
    @Test
    public void testSync1() throws ClassNotFoundException, IOException {
        migrator.migrate(l);
        Object[] cloudObjs = sendViaNetWork(l, migrator.takeToken());
        List cloudList = (List) cloudObjs[0];
        Token cloudToken = (Token) cloudObjs[1];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        cloudList.add(100);
        assertNotNull(cloudList.head.next.next.next.next);
        assertEquals(100, cloudList.head.next.next.next.next.val);
        assertEquals(5, cloudList.size);
        assertNull(l3.next);
        cloudToken = cloudToken.expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Token cloudBackToken = buildBackToken(cloudToken, cloudDiffs);
        Object[] cloudBackObjs = sendViaNetWork(cloudBackToken, cloudDiffs);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(backToken, backDiff);
        assertEquals(5, l.size);
        assertEquals(l.head, l0);
        assertEquals(prevL, l);
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
        assertNotNull(l3.next);
        assertEquals(100, l3.next.val);
    }
    
    @Test
    public void testSync2() throws ClassNotFoundException, IOException {
        migrator.migrate(l);
        Object[] cloudObjs = sendViaNetWork(l, migrator.takeToken());
        List cloudList = (List) cloudObjs[0];
        Token cloudToken = (Token) cloudObjs[1];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        cloudList.remove(0);
        assertEquals(3, cloudList.size);
        assertEquals(1, cloudList.head.val);
        assertEquals(4, l.size);
        assertEquals(0, l.head.val);
        cloudToken = cloudToken.expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Token cloudBackToken = buildBackToken(cloudToken, cloudDiffs);
        Object[] cloudBackObjs = sendViaNetWork(cloudBackToken, cloudDiffs);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(backToken, backDiff);
        assertEquals(3, l.size);
        assertEquals(l.head, l1);
        assertEquals(prevL, l);
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
    }
    
    @Test
    public void TestSync3() throws ClassNotFoundException, IOException {
        migrator.migrate(l0);
        Object[] cloudObjs = sendViaNetWork(l0, migrator.takeToken());
        ListNode cloudL0 = (ListNode) cloudObjs[0];
        Token cloudToken = (Token) cloudObjs[1];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        ListNode cloudL4 = new ListNode(100);
        cloudL0.next.next.next.next = cloudL0;
        cloudL4.next = cloudL0;
        assertNull(l3.next);
        cloudToken = new Token.Builder(cloudToken).addObject(cloudL4).build().expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Token cloudBackToken = buildBackToken(cloudToken, cloudDiffs);
        Object[] cloudBackObjs = sendViaNetWork(cloudBackToken, cloudDiffs);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(backToken, backDiff);
        ListNode l4 = (ListNode) backToken.getObject(4);
        assertEquals(100, l4.val);
        assertEquals(l0, l4.next);
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(prevL0, l0);
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(prevL1, l1);
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(prevL2, l2);
        assertEquals(3, l3.val);
        assertEquals(l0, l3.next);
        assertEquals(prevL3, l3);
        assertEquals(l0, l.head);
        assertEquals(4, l.size);
    }
    
    
    @Test
    public void testSync4() throws ClassNotFoundException, IOException {
        l.head = null;
        l.size = 0;
        ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        migrator.migrate(l);
        migrator.migrate(nodes);
        Object[] cloudObjs = sendViaNetWork(l, nodes, migrator.takeToken());
        List cloudList = (List) cloudObjs[0];
        ListNode[] cloudNodes = (ListNode[]) cloudObjs[1];
        Token cloudToken = (Token) cloudObjs[2];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        cloudList.addNodes(cloudNodes);
        cloudToken = cloudToken.expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Token cloudBackToken = buildBackToken(cloudToken, cloudDiffs);
        Object[] cloudBackObjs = sendViaNetWork(cloudBackToken, cloudDiffs);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(backToken, backDiff);
        assertEquals(4, l.size);
        assertEquals(l.head, l0);
        assertEquals(prevL, l);
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
        assertNull(l3.next);
        assertEquals(l0, nodes[0]);
        assertEquals(l1, nodes[1]);
        assertEquals(l2, nodes[2]);
        assertEquals(l3, nodes[3]);
    }
    
    @Test
    public void testSync5() throws ClassNotFoundException, IOException {
        final ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        TestArray t = new TestArray();
        t.n = nodes;
        migrator.migrate(t);
        Object[] cloudObjs = sendViaNetWork(t, migrator.takeToken());
        TestArray cloudArray = (TestArray) cloudObjs[0];
        Token cloudToken = (Token) cloudObjs[1];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        cloudArray.n[0] = cloudArray.n[1] = cloudArray.n[2] = cloudArray.n[3];
        cloudToken = cloudToken.expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Token cloudBackToken = buildBackToken(cloudToken, cloudDiffs);
        Object[] cloudBackObjs = sendViaNetWork(cloudBackToken, cloudDiffs);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        migrator.sync(backToken, backDiff);
        assertEquals(l3, t.n[0]);
        assertEquals(l3, t.n[1]);
        assertEquals(l3, t.n[2]);
        assertEquals(l3, t.n[3]);
        assertEquals(l3, nodes[0]);
        assertEquals(l3, nodes[1]);
        assertEquals(l3, nodes[2]);
        assertEquals(l3, nodes[3]);
        assertTrue(nodes == t.n);
    }
    
    @Test
    public void testSync6() throws ClassNotFoundException, IOException {
        final ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        TestArray t = new TestArray();
        migrator.migrate(t);
        migrator.migrate(nodes);
        Object[] cloudObjs = sendViaNetWork(t, nodes, migrator.takeToken());
        TestArray cloudArray = (TestArray) cloudObjs[0];
        ListNode[] cloudNodes = (ListNode[]) cloudObjs[1];
        Token cloudToken = (Token) cloudObjs[2];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        cloudArray.n = cloudNodes;
        cloudArray.n[0] = cloudArray.n[1] = cloudArray.n[2] = cloudArray.n[3];
        cloudToken = cloudToken.expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Token cloudBackToken = buildBackToken(cloudToken, cloudDiffs);
        Object[] cloudBackObjs = sendViaNetWork(cloudBackToken, cloudDiffs);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        migrator.sync(backToken, backDiff);
        assertEquals(l3, t.n[0]);
        assertEquals(l3, t.n[1]);
        assertEquals(l3, t.n[2]);
        assertEquals(l3, t.n[3]);
        assertEquals(l3, nodes[0]);
        assertEquals(l3, nodes[1]);
        assertEquals(l3, nodes[2]);
        assertEquals(l3, nodes[3]);
        assertTrue(nodes == t.n);
    }
    
    @Test
    public void testGetObject() throws ClassNotFoundException, IOException {
        migrator.migrate(l);
        Token token = migrator.takeToken();
        Object[] cloudObjs = sendViaNetWork(l, token);
        List cloudList = (List) cloudObjs[0];
        Token cloudToken = (Token) cloudObjs[1];
        SnapShot cloudSnapShot = cloudToken.takeSnapShot();
        cloudList.add(100);
        cloudToken = cloudToken.expand();
        Map<Integer, ObjDiff> cloudDiffs = getDiffs(cloudToken, cloudSnapShot);
        Object[] cloudBackObjs = sendViaNetWork(cloudToken, cloudDiffs, cloudList);
        Token backToken = (Token) cloudBackObjs[0];
        Map<Integer, ObjDiff> backDiff = (Map<Integer, ObjDiff>) cloudBackObjs[1];
        List backList = (List) cloudBackObjs[2];
        migrator.sync(backToken, backDiff);
        assertEquals(l, migrator.getObject(backList));
        assertEquals(l0, migrator.getObject(backList.head));
        assertEquals(l1, migrator.getObject(backList.head.next));
        assertEquals(l2, migrator.getObject(backList.head.next.next));
        assertEquals(l3, migrator.getObject(backList.head.next.next.next));
        assertEquals(l3.next, migrator.getObject(backList.head.next.next.next.next));
        assertEquals(100, l3.next.val);
    }
    
}
