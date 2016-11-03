import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("unused")
public class TimedLRUCache<T> {
	
	public LinkedHashMap<T, Long> cache;
	public long timeout;
	
	public TimedLRUCache (long timeout) {
		this.cache = new LinkedHashMap<T, Long>();
		this.timeout = timeout;
	}
	
	public synchronized void add(T value) {
		removeTimeOutEle();
		if(cache.containsKey(value)) {
			cache.remove(value);
		}
		cache.put(value, System.currentTimeMillis());
	}
	
	private void removeTimeOutEle() {
		long curTime = System.currentTimeMillis();
		Iterator<T> iter =  cache.keySet().iterator();
		while(iter.hasNext()) {
			T next = iter.next();
			if(curTime-cache.get(next)>=timeout) {
				iter.remove();
			}
		}
	}
	
	public synchronized List<T> getAll() {
		removeTimeOutEle();
		return new ArrayList<T>(cache.keySet());
	}
}
