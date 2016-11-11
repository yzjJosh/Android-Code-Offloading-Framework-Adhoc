package mobilecloud.metric;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricGenerator {
    
    private static final long METRIC_TIME_PERIOD = 3000;
    private static MetricGenerator instance;
    
    private ConcurrentLinkedQueue<StampedInteger> requestQueue;
    private ConcurrentLinkedQueue<StampedInteger> readQueue;
    private ConcurrentLinkedQueue<StampedInteger> writeQueue;
    
    public MetricGenerator() {
        this.readQueue = new ConcurrentLinkedQueue<>();
        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.requestQueue = new ConcurrentLinkedQueue<>();
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
     * @return this generator for method chaining
     */
    public MetricGenerator reportWrite(int bytes) {
        removeOutDatedRecords(writeQueue);
        writeQueue.add(new StampedInteger(System.currentTimeMillis(), bytes));
        return this;
    }
    
    /**
     * Report that a request has been received
     * @return this generator for method chaining
     */
    public MetricGenerator reportRequest() {
        removeOutDatedRecords(requestQueue);
        requestQueue.add(new StampedInteger(System.currentTimeMillis(), 1));
        return this;
    }
    
    private int getReadBPS() {
        removeOutDatedRecords(readQueue);
        int bytes = 0;
        for(StampedInteger i: readQueue) {
            bytes += i.val;
        }
        return (int) (bytes/((double)METRIC_TIME_PERIOD/1000));
    }
    
    private int getWriteBPS() {
        removeOutDatedRecords(writeQueue);
        int bytes = 0;
        for(StampedInteger i: writeQueue) {
            bytes += i.val;
        }
        return (int) (bytes/((double)METRIC_TIME_PERIOD/1000));
    }
    
    private double getRequestPerSecond() {
        removeOutDatedRecords(requestQueue);
        int requests  = 0;
        for(StampedInteger i: requestQueue) {
            requests += i.val;
        }
        return (double) requests/((double)METRIC_TIME_PERIOD/1000);
    }
    
    private double getCPULoadPercentage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                  + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (double)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)) * 100;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }
    
    /**
     * Get the current metric of Android device
     * 
     * @return the metric
     */
    public Metric getMetric() {
        return new Metric(getReadBPS(), getWriteBPS(), getCPULoadPercentage(), getRequestPerSecond());
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
