package mobilecloud.engine.schedular;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mobilecloud.engine.host.Host;
import mobilecloud.metric.Metric;
import mobilecloud.utils.RandomizedSet;

public class CPULoadSchedular extends Schedular {
	private static class HostWrapper implements Comparable<HostWrapper> {
		public Host host;
		public Metric metric;

		public HostWrapper(Host host, Metric metric) {
			this.host = host;
			this.metric = metric;
		}

		@Override
		public int compareTo(HostWrapper b) {
		    HostWrapper a = this;
			Metric m1 = a.metric;
			Metric m2 = b.metric;
			if (m1 == null && m2 == null) {
				return -1;
			} else if (m1 == null) {
				return 1;
			} else if (m2 == null) {
				return -1;
			} else {
				if (m1.cpuLoadPercentage != m2.cpuLoadPercentage) {
					Double d1 = new Double(m1.cpuLoadPercentage);
					Double d2 = new Double(m2.cpuLoadPercentage);
					return d1.compareTo(d2);
				} else if (m1.requestPerSecond != m2.requestPerSecond) {
					Double d1 = new Double(m1.requestPerSecond);
					Double d2 = new Double(m2.requestPerSecond);
					return d1.compareTo(d2);
				} else {
					int readAndWritePerSecond1 = m1.readBPS + m1.writeBPS;
					int readAndWritePerSecond2 = m2.readBPS + m2.writeBPS;
					return readAndWritePerSecond1 - readAndWritePerSecond2;
				}
			}
		}
		
		@Override
		public String toString() {
		    return host + " | " + metric;
		}
	}

	private RandomizedSet<HostWrapper> set = new RandomizedSet<>();
	private Map<Host, HostWrapper> map = new HashMap<>();

	@Override
	public synchronized Host schedule() {
		if (!haveAvailable())
			return null;
		
		List<HostWrapper> res = set.sample(Config.CPU_LOAD_SCHEDULAR_SAMPLE_SIZE);
		HostWrapper wrapper = null;
		for(HostWrapper hostWrapper : res) {
			if(wrapper!=null) {
				if(hostWrapper.compareTo(wrapper) < 0) {
					wrapper = hostWrapper;
				}
			} else {
				wrapper = hostWrapper;
			}
			
		}
		return wrapper.host;
	}

	@Override
	public synchronized Host trySchedule() {
		return schedule();
	}

	@Override
	public synchronized int availableNum() {
		return set.size();
	}

	@Override
	public synchronized boolean haveAvailable() {
		return availableNum() != 0;
	}

	@Override
	public synchronized void addHost(Host host) {
		if (map.containsKey(host))
			return;
		HostWrapper hostWrapper = new HostWrapper(host, null);
		set.add(hostWrapper);
		map.put(host, hostWrapper);
	}

	@Override
	public synchronized void removeHost(Host host) {
		if (!map.containsKey(host))
			return;
		HostWrapper hostWrapper = map.remove(host);
		set.remove(hostWrapper);
	}

	@Override
	public synchronized void updateMetric(Host host, Metric metric) {
		HostWrapper hostWrapper = map.get(host);
		if (hostWrapper == null) {
			hostWrapper = new HostWrapper(host, metric);
			set.add(hostWrapper);
			map.put(host, hostWrapper);
		} else {
			set.remove(hostWrapper);
			hostWrapper.metric = metric;
			set.add(hostWrapper);
		}
	}
}
