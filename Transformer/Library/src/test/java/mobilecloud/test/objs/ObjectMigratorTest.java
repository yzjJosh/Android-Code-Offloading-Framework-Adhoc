package mobilecloud.test.objs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mobilecloud.lib.Remotable;
import mobilecloud.objs.ObjDiff;
import mobilecloud.objs.ObjectMigrator;
import mobilecloud.objs.ObjectVisitor;
import mobilecloud.objs.OnObjectVisitedListener;
import mobilecloud.objs.Token;
import mobilecloud.objs.Token.SnapShot;
import mobilecloud.utils.ClassUtils;

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
        ObjectInputStream is = new ObjectInputStream(ClassUtils.toInputStream(objs));
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
                Array.set(array, index, null);
                return true;
            }
            @Override
            public boolean onObjectVisited(Object obj, Object from, Field field) {
                int modifier = field.getModifiers();
                if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier) || Modifier.isFinal(modifier)) {
                    // ignore static and transient fields
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
    
 /*   @Test
    public void testSync2() throws ClassNotFoundException, IOException {
        migrator.moveOut(l);
        List cloud = (List) sendViaNetWork(l);
        cloud.remove(0);
        assertEquals(3, cloud.size);
        assertEquals(1, cloud.head.val);
        assertEquals(4, l.size);
        assertEquals(0, l.head.val);
        List cloudBack = (List) sendViaNetWork(cloud);
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(cloudBack);
        assertTrue(l.isOnServer());
        assertFalse(l.isNew());
        assertEquals(3, l.size);
        assertEquals(l.head, l1);
        assertEquals(prevL, l);
        assertTrue(l0.isOnServer());
        assertFalse(l0.isNew());
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertTrue(l1.isOnServer());
        assertFalse(l1.isNew());
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertTrue(l2.isOnServer());
        assertFalse(l2.isNew());
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertTrue(l3.isOnServer());
        assertFalse(l3.isNew());
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
    }
    
    @Test
    public void TestSync3() throws ClassNotFoundException, IOException {
        migrator.moveOut(l0);
        ListNode cloud = (ListNode) sendViaNetWork(l0);
        ListNode cloudL4 = new ListNode(100);
        cloudL4.setIsNew(true);
        cloudL4.setIsOnServer(true);
        cloud.next.next.next.next = cloud;
        cloudL4.next = cloud;
        assertNull(l3.next);
        ListNode cloudBack = (ListNode) sendViaNetWork(cloudL4);
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        ListNode l4 = (ListNode) migrator.sync(cloudBack);
        assertTrue(l4.isNew());
        assertTrue(l4.isOnServer());
        assertEquals(100, l4.val);
        assertEquals(l0, l4.next);
        assertTrue(l0.isOnServer());
        assertFalse(l0.isNew());
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(prevL0, l0);
        assertTrue(l1.isOnServer());
        assertFalse(l1.isNew());
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(prevL1, l1);
        assertTrue(l2.isOnServer());
        assertFalse(l2.isNew());
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(prevL2, l2);
        assertTrue(l3.isOnServer());
        assertFalse(l3.isNew());
        assertEquals(3, l3.val);
        assertEquals(l0, l3.next);
        assertEquals(prevL3, l3);
        assertFalse(l.isOnServer());
        assertFalse(l.isNew());
        assertEquals(l0, l.head);
        assertEquals(4, l.size);
    }
    
    @Test
    public void testSync4 () throws ClassNotFoundException, IOException {
        l.setIsNew(true);
        l.setIsOnServer(true);
        l0.setIsNew(true);
        l0.setIsOnServer(true);
        l1.setIsNew(true);
        l1.setIsOnServer(true);
        l2.setIsNew(true);
        l2.setIsOnServer(true);
        l3.setIsNew(true);
        l3.setIsOnServer(true);
        l3.next = l0;
        List cloudBack = (List) sendViaNetWork(l);
        migrator.sync(cloudBack);
        List backL = cloudBack;
        ListNode back0 = cloudBack.head;
        ListNode back1 = back0.next;
        ListNode back2 = back1.next;
        ListNode back3 = back2.next;
        assertTrue(backL.isNew());
        assertTrue(backL.isOnServer());
        assertTrue(back0.isNew());
        assertTrue(back0.isOnServer());
        assertEquals(0, back0.val);
        assertEquals(back1, back0.next);
        assertTrue(back1.isNew());
        assertTrue(back1.isOnServer());
        assertEquals(1, back1.val);
        assertEquals(back2, back1.next);
        assertTrue(back2.isNew());
        assertTrue(back2.isOnServer());
        assertEquals(2, back2.val);
        assertEquals(back3, back2.next);
        assertTrue(back3.isNew());
        assertTrue(back3.isOnServer());
        assertEquals(3, back3.val);
        assertEquals(back0, back3.next);
    }
    
    @Test
    public void testSync5() throws ClassNotFoundException, IOException {
        l.head = null;
        l.size = 0;
        ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        migrator.moveOut(l);
        migrator.moveOut(nodes);
        List cloud = (List) sendViaNetWork(l);
        ListNode[] cloudNodes = (ListNode[]) sendViaNetWork(nodes);
        cloud.addNodes(cloudNodes);
        List cloudBack = (List) sendViaNetWork(cloud);
        ListNode[] cloudBackNodes = (ListNode[]) sendViaNetWork(cloudNodes);
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        List l = (List) migrator.sync(cloudBack);
        nodes = (ListNode[]) migrator.sync(cloudBackNodes);
        assertTrue(l.isOnServer());
        assertFalse(l.isNew());
        assertEquals(4, l.size);
        assertEquals(l.head, l0);
        assertEquals(prevL, l);
        assertTrue(l0.isOnServer());
        assertFalse(l0.isNew());
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertTrue(l1.isOnServer());
        assertFalse(l1.isNew());
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertTrue(l2.isOnServer());
        assertFalse(l2.isNew());
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertTrue(l3.isOnServer());
        assertFalse(l3.isNew());
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
        assertNull(l3.next);
        assertEquals(l0, cloudBackNodes[0]);
        assertEquals(l1, cloudBackNodes[1]);
        assertEquals(l2, cloudBackNodes[2]);
        assertEquals(l3, cloudBackNodes[3]);
    }
    
    @Test
    public void testSync6() throws ClassNotFoundException, IOException {
        final ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        
        TestArray t = new TestArray();
        t.n = nodes;
        migrator.moveOut(t);
        TestArray cloud = (TestArray) sendViaNetWork(t);
        cloud.n[0] = cloud.n[1] = cloud.n[2] = cloud.n[3];
        TestArray cloudBack = (TestArray) sendViaNetWork(cloud);
        migrator.sync(cloudBack);
        assertEquals(l3, t.n[0]);
        assertEquals(l3, t.n[1]);
        assertEquals(l3, t.n[2]);
        assertEquals(l3, t.n[3]);
        assertEquals(l3, nodes[0]);
        assertEquals(l3, nodes[1]);
        assertEquals(l3, nodes[2]);
        assertEquals(l3, nodes[3]);
    }
    
    @Test
    public void testJoinObjects1() {
        migrator.moveOut(l);
        migrator.joinObjects();
        assertFalse(l.isNew());
        assertFalse(l.isOnServer());
        assertFalse(l0.isNew());
        assertFalse(l0.isOnServer());
        assertFalse(l1.isNew());
        assertFalse(l1.isOnServer());
        assertFalse(l2.isNew());
        assertFalse(l2.isOnServer());
        assertFalse(l3.isNew());
        assertFalse(l3.isOnServer());
    }

    @Test
    public void testJoinObjects2() throws ClassNotFoundException, IOException {
        migrator.moveOut(l);
        List cloud = (List) sendViaNetWork(l);
        cloud.add(100);
        assertNotNull(cloud.head.next.next.next.next);
        assertEquals(100, cloud.head.next.next.next.next.val);
        assertEquals(5, cloud.size);
        cloud.head.next.next.next.next.setIsNew(true);
        cloud.head.next.next.next.next.setIsOnServer(true);
        assertNull(l3.next);
        List cloudBack = (List)sendViaNetWork(cloud);
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(cloudBack);
        migrator.joinObjects();
        assertFalse(l.isOnServer());
        assertFalse(l.isNew());
        assertEquals(5, l.size);
        assertEquals(l.head, l0);
        assertEquals(prevL, l);
        assertFalse(l0.isOnServer());
        assertFalse(l0.isNew());
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertFalse(l1.isOnServer());
        assertFalse(l1.isNew());
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertFalse(l2.isOnServer());
        assertFalse(l2.isNew());
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertFalse(l3.isOnServer());
        assertFalse(l3.isNew());
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
        assertNotNull(l3.next);
        assertEquals(100, l3.next.val);
        assertFalse(l3.next.isOnServer());
        assertFalse(l3.next.isNew());
    }
    
    @Test
    public void testJoinObjects3() throws ClassNotFoundException, IOException {
        migrator.moveOut(l);
        List cloud = (List) sendViaNetWork(l);
        cloud.remove(0);
        assertEquals(3, cloud.size);
        assertEquals(1, cloud.head.val);
        assertEquals(4, l.size);
        assertEquals(0, l.head.val);
        List cloudBack = (List) sendViaNetWork(cloud);
        List prevL = l;
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        migrator.sync(cloudBack);
        migrator.joinObjects();
        assertFalse(l.isOnServer());
        assertFalse(l.isNew());
        assertEquals(3, l.size);
        assertEquals(l.head, l1);
        assertEquals(prevL, l);
        assertFalse(l0.isOnServer());
        assertFalse(l0.isNew());
        assertEquals(0, l0.val);
        assertEquals(l0.next, l1);
        assertEquals(prevL0, l0);
        assertFalse(l1.isOnServer());
        assertFalse(l1.isNew());
        assertEquals(1, l1.val);
        assertEquals(l1.next, l2);
        assertEquals(prevL1, l1);
        assertFalse(l2.isOnServer());
        assertFalse(l2.isNew());
        assertEquals(2, l2.val);
        assertEquals(l2.next, l3);
        assertEquals(prevL2, l2);
        assertFalse(l3.isOnServer());
        assertFalse(l3.isNew());
        assertEquals(3, l3.val);
        assertEquals(prevL3, l3);
    }
    
    @Test
    public void TestJoinObjects5() throws ClassNotFoundException, IOException {
        migrator.moveOut(l0);
        ListNode cloud = (ListNode) sendViaNetWork(l0);
        ListNode cloudL4 = new ListNode(100);
        cloudL4.setIsNew(true);
        cloudL4.setIsOnServer(true);
        cloud.next.next.next.next = cloud;
        cloudL4.next = cloud;
        assertNull(l3.next);
        ListNode cloudBack = (ListNode) sendViaNetWork(cloudL4);
        ListNode prevL0 = l0;
        ListNode prevL1 = l1;
        ListNode prevL2 = l2;
        ListNode prevL3 = l3;
        ListNode l4 = (ListNode) migrator.sync(cloudBack);
        migrator.joinObjects();
        assertFalse(l4.isNew());
        assertFalse(l4.isOnServer());
        assertEquals(100, l4.val);
        assertEquals(l0, l4.next);
        assertFalse(l0.isOnServer());
        assertFalse(l0.isNew());
        assertEquals(0, l0.val);
        assertEquals(l1, l0.next);
        assertEquals(prevL0, l0);
        assertFalse(l1.isOnServer());
        assertFalse(l1.isNew());
        assertEquals(1, l1.val);
        assertEquals(l2, l1.next);
        assertEquals(prevL1, l1);
        assertFalse(l2.isOnServer());
        assertFalse(l2.isNew());
        assertEquals(2, l2.val);
        assertEquals(l3, l2.next);
        assertEquals(prevL2, l2);
        assertFalse(l3.isOnServer());
        assertFalse(l3.isNew());
        assertEquals(3, l3.val);
        assertEquals(l0, l3.next);
        assertEquals(prevL3, l3);
        assertFalse(l.isOnServer());
        assertFalse(l.isNew());
        assertEquals(l0, l.head);
        assertEquals(4, l.size);
    }
    
    @Test
    public void testJoinObjects6 () throws ClassNotFoundException, IOException {
        l.setIsNew(true);
        l.setIsOnServer(true);
        l0.setIsNew(true);
        l0.setIsOnServer(true);
        l1.setIsNew(true);
        l1.setIsOnServer(true);
        l2.setIsNew(true);
        l2.setIsOnServer(true);
        l3.setIsNew(true);
        l3.setIsOnServer(true);
        l3.next = l0;
        List cloudBack = (List) sendViaNetWork(l);
        migrator.sync(cloudBack);
        migrator.joinObjects();
        List backL = cloudBack;
        ListNode back0 = cloudBack.head;
        ListNode back1 = back0.next;
        ListNode back2 = back1.next;
        ListNode back3 = back2.next;
        assertFalse(backL.isNew());
        assertFalse(backL.isOnServer());
        assertFalse(back0.isNew());
        assertFalse(back0.isOnServer());
        assertEquals(0, back0.val);
        assertEquals(back1, back0.next);
        assertFalse(back1.isNew());
        assertFalse(back1.isOnServer());
        assertEquals(1, back1.val);
        assertEquals(back2, back1.next);
        assertFalse(back2.isNew());
        assertFalse(back2.isOnServer());
        assertEquals(2, back2.val);
        assertEquals(back3, back2.next);
        assertFalse(back3.isNew());
        assertFalse(back3.isOnServer());
        assertEquals(3, back3.val);
        assertEquals(back0, back3.next);
    }
    
    @Test
    public void testJoinObjects7() {
        ListNode[] nodes = new ListNode[]{l0, l1, l2, l3};
        migrator.moveOut(nodes);
        migrator.joinObjects();
        assertFalse(l0.isNew());
        assertFalse(l0.isOnServer());
        assertFalse(l1.isNew());
        assertFalse(l1.isOnServer());
        assertFalse(l2.isNew());
        assertFalse(l2.isOnServer());
        assertFalse(l3.isNew());
        assertFalse(l3.isOnServer());
    } */

}
