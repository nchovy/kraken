package org.krakenapps.event.api;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

public interface EventApi {
	Collection<Event> getEventsByArea(String lang, int organizationId, int offset, int limit, Date begin, Date end,
			Integer areaId, Set<EventSeverity> severities, Collection<EventPredicate> conditions, long[] totalCount);

	Collection<Event> getEventsByHosts(String lang, int organizationId, int offset, int limit, Date begin, Date end,
			Set<Integer> hostIds, Set<EventSeverity> severities, Collection<EventPredicate> conditions,
			long[] totalCount);

	Event getEvent(EventKey key);
}
