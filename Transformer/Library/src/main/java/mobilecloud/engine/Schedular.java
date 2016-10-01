package mobilecloud.engine;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Schedular is responsible to schedule which host to execute a task
 *
 */
public class Schedular {
    
    private static Schedular instance;
    
    private Set<Host> hosts = new LinkedHashSet<>();
    
    /**
     * Schedule hosts in a round-robin manner
     * @return
     */
    public synchronized Host schedule() {
        Host host = hosts.iterator().next();
        hosts.remove(host);
        hosts.add(host);
        return host;
    }
    
    /**
     * Get the available 
     * @return
     */
    public synchronized int availableNum() {
        return hosts.size();
    }
    
    public synchronized boolean haveAvailable() {
        return availableNum() > 0;
    }
    
    /**
     * Add a host to this schedular so that this schedular can begin scheduling
     * @param host
     */
    public synchronized void addHost(Host host) {
        hosts.add(host);
    }
    
    /**
     * Remove a host from the schedular
     * @param host the host to remove
     */
    public synchronized void removeHost(Host host) {
        hosts.remove(host);
    }
    
    public static Schedular getInstance() {
        if(instance == null) {
            synchronized(Schedular.class) {
                if(instance == null) {
                    instance = new Schedular();
                }
            }
        }
        return instance;
    }

}
