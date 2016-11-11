package mobilecloud.engine.schedular;

import mobilecloud.engine.Config;
import mobilecloud.engine.host.Host;
import mobilecloud.metric.Metric;

/**
 * Schedular is responsible to schedule which host to execute a task
 *
 */
public abstract class Schedular {

    private static Schedular instance;

    /**
     * Schedule hosts, get next host which should take over a task. This method
     * will change the schedular state.
     * 
     * @return the next host
     */
    public abstract Host schedule();

    /**
     * Check what is the next host to be scheduled. This method does not change
     * the schedular state.
     * 
     * @return the next host
     */
    public abstract Host trySchedule();

    /**
     * Get the number of available hosts
     * 
     * @return the number of available hosts
     */
    public abstract int availableNum();

    /**
     * Check if there are available hosts
     * 
     * @return true if there are available hosts
     */
    public abstract boolean haveAvailable();

    /**
     * Add a host to this schedular so that this schedular can begin scheduling.
     * If the host is already inside this schedualr, do nothing
     * 
     * @param host
     *            a new host
     */
    public abstract void addHost(Host host);

    /**
     * Remove a host from the schedular
     * 
     * @param host
     *            the host to remove
     */
    public abstract void removeHost(Host host);
    
    /**
     * Update the metric of a host
     * @param host the host to be updated
     * @param metric its new metric
     */
    public abstract void updateMetric(Host host, Metric metric);

    /**
     * Get the global schedular
     * 
     * @return the schedular
     */
    public static Schedular getInstance() {
        if (instance == null) {
            synchronized (Schedular.class) {
                if (instance == null) {
                    try {
                        instance = (Schedular) Class.forName(Config.SCHEDULAR).newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }

}
