/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.ipmanager;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.krakenapps.ipmanager.model.IpEventLog;

public class LogQueryCondition extends QueryCondition {
	private int orgId;
	private Integer agentId;
	private int page;
	private int pageSize;
	private Date from;
	private Date to;
	private IpEventLog.Type type;
	private Long ipFrom;
	private Long ipTo;
	private String mac;

	public LogQueryCondition(int orgId, int page, int pageSize) {
		this.orgId = orgId;
		this.page = page;
		this.pageSize = pageSize;
	}

	public int getOrgId() {
		return orgId;
	}

	public int getPage() {
		return page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public IpEventLog.Type getType() {
		return type;
	}

	public void setType(IpEventLog.Type type) {
		this.type = type;
	}

	public String getIpFrom() {
		return longToIp(ipFrom);
	}

	public String getIpTo() {
		return longToIp(ipTo);
	}

	private String longToIp(long ip) {
		String s = "";
		for (int i = 0; i < 4; i++) {
			if (!s.isEmpty())
				s = "." + s;
			s = (ip & 0xff) + s;
			ip = ip >> 8;
		}
		return s;
	}

	public void setIpRange(String ipFrom, String ipTo) {
		this.ipFrom = ipToLong(ipFrom);
		this.ipTo = ipToLong(ipTo);
	}

	private long ipToLong(String ip) {
		long l = 0;
		String[] t = ip.split("\\.");
		for (String s : t)
			l = (l << 8) + Integer.parseInt(s);
		return l;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	@Override
	public Predicate getPredicate(CriteriaBuilder cb, Root<?> root) {
		Predicate p = cb.equal(root.get("orgId"), orgId);

		if (agentId != null)
			p = cb.and(p, cb.equal(root.join("agent").get("id"), agentId));
		if (from != null)
			p = cb.and(p, cb.greaterThanOrEqualTo(root.<Date> get("date"), from));
		if (to != null)
			p = cb.and(p, cb.lessThanOrEqualTo(root.<Date> get("date"), to));
		if (type != null)
			p = cb.and(p, cb.equal(root.get("type"), type.getCode()));
		if (ipFrom != null) {
			Predicate p1 = cb.between(root.<Long> get("ip1long"), ipFrom, ipTo);
			Predicate p2 = cb.between(root.<Long> get("ip2long"), ipFrom, ipTo);
			p = cb.and(p, cb.or(p1, p2));
		}
		if (mac != null) {
			String m = "%" + mac.toUpperCase() + "%";
			Predicate p1 = cb.like(root.<String> get("mac1"), m);
			Predicate p2 = cb.like(root.<String> get("mac2"), m);
			p = cb.and(p, cb.or(p1, p2));
		}

		return p;
	}
}
