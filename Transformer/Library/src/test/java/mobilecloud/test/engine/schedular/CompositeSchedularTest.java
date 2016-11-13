package mobilecloud.test.engine.schedular;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.schedular.CompositeSchedular;
import mobilecloud.engine.schedular.Schedular;
import mobilecloud.metric.Metric;

public class CompositeSchedularTest {
	private final int hostNum = 12;
	private Host[] hosts = new Host[hostNum];
	private double[] cpuLoad;
	private Schedular schedular = new CompositeSchedular();
	private int[] count;
	
	@Before 
	public void setUp() {
		count = new int[hostNum];
		cpuLoad = new double[hostNum];
		for(int i=0; i<hostNum; i++) {
			hosts[i] = new Host("192.168.1.1", i);
			schedular.addHost(hosts[i]);
			schedular.updateMetric(hosts[i], new Metric(0, 0, 0, 0));
		}
	}
	
	@Test
	public void compositeSchedularTest() {
		System.out.println("Test1");
		for(int i=0; i<10000; i++) {
			Host host = schedular.schedule();
			cpuLoad[host.port] += 0.05;
			count[host.port]++;
			schedular.updateMetric(host, new Metric(0, 0, cpuLoad[host.port], 0));
		}
	}
	
	@Test
	public void compositeSchedularTest2() {
		System.out.println("Test2");
		for(int i=0; i<10000; i++) {
			Host host = schedular.schedule();
			cpuLoad[host.port] += 0.05;
			count[host.port]++;
			if(i%200==0) {
				for(int j=0; j<hostNum; j++) {
					schedular.updateMetric(host, new Metric(0, 0, cpuLoad[j], 0));
				}
			}	
		}
	}
	
	@After
	public void tearDown() {
		System.out.println(Arrays.toString(count));
	}
}
