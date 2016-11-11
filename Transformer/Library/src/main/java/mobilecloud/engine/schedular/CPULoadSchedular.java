package mobilecloud.engine.schedular;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import mobilecloud.engine.host.Host;
import mobilecloud.metric.Metric;

public class CPULoadSchedular extends Schedular {
	private static class HostWrapper implements Comparator<HostWrapper> {
		public Host host;
		public Metric metric;

		public HostWrapper(Host host, Metric metric) {
			this.host = host;
			this.metric = metric;
		}

		@Override
		public int compare(HostWrapper a, HostWrapper b) {
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
	}

	TreeSet<HostWrapper> queue = new TreeSet<>();
	Map<Host, HostWrapper> map = new HashMap<>();

	@Override
	public Host schedule() {
		if (!haveAvailable())
			return null;
		return queue.first().host;
	}

	@Override
	public Host trySchedule() {
		return schedule();
	}

	@Override
	public int availableNum() {
		return queue.size();
	}

	@Override
	public boolean haveAvailable() {
		return availableNum() != 0;
	}

	@Override
	public void addHost(Host host) {
		if (map.containsKey(host))
			return;
		HostWrapper hostWrapper = new HostWrapper(host, null);
		queue.add(hostWrapper);
		map.put(host, hostWrapper);
	}

	@Override
	public void removeHost(Host host) {
		if (!map.containsKey(host))
			return;
		HostWrapper hostWrapper = map.remove(host);
		queue.remove(hostWrapper);
	}

	@Override
	public void updateMetric(Host host, Metric metric) {
		HostWrapper hostWrapper = map.get(host);
		if (hostWrapper == null) {
			hostWrapper = new HostWrapper(host, metric);
			queue.add(hostWrapper);
			map.put(host, hostWrapper);
		} else {
			queue.remove(hostWrapper);
			hostWrapper.metric = metric;
			queue.add(hostWrapper);
		}
	}
}
