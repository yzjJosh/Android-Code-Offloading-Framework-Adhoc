package mobilecloud.engine.host.monitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import mobilecloud.api.MonitorHostRequest;
import mobilecloud.api.Response;
import mobilecloud.client.Client;
import mobilecloud.engine.Config;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.provider.HostProvider;

/**
 * A host monitor is a monitor which detects state of servers. If server
 * crashes, monitor will detect it and remove it from configurations.
 * If a new server starts, it will add it to the configuration.
 *
 */
public class HostMonitor {
    
    private static HostMonitor instance;
    
    private final HostProvider provider;
    private final Client client;
    private final Map<Host, MonitorThread> threads;
    private final CenterThread centerThread;
    private long checkProviderInterval = Config.HOST_MONITOR_CHECK_PROVIDER_INTERVAL;
    private long checkHostInterval = Config.HOST_MONITOR_CHECK_HOST_INTERVAL;
    private HostStatusChangeListener listener;
    private boolean hasStarted;
    
    public HostMonitor(@NonNull HostProvider provider, @NonNull Client client) {
        this.provider = provider;
        this.client = client;
        this.hasStarted = false;
        this.threads = new ConcurrentHashMap<>();
        this.centerThread = new CenterThread();
    }
    
    /**
     * Add listener to this monitor
     * @param listener the listener to add
     * @return this monitor
     */
    public HostMonitor withHostStatusChangeListener(HostStatusChangeListener listener) {
        this.listener = listener;
        return this;
    }
    
    /**
     * Set check provider interval
     * @param interval the interval
     * @return this monitor
     */
    public HostMonitor withCheckProviderInterval(long interval) {
        this.checkProviderInterval = Math.max(0, interval);
        return this;
    }
    
    /**
     * Set check host interval
     * @param interval the interval
     * @return this monitor
     */
    public HostMonitor withCheckHostInterval(long interval) {
        this.checkHostInterval = Math.max(0, interval);
        return this;
    }
    
    /**
     * Start this monitor
     */
    public void start() {
        if(!hasStarted) {
            synchronized(this) {
                if(!hasStarted) {
                    centerThread.start();
                    hasStarted = true;
                }
            }
        }
    }
    
    /**
     * Stop this monitor. This method will wait until all working thread exists
     */
    public void stop() {
        centerThread.kill();
        try {
            centerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(MonitorThread monitor: threads.values()) {
            monitor.kill();
            try {
                monitor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    //A thread which periodically check provider and control monitor threads
    private class CenterThread extends Thread {

        private boolean stopSign = false;
        
        @Override
        public void run() {
            while(!stopSign) {
                Set<Host> hosts = new HashSet<>(provider.provide());
                for(Host host: hosts) {
                    if(!threads.containsKey(host)) {
                        MonitorThread monitorThread = new MonitorThread(host);
                        threads.put(host, monitorThread);
                        monitorThread.start();
                    }
                }
                Iterator<Host> iterator = threads.keySet().iterator();
                while(iterator.hasNext()) {
                    Host host = iterator.next();
                    if(!hosts.contains(host)) {
                        threads.get(host).kill();
                        iterator.remove();
                    }
                }
                try {
                    Thread.sleep(checkProviderInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void kill() {
            stopSign = true;
            this.interrupt();
        }
    }

    //A thread which periodically check if a host is alive
    private class MonitorThread extends Thread {

        private final Host host;
        private Boolean isAlive;
        private boolean stopSign;

        public MonitorThread(@NonNull Host host) {
            this.host = host;
            this.isAlive = null;
            this.stopSign = false;
        }

        @Override
        public void run() {
            while (!stopSign) {
                try {
                    Response resp = client.request(new MonitorHostRequest().setIp(host.ip).setPort(host.port));
                    if (!resp.isSuccess()) {
                        throw resp.getThrowable();
                    } else {
                        if (isAlive == null || !isAlive) {
                            isAlive = true;
                            if (listener != null) {
                                listener.onHostStatusChange(host, isAlive);
                            }
                        }
                    }
                } catch (Throwable e) {
                    if (isAlive == null || isAlive) {
                        isAlive = false;
                        if (listener != null) {
                            listener.onHostStatusChange(host, isAlive);
                        }
                    }
                }
                try {
                    Thread.sleep(checkHostInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!centerThread.stopSign) {
                // If this thread is killed by center thread, then we know that the
                // provider has removed this host. Then we assume that the host
                // becomes unavailable.
                if (isAlive == null || isAlive) {
                    isAlive = false;
                    if (listener != null) {
                        listener.onHostStatusChange(host, isAlive);
                    }
                }
            }
        }

        public void kill() {
            stopSign = true;
            this.interrupt();
        }
    }
    
    /**
     * Get default host monitor
     * @return the monitor
     */
    public static HostMonitor getInstance() {
        if(instance == null) {
            synchronized(HostMonitor.class) {
                if(instance == null) {
                    try {
                        HostProvider provider = (HostProvider) Class.forName(Config.HOST_PROVIDER).newInstance();
                        instance = new HostMonitor(provider, Client.getInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }
}
