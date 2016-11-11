package mobilecloud.engine.host.monitor;

import mobilecloud.engine.host.Host;
import mobilecloud.metric.Metric;

/**
 * A listener which listens to host metric updated events
 *
 */
public interface HostMetricUpdatedListener {
    
    public void onHostMetricUpdated(Host host, Metric metric);
    
}
