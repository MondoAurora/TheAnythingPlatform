package me.giskard.dust.dev;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DustDevCounter<KeyType> implements Iterable<Map.Entry<KeyType, Long>> {
	private final Map<KeyType, Long> counts;

	public DustDevCounter(boolean sorted) {
		counts = sorted ? new TreeMap<>() : new HashMap<>();
	}

	public void reset() {
		counts.clear();
	}

	public void add(KeyType ob) {
		add(ob, 1L);
	}

	public void add(KeyType ob, long count) {
		Long l = counts.getOrDefault(ob, 0L);
		counts.put(ob, l + count);
	}

	@Override
	public Iterator<Entry<KeyType, Long>> iterator() {
		return counts.entrySet().iterator();
	}

	public boolean contains(Object ob) {
		return counts.containsKey(ob);
	}

	public Long peek(Object ob) {
		return counts.getOrDefault(ob, 0L);
	}
	
	public Long getCount() {
		Long ret = 0L;
		
		for ( Long l : counts.values() ) {
			ret += l;
		}
		
		return ret;
	}
}
