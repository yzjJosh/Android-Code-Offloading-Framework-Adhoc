package mobilecloud.metric;

public class Metric {
    
    public final int readBPS;
    public final int writeBPS;
    public final double cpuLoadPercentage;
    
    public Metric(int readBPS, int writeBPS, double cpuLoadPercentage) {
        this.readBPS = readBPS;
        this.writeBPS = writeBPS;
        this.cpuLoadPercentage = cpuLoadPercentage;
    }
    
}
