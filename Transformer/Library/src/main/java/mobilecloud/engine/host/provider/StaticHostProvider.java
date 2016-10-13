package mobilecloud.engine.host.provider;

import java.util.LinkedList;
import java.util.List;

import mobilecloud.engine.host.Host;

/**
 * A host provider that provides host information statically. This
 * provider is for development use
 */
public class StaticHostProvider implements HostProvider{
    
    private static List<Host> lists = new LinkedList<>();

    @Override
    public List<Host> provide() {
        synchronized (StaticHostProvider.class) {
            return lists;
        }
    }
    
    /**
     * Add a host to this static host provider
     * @param host the host to add
     */
    public static void addHost(Host host) {
        synchronized (StaticHostProvider.class) {
            lists.add(host);
        }
    }
    
}
