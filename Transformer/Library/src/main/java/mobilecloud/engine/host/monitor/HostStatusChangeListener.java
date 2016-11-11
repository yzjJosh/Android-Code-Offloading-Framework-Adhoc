package mobilecloud.engine.host.monitor;

import mobilecloud.engine.host.Host;

/**
 * A listener which listens to the host status changed event
 */
public interface HostStatusChangeListener {
    
    public void onHostStatusChange(Host host, boolean isAlive);
    
}
