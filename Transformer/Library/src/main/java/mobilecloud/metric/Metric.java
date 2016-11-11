package mobilecloud.metric;

import java.io.Serializable;

public class Metric implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public final int readBPS;
    public final int writeBPS;
    public final double cpuLoadPercentage;
    
    public Metric(int readBPS, int writeBPS, double cpuLoadPercentage) {
        this.readBPS = readBPS;
        this.writeBPS = writeBPS;
        this.cpuLoadPercentage = cpuLoadPercentage;
    }

    @Override
    public String toString() {
        return "readBPS: " + readBPS + "bps, writeBPS: " + writeBPS + "bps, cpuLoad: " + cpuLoadPercentage + "%";
    }
    
}
