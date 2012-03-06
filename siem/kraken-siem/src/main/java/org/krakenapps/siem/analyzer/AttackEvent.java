package org.krakenapps.siem.analyzer;

import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.event.api.Event;

class AttackEvent {
	private Event event;
	private AtomicInteger count;

	public Event getEvent() {
		return event;
	}

	public AtomicInteger getCount() {
		return count;
	}

	public AttackEvent(Event event) {
		this.event = event;
		this.count = new AtomicInteger(1);
	}
}
