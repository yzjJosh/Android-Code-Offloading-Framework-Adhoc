package mobilecloud.engine.schedular;

import java.util.LinkedHashSet;
import java.util.Set;

import mobilecloud.engine.host.Host;
import mobilecloud.metric.Metric;

/**
 * A schedular which schedules hosts in a round-robin manner
 *
 */
public class RoundRobinSchedular extends Schedular{
    
    private Set<Host> hosts = new LinkedHashSet<>();
    
    @Override
    public synchronized Host schedule() {
        Host host = trySchedule();
        hosts.remove(host);
        hosts.add(host);
        return host;
    }
    
    @Override
    public synchronized Host trySchedule() {
        return hosts.iterator().next();
    }
    
    @Override
    public synchronized int availableNum() {
        return hosts.size();
    }

    @Override
    public synchronized boolean haveAvailable() {
        return availableNum() > 0;
    }
    
    @Override
    public synchronized void addHost(Host host) {
        if(!hosts.contains(host)) {
            hosts.add(host);
        }
    }
    
    @Override
    public synchronized void removeHost(Host host) {
        hosts.remove(host);
    }

    @Override
    public void updateMetric(Host host, Metric metric) {
        // Do nothing with metric in round robin schedular
    }

}
