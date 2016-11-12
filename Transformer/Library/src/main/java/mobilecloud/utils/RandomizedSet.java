package mobilecloud.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomizedSet<T> {
	private List<T> list = new ArrayList<>();
	private Map<T, Integer> map = new HashMap<T, Integer>();
	
	public boolean add(T ele) {
		if(map.containsKey(ele)) return false;
		map.put(ele, list.size());
		list.add(ele);
		return true;
	}
	
	public boolean remove(T ele) {
		if(!map.containsKey(ele)) return false;
		int index = map.get(ele);
		T last = list.get(list.size()-1);
		list.set(index, last);
		list.remove(list.size()-1);
		map.put(last, index);
		map.remove(ele);
		return true;
	}
	
	public List<T> sample(int n) {
		if(n >= list.size()) {
			return new ArrayList<>(list);
		} else if (n <= 0) {
			return new ArrayList<>();
		} else {
			List<T> res = new ArrayList<>(n);
			for(int i=0; i<list.size(); i++) {
				int index = new Random().nextInt(i+1);
				if(index < res.size()) {
					T val = res.get(index);
					res.set(index, list.get(i));
					if(i < res.size()) {
						res.set(i, val);
					}
				}
			}
			return res;
		}
	}
	
	public int size() {
		return list.size();
	}
}
