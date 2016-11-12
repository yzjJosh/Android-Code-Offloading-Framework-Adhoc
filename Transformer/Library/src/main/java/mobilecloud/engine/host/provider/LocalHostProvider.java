package mobilecloud.engine.host.provider;

import java.util.LinkedList;
import java.util.List;

import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.LocalHost;

/**
 * A host provider which provides only local host
 */
public class LocalHostProvider implements HostProvider {
    private final List<Host> localHosts;
    
    public LocalHostProvider() {
        localHosts = new LinkedList<>();
        localHosts.add(new LocalHost());
    }
    
    @Override
    public List<Host> provide() {
        return localHosts;
    }
}
