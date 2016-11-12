package mobilecloud.engine.schedular;

import mobilecloud.engine.host.Host;
import mobilecloud.metric.Metric;

public class CompositeSchedular extends Schedular{
	
	private CPULoadSchedular cLoadSchedular = new CPULoadSchedular();
	private RoundRobinSchedular rSchedular = new RoundRobinSchedular();
	
	@Override
	public synchronized Host schedule() {
		Host host1 = cLoadSchedular.schedule();
		Host host2 = rSchedular.schedule();
		if(availableNum()>Config.COMPOSITE_SCHEDULAR_THRESHOLD) {
			return host1;
		} else {
			return host2;
		}
	}

	@Override
	public synchronized Host trySchedule() {
		if(availableNum()>Config.COMPOSITE_SCHEDULAR_THRESHOLD) {
			return cLoadSchedular.trySchedule();
		} else {
			return rSchedular.trySchedule();
		}
	}

	@Override
	public synchronized int availableNum() {
		return cLoadSchedular.availableNum();
	}

	@Override
	public synchronized boolean haveAvailable() {
		return cLoadSchedular.haveAvailable();
	}

	@Override
	public synchronized void addHost(Host host) {
		cLoadSchedular.addHost(host);
		rSchedular.addHost(host);
	}

	@Override
	public synchronized void removeHost(Host host) {
		cLoadSchedular.removeHost(host);
		rSchedular.removeHost(host);
	}

	@Override
	public synchronized void updateMetric(Host host, Metric metric) {
		rSchedular.updateMetric(host, metric);
		cLoadSchedular.updateMetric(host, metric);
	}

}
