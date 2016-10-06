package mobilecloud.test.engine.schedular;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import mobilecloud.engine.host.Host;
import mobilecloud.engine.schedular.RoundRobinSchedular;
import mobilecloud.engine.schedular.Schedular;

public class RoundRobinSchedularTest {
    
    private Host h0 = new Host("192.168.1.1", 0);
    private Host h1 = new Host("192.168.1.1", 1);
    private Host h2 = new Host("192.168.1.1", 2);
    private Host h3 = new Host("192.168.1.1", 3);
    private Schedular schedular = new RoundRobinSchedular();
    
    @Before
    public void setUp() {
        schedular.addHost(h0);
        schedular.addHost(h1);
        schedular.addHost(h2);
        schedular.addHost(h3);
    }
    
    @Test
    public void testSchedule() {
        assertEquals(h0, schedular.schedule());
        assertEquals(h1, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
        assertEquals(h0, schedular.schedule());
        assertEquals(h1, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
        assertEquals(h0, schedular.schedule());
        assertEquals(h1, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
    }
    
    @Test
    public void testNum() {
        assertTrue(schedular.haveAvailable());
        assertEquals(4, schedular.availableNum());
    }
    
    @Test
    public void testAdd() {
        Host h4 = new Host("192.168.1.1", 4);
        schedular.addHost(h4);
        assertEquals(5, schedular.availableNum());
        assertTrue(schedular.haveAvailable());
        assertEquals(h0, schedular.schedule());
        assertEquals(h1, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
        assertEquals(h4, schedular.schedule());
        assertEquals(h0, schedular.schedule());
        assertEquals(h1, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
        assertEquals(h4, schedular.schedule());
    }
    
    @Test
    public void testRemove() {
        schedular.removeHost(h1);
        assertEquals(3, schedular.availableNum());
        assertTrue(schedular.haveAvailable());
        assertEquals(h0, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
        assertEquals(h0, schedular.schedule());
        assertEquals(h2, schedular.schedule());
        assertEquals(h3, schedular.schedule());
    }

}
