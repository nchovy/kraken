package org.krakenapps.siem.msgbus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.msgbus.Marshalable;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.siem.model.TopEntry;

@Component(name = "siem-dashboard-plugin")
@JpaConfig(factory = "siem")
@MsgbusPlugin
public class DashboardPlugin {

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@SuppressWarnings("unchecked")
	@Transactional
	@MsgbusMethod
	public void getSeverityStats(Request req, Response resp) {
		Date begin = req.getDate("begin");
		Date end = req.getDate("end");

		EntityManager em = entityManagerService.getEntityManager();
		String query = "SELECT e.severity, SUM(e.count) FROM Event e WHERE e.lastSeen >= ? AND e.lastSeen <= ? GROUP BY e.severity";
		List<Object> rows = em.createQuery(query).setParameter(1, begin).setParameter(2, end).getResultList();

		SeverityStats stats = new SeverityStats();

		for (Object row : rows) {
			Object[] columns = (Object[]) row;
			int severity = (Integer) columns[0];
			long count = (Long) columns[1];

			switch (severity) {
			case 1:
				stats.emergency = count;
				break;
			case 2:
				stats.critical = count;
				break;
			case 3:
				stats.alert = count;
				break;
			case 4:
				stats.warning = count;
				break;
			case 5:
				stats.notice = count;
				break;
			}
		}

		resp.put("stats", stats.marshal());
	}

	@MsgbusMethod
	public void getAttackStats(Request req, Response resp) {
		// need rule id column
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@MsgbusMethod
	public void getAttackerStats(Request req, Response resp) {
		Date begin = req.getDate("begin");
		Date end = req.getDate("end");
		int limit = req.getInteger("limit");

		EntityManager em = entityManagerService.getEntityManager();
		String query = "SELECT new org.krakenapps.siem.model.TopEntry(e.sourceIp, SUM(e.count)) "
				+ "FROM Event e WHERE e.category = ? AND e.lastSeen >= ? AND e.lastSeen <= ? GROUP BY e.sourceIp ORDER BY SUM(e.count)";
		List<TopEntry> entries = em.createQuery(query).setParameter(1, "Attack").setParameter(2, begin).setParameter(3,
				end).setMaxResults(limit).getResultList();

		resp.put("stats", Marshaler.marshal(entries));
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	@Transactional
	public void getVictimStats(Request req, Response resp) {
		Date begin = req.getDate("begin");
		Date end = req.getDate("end");
		int limit = req.getInteger("limit");

		EntityManager em = entityManagerService.getEntityManager();
		String query = "SELECT new org.krakenapps.siem.model.TopEntry(e.destinationIp, SUM(e.count)) "
				+ "FROM Event e WHERE e.category = ? AND e.lastSeen >= ? AND e.lastSeen <= ? GROUP BY e.destinationIp ORDER BY SUM(e.count)";
		List<TopEntry> entries = em.createQuery(query).setParameter(1, "Attack").setParameter(2, begin).setParameter(3,
				end).setMaxResults(limit).getResultList();

		resp.put("stats", Marshaler.marshal(entries));
	}

	@MsgbusMethod
	public void getAttackTrendGraph(Request req, Response resp) {

	}

	private static class SeverityStats implements Marshalable {
		private long emergency;
		private long alert;
		private long critical;
		private long warning;
		private long notice;

		@Override
		public Map<String, Object> marshal() {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("emergency", emergency);
			m.put("alert", alert);
			m.put("critical", critical);
			m.put("warning", warning);
			m.put("notice", notice);
			return m;
		}
	}

}
