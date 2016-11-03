package mobilecloud.test.engine.host.monitor;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.Matchers;

import mobilecloud.api.request.Request;
import mobilecloud.api.response.MonitorHostResponse;
import mobilecloud.api.response.Response;
import mobilecloud.client.Client;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.monitor.HostMonitor;
import mobilecloud.engine.host.monitor.HostStatusChangeListener;
import mobilecloud.engine.host.provider.HostProvider;

public class HostMonitorTest implements HostProvider{
    
    private Host host0 = new Host("192.168.0.0", 0);
    private Host host1 = new Host("192.168.0.1", 0);
    private Host host2 = new Host("192.168.0.2", 0);
    private Queue<Host> hosts = new ConcurrentLinkedQueue<>();
    private HostMonitor monitor;
    private Client client;
    
    @Before
    public void setUp() {
        client = Mockito.mock(Client.class);
        monitor = new HostMonitor(this, client).withCheckHostInterval(0).withCheckProviderInterval(0);
    }

    @Override
    public List<Host> provide() {
        return new LinkedList<>(hosts);
    }
    
    @Test(timeout = 10000)
    public void testMonitorWithDynamicHostStatus() throws Exception {
        hosts.add(host0);
        hosts.add(host1);
        
        final Map<Host, Response> map = new ConcurrentHashMap<>();
        map.put(host0, new MonitorHostResponse().setSuccess(true));
        map.put(host1, new MonitorHostResponse().setSuccess(false).setThrowable(new RuntimeException()));

        Mockito.when(client.request(Matchers.any(Request.class))).thenAnswer(new Answer<Response>() {
            @Override
            public Response answer(InvocationOnMock invocation) throws Throwable {
                Request request = (Request) invocation.getArguments()[0];
                return map.get(new Host(request.getIp(), request.getPort()));
            }
        });

        final int[] count = new int[] { 0 };
        monitor.withHostStatusChangeListener(new HostStatusChangeListener() {
            @Override
            public void onHostStatusChange(Host host, boolean isAlive) {
                assertNotNull(host);
                assertEquals(isAlive, map.get(host).isSuccess());
                synchronized (count) {
                    count[0] ++;
                    count.notifyAll();
                }
            }
        });

        monitor.start();
        synchronized (count) {
            while (count[0] < 2) {
                count.wait();
            }
        }
        assertEquals(2, count[0]);
        
        map.put(host0, new MonitorHostResponse().setSuccess(false));
        synchronized (count) {
            while (count[0] < 3) {
                count.wait();
            }
        }
        assertEquals(3, count[0]);
        
        map.put(host1, new MonitorHostResponse().setSuccess(true));
        synchronized (count) {
            while (count[0] < 4) {
                count.wait();
            }
        }
        assertEquals(4, count[0]);
        
        monitor.stop();
        assertEquals(4, count[0]);
    }
    
    @Test(timeout = 10000)
    public void testMonitorWithDynamicProvider() throws Exception {
        final Map<Host, Response> map = new ConcurrentHashMap<>();
        map.put(host0, new MonitorHostResponse().setSuccess(true));
        map.put(host1, new MonitorHostResponse().setSuccess(false).setThrowable(new RuntimeException()));
        map.put(host2, new MonitorHostResponse().setSuccess(true));
        
        Mockito.when(client.request(Matchers.any(Request.class))).thenAnswer(new Answer<Response>() {
            @Override
            public Response answer(InvocationOnMock invocation) throws Throwable {
                Request request = (Request) invocation.getArguments()[0];
                return map.get(new Host(request.getIp(), request.getPort()));
            }
        });
        
        final int[] count = new int[] { 0 };
        monitor.withHostStatusChangeListener(new HostStatusChangeListener() {
            @Override
            public void onHostStatusChange(Host host, boolean isAlive) {
                assertNotNull(host);
                assertEquals(isAlive, map.get(host).isSuccess());
                synchronized (count) {
                    count[0] ++;
                    count.notifyAll();
                }
            }
        });
        
        monitor.start();
        assertEquals(0, count[0]);
        
        hosts.add(host0);
        synchronized (count) {
            while (count[0] < 1) {
                count.wait();
            }
        }
        assertEquals(1, count[0]);
        
        hosts.add(host1);
        synchronized (count) {
            while (count[0] < 2) {
                count.wait();
            }
        }
        assertEquals(2, count[0]);
        
        hosts.add(host2);
        synchronized (count) {
            while (count[0] < 3) {
                count.wait();
            }
        }
        assertEquals(3, count[0]);
        
        monitor.withHostStatusChangeListener(new HostStatusChangeListener() {
            @Override
            public void onHostStatusChange(Host host, boolean isAlive) {
                assertNotNull(host);
                assertEquals(host, host0);
                assertFalse(isAlive);
                synchronized (count) {
                    count[0] ++;
                    count.notifyAll();
                }
            }
        });
        
        hosts.poll();
        synchronized (count) {
            while (count[0] < 4) {
                count.wait();
            }
        }
        assertEquals(4, count[0]);
        
        monitor.withHostStatusChangeListener(new HostStatusChangeListener() {
            @Override
            public void onHostStatusChange(Host host, boolean isAlive) {
                assertNotNull(host);
                assertEquals(host, host0);
                assertTrue(isAlive);
                synchronized (count) {
                    count[0] ++;
                    count.notifyAll();
                }
            }
        });
        
        hosts.add(host0);
        synchronized (count) {
            while (count[0] < 5) {
                count.wait();
            }
        }
        assertEquals(5, count[0]);
        
        monitor.stop();
        assertEquals(5, count[0]);
    }

}
