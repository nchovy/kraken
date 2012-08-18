package org.krakenapps.eventstorage.engine;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.eventstorage.Event;

public class EventReadHelper {
	private final int offset;
	private final int limit;
	private int hits = 0;
	private LongSet readed = new LongSet();
	private List<Event> result = new ArrayList<Event>();

	public EventReadHelper(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	public int getHits() {
		return hits;
	}

	public int incrementHits(int n) {
		hits += n;
		return hits;
	}

	public boolean addReadedId(long id) {
		return readed.add(id);
	}

	public List<Event> getResult() {
		return result;
	}

	public void addResult(Event event) {
		result.add(event);
	}

	public void close() {
		if (readed != null)
			readed.close();
	}
}
