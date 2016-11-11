package mobilecloud.engine.host.monitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.MonitorHostResponse;
import mobilecloud.api.response.Response;
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
    private long retryInterval = Config.HOST_MONITOR_RETRY_INTERVAL;
    private int retryTimes = Config.HOST_MONITOR_RETRY_TIMES;
    private HostStatusChangeListener hostStatusChangedListener;
    private HostMetricUpdatedListener hostMetricUpdatedListener;
    private boolean hasStarted;
    
    public HostMonitor(HostProvider provider, Client client) {
        this.provider = provider;
        this.client = client;
        this.hasStarted = false;
        this.threads = new ConcurrentHashMap<>();
        this.centerThread = new CenterThread();
    }
    
    /**
     * Add hostStatusChangedListener to this monitor
     * @param hostStatusChangedListener the hostStatusChangedListener to add
     * @return this monitor
     */
    public HostMonitor withHostStatusChangeListener(HostStatusChangeListener listener) {
        this.hostStatusChangedListener = listener;
        return this;
    }
    
    /**
     * Add hostMetricUpdatedListener to this monitor
     * @param listener the hostMetricUpdatedListener
     * @return this monitor
     */
    public HostMonitor withMetricUpdatedListener(HostMetricUpdatedListener listener) {
        this.hostMetricUpdatedListener = listener;
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
     * Set the retry interval
     * @param retryInterval the retry interval
     * @return this monitor
     */
    public HostMonitor withRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }
    
    /**
     * Set the retry times
     * @param retryTimes the retry times
     * @return this monitor
     */
    public HostMonitor withRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
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

    // A thread which periodically check if a host is alive
    private class MonitorThread extends Thread {

        private final Host host;
        private Boolean isAlive;
        private boolean stopSign;

        public MonitorThread(Host host) {
            this.host = host;
            this.isAlive = null;
            this.stopSign = false;
        }

        @Override
        public void run() {
            while (!stopSign) {
                try {
                    Response resp = monitorRetry(host, retryTimes, retryInterval);
                    if (!resp.isSuccess()) {
                        throw resp.getThrowable();
                    } else {
                        if (isAlive == null || !isAlive) {
                            isAlive = true;
                            if (hostStatusChangedListener != null) {
                                hostStatusChangedListener.onHostStatusChange(host, isAlive);
                            }
                        }
                        if(hostMetricUpdatedListener != null) {
                            hostMetricUpdatedListener.onHostMetricUpdated(host, ((MonitorHostResponse)resp).getMetric());
                        }
                    }
                } catch (Throwable e) {
                    if (isAlive == null || isAlive) {
                        isAlive = false;
                        if (hostStatusChangedListener != null) {
                            hostStatusChangedListener.onHostStatusChange(host, isAlive);
                        }
                    }
                }
                try {
                    Thread.sleep(checkHostInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!centerThread.stopSign) {
                // If this thread is killed by center thread, then we know that
                // the
                // provider has removed this host. Then we assume that the host
                // becomes unavailable.
                if (isAlive == null || isAlive) {
                    isAlive = false;
                    if (hostStatusChangedListener != null) {
                        hostStatusChangedListener.onHostStatusChange(host, isAlive);
                    }
                }
            }
        }

        public void kill() {
            stopSign = true;
            this.interrupt();
        }
    }

    private Response monitorRetry(Host host, int retryTimes, long retryInterval) throws Exception {
        Request req = new MonitorHostRequest().setIp(host.ip).setPort(host.port);
        for (int i = 0; i < retryTimes - 1; i++) {
            try {
                return client.request(req);
            } catch (Exception e) {
                Thread.sleep(retryInterval);
            }
        }
        return client.request(req);
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
