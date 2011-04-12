package org.krakenapps.event.api.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.model.Host;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventApi;
import org.krakenapps.event.api.EventKey;
import org.krakenapps.event.api.EventPredicate;
import org.krakenapps.event.api.EventSeverity;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "event-api")
@Provides
public class EventApiImpl implements EventApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private HostApi hostApi;

	@Override
	public Collection<Event> getEventsByArea(String lang, int organizationId, int offset, int limit, Date begin,
			Date end, Integer areaId, Set<EventSeverity> severities, Collection<EventPredicate> conditions,
			long[] totalCount) {
		return getEvents(Event.class, lang, organizationId, offset, limit, begin, end, areaId, severities, conditions,
				totalCount);
	}

	@Override
	public Collection<Event> getEventsByHosts(String lang, int organizationId, int offset, int limit, Date begin,
			Date end, Set<Integer> hostIds, Set<EventSeverity> severities, Collection<EventPredicate> conditions,
			long[] totalCount) {
		return getEventsInternal(Event.class, lang, organizationId, offset, limit, begin, end, hostIds, severities,
				conditions, totalCount);
	}

	@Transactional
	@Override
	public Event getEvent(EventKey key) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(Event.class, key);
	}

	private <T> Collection<T> getEvents(Class<T> returnType, String lang, int organizationId, int offset, int limit,
			Date begin, Date end, Integer areaId, Set<EventSeverity> severities, Collection<EventPredicate> conditions,
			long[] totalCount) {
		List<Host> hosts = null;
		if (areaId == null || areaId.intValue() == 0)
			hosts = hostApi.getHosts(organizationId);
		else {
			hosts = hostApi.getHostsRecursively(organizationId, areaId);
		}

		if (hosts.isEmpty()) {
			if (totalCount != null && totalCount.length != 0)
				totalCount[0] = 0;
			return new ArrayList<T>();
		}

		Set<Integer> hostIdList = new HashSet<Integer>();
		for (Host host : hosts) {
			hostIdList.add(host.getId());
		}

		return getEventsInternal(returnType, lang, organizationId, offset, limit, begin, end, hostIdList, severities,
				conditions, totalCount);
	}

	@Transactional
	private <T> Collection<T> getEventsInternal(Class<T> returnType, String lang, int orgId, int offset, int limit,
			Date begin, Date end, Set<Integer> hostIds, Set<EventSeverity> severities,
			Collection<EventPredicate> conditions, long[] totalCount) {
		EntityManager em = entityManagerService.getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<T> eaq = cb.createQuery(returnType);
		Root<T> eaf = eaq.from(returnType);
		Predicate eapred = generatePredicate(orgId, begin, end, hostIds, severities, conditions, cb, eaf);
		eaq.where(eapred);
		eaq.orderBy(cb.desc(eaf.get("lastSeen")));

		if (totalCount != null && totalCount.length > 0) {
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<T> cf = cq.from(returnType);
			Predicate pred = generatePredicate(orgId, begin, end, hostIds, severities, conditions, cb, cf);
			cq.where(pred);
			cq.select(cb.count(cf));
			totalCount[0] = em.createQuery(cq).getSingleResult();
		}

		TypedQuery<T> query = em.createQuery(eaq);
		List<T> resultList = query.setFirstResult(offset).setMaxResults(limit).getResultList();

		return resultList;
	}

	private <T> Predicate generatePredicate(int organizationId, Date begin, Date end, Set<Integer> hostIdList,
			Set<EventSeverity> severities, Collection<EventPredicate> conditions, CriteriaBuilder cb, Root<T> f) {
		Predicate pred = cb.equal(f.<EventKey> get("key").<Integer> get("organizationId"), organizationId);

		if (!hostIdList.isEmpty())
			pred = cb.and(pred, f.get("hostId").in(hostIdList.toArray()));
		if (severities != null && !severities.isEmpty())
			pred = cb.and(pred, f.get("severity").in(toIntegerSet(severities).toArray()));
		pred = cb.and(pred, cb.between(f.<Date> get("lastSeen"), begin, end));

		if (conditions != null) {
			for (EventPredicate cond : conditions) {
				pred = cb.and(pred, generatePredicate(cb, f, cond));
			}
		}

		return pred;
	}

	private <T> Predicate generatePredicate(CriteriaBuilder cb, Root<T> f, EventPredicate condition) {
		String propertyName = convertPropertyName(condition.getName());
		Object value1, value2;

		if (propertyName.equals("sourceIp") || propertyName.equals("targetIp")) {
			propertyName = propertyName + "Raw";
			value1 = (Long) parseIpv4(condition.getField1());
			value2 = (Long) parseIpv4(condition.getField2());
		} else {
			value1 = condition.getField1();
			value2 = condition.getField2();
		}

		String operator = condition.getOperator().getMnemonic();
		if (operator.equals("eq"))
			return cb.equal(f.get(propertyName), value1);
		if (operator.equals("neq"))
			return cb.notEqual(f.get(propertyName), value1);
		if (operator.equals("gt"))
			return cb.gt(f.<Number> get(propertyName), (Number) value1);
		if (operator.equals("gte"))
			return cb.ge(f.<Number> get(propertyName), (Number) value1);
		if (operator.equals("lt"))
			return cb.lt(f.<Number> get(propertyName), (Number) value1);
		if (operator.equals("lte"))
			return cb.le(f.<Number> get(propertyName), (Number) value1);
		if (operator.equals("like"))
			return cb.like(cb.upper(f.<String> get(propertyName)), "%" + ((String) value1).toUpperCase() + "%");
		if (operator.equals("between")) {
			return cb.between(f.<Long> get(propertyName), (Long) value1, (Long) value2);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private static class ConditionMapping {
		private String propertyName;

		private String type;

		public ConditionMapping(String propertyName, String type) {
			this.propertyName = propertyName;
			this.type = type;
		}
	}

	private static final Map<String, ConditionMapping> conditionMap = new HashMap<String, ConditionMapping>();

	static {
		conditionMap.put("category", new ConditionMapping("category", "String"));
		conditionMap.put("severity", new ConditionMapping("severity", "Integer"));
		conditionMap.put("count", new ConditionMapping("count", "Integer"));
		conditionMap.put("sourceIp", new ConditionMapping("sourceIp", "String"));
		conditionMap.put("targetIp", new ConditionMapping("targetIp", "String"));
		conditionMap.put("sourcePort", new ConditionMapping("sourcePort", "Integer"));
		conditionMap.put("targetPort", new ConditionMapping("targetPort", "Integer"));
		conditionMap.put("eventSource", new ConditionMapping("eventSource", "String"));
		conditionMap.put("message", new ConditionMapping("message", "String"));
		conditionMap.put("hostGuid", new ConditionMapping("hostGuid", "String"));
		conditionMap.put("logger", new ConditionMapping("logger", "String"));
		conditionMap.put("logs", new ConditionMapping("logs", "String"));
	}

	private String convertPropertyName(String property) {
		return conditionMap.get(property).propertyName;
	}

	private Long parseIpv4(Object value2) {
		String val = (String) value2;
		Inet4Address byName;

		try {
			byName = (Inet4Address) InetAddress.getByName(val);
			byte[] address = byName.getAddress();
			long a = ((address[0] & 0xFF) << 24) | ((address[1] & 0xFF) << 16) | ((address[2] & 0xFF) << 8)
					| ((address[3] & 0xFF));

			return a;
		} catch (UnknownHostException e) {
			return 0L;
		}
	}

	private Set<Integer> toIntegerSet(Set<EventSeverity> severities) {
		Set<Integer> ret = new TreeSet<Integer>();
		for (EventSeverity es : severities) {
			ret.add(es.ordinal());
		}
		return ret;
	}

}
