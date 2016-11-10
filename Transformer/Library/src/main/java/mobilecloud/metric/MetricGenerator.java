package mobilecloud.metric;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricGenerator {
    
    private static final long METRIC_TIME_PERIOD = 3000;
    private static MetricGenerator instance;
    
    private ConcurrentLinkedQueue<StampedInteger> readQueue;
    private ConcurrentLinkedQueue<StampedInteger> writeQueue;
    
    public MetricGenerator() {
        this.readQueue = new ConcurrentLinkedQueue<>();
        this.writeQueue = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Report a number of bytes has been read currently
     * @param bytes number of bytes read
     * @return this generator for method chaining
     */
    public MetricGenerator reportRead(int bytes) {
        removeOutDatedRecords(readQueue);
        readQueue.add(new StampedInteger(System.currentTimeMillis(), bytes));
        return this;
    }
    
    private void removeOutDatedRecords(Queue<StampedInteger> queue) {
        long cur = System.currentTimeMillis();
        while(!queue.isEmpty() && queue.peek().timestamp < cur - METRIC_TIME_PERIOD) {
            queue.poll();
        }
    }
    
    /**
     * Report a number of bytes has been written currently
     * @param bytes number of bytes written
     * @return thsi generator for method chaining
     */
    public MetricGenerator reportWrite(int bytes) {
        removeOutDatedRecords(writeQueue);
        writeQueue.add(new StampedInteger(System.currentTimeMillis(), bytes));
        return this;
    }
    
    private int getReadBPS() {
        removeOutDatedRecords(readQueue);
        int bytes = 0;
        for(StampedInteger i: readQueue) {
            bytes += i.val;
        }
        return (int) (bytes/(METRIC_TIME_PERIOD/1000));
    }
    
    private int getWriteBPS() {
        removeOutDatedRecords(writeQueue);
        int bytes = 0;
        for(StampedInteger i: writeQueue) {
            bytes += i.val;
        }
        return (int) (bytes/(METRIC_TIME_PERIOD/1000));
    }
    
    private double getCPULoadPercentage() {
        return 0.0;
    }
    
    /**
     * Get the current metric of Android device
     * @return the metric
     */
    public Metric getMetric() {
        return new Metric(getReadBPS(), getWriteBPS(), getCPULoadPercentage());
    }
    
    /**
     * Get the default metric generator instance
     * @return the default metric generator
     */
    public static MetricGenerator getInstance() {
        if(instance == null) {
            synchronized(MetricGenerator.class) {
                if(instance == null) {
                    instance = new MetricGenerator();
                }
            }
        }
        return instance;
    }
    
    
    private static class StampedInteger {
        public final long timestamp;
        public final int val;
        
        public StampedInteger(long timestamp, int val) {
            this.timestamp = timestamp;
            this.val = val;
        }
    }

}
