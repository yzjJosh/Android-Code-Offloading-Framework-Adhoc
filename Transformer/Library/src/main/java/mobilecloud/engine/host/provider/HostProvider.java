package mobilecloud.engine.host.provider;

import java.util.List;

import mobilecloud.engine.host.Host;

/**
 * A host provider can provide a list of hosts which may provide cloud service
 *
 */
public interface HostProvider {
    
    public List<Host> provide();

}
