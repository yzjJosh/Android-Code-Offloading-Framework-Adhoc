package mobilecloud.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Timer key lock holds critical section for each single key. Also it ensures
 * that the interval between two lock() on same key are no
 * smaller than given threshold.
 *
 */
public class TimerKeyLock {
    
    private final int minInterval;
    private final Map<Object, TimerLock> locks;
    
    /**
     * Build a timer key lock
     * @param minInterval the minimum time interval between lock and previous unlock for same key
     */
    public TimerKeyLock(int minInterval) {
        this.minInterval = minInterval;
        this.locks = new HashMap<>();
    }
    
    /**
     * Require this lock against given key
     * @param key the key
     * @throws InterruptedException if current thread is interrupted
     */
    public void lock(Object key) throws InterruptedException {
        getLock(key).lock();
    }
    
    /**
     * Unlock this lock against given key
     * @param key the key
     */
    public void unlock(Object key) {
        getLock(key).unlock();
    }
    
    private synchronized TimerLock getLock(Object key) {
        TimerLock lock = locks.get(key);
        if(lock == null) {
            lock = new TimerLock();
            locks.put(key, lock);
        }
        return lock;
    }
    
    private class TimerLock {
        
        private long lastLockTime = 0;
        private boolean available = true;
        
        public synchronized void lock() throws InterruptedException {
            long interval = 0;
            while(!available  || (interval = System.currentTimeMillis() - lastLockTime) < minInterval) {
                if(minInterval > 0) {
                    wait(minInterval - interval);
                } else {
                    wait();
                }
            }
            available = false;
            lastLockTime = System.currentTimeMillis();
        }
        
        public synchronized void unlock() {
            available = true;
            notify();
        }
        
    }

}
