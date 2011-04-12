/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.event.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "siem_events")
public class Event {
	@EmbeddedId
	private EventKey key;

	@Column(name = "org_id")
	private int organizationId;

	@Column(name = "first_seen", nullable = false)
	private Date firstSeen;

	@Column(name = "last_seen", nullable = false)
	private Date lastSeen;

	@Column(name = "category", nullable = false, length = 20)
	private String category;

	@Column(name = "severity", nullable = false)
	private int severity;

	@Column(name = "host")
	private Integer hostId;

	@Column(name = "src_ip", length = 20)
	private String sourceIp; // IPv4

	@Column(name = "dst_ip", length = 20)
	private String destinationIp; // IPv4

	@Column(name = "src_ip_raw")
	private Long sourceIpRaw;

	@Column(name = "dst_ip_raw")
	private Long destinationIpRaw;

	@Column(name = "src_port")
	private Integer sourcePort;

	@Column(name = "dst_port")
	private Integer destinationPort;

	@Column(name = "msg_key", length = 60)
	private String messageKey;

	@Column(name = "msg", length = 250)
	private String messageValues;

	@Column(name = "rule", length = 20)
	private String rule;

	@Column(name = "cve", length = 20)
	private String cve;

	@Column(name = "detail", length = 250)
	private String detail;

	@Column(name = "count", nullable = false)
	private int count;

	@Column(name = "is_acked")
	private boolean isAcked;

	public EventKey getKey() {
		return key;
	}

	public void setKey(EventKey key) {
		this.key = key;
	}

	public int getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public Date getFirstSeen() {
		return firstSeen;
	}

	public void setFirstSeen(Date firstSeen) {
		this.firstSeen = firstSeen;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getSeverity() {
		return severity;
	}

	public String getSeverityString() {
		return EventSeverity.values()[severity].name();
	}

	public void setSeverity(EventSeverity severity) {
		this.severity = severity.ordinal();
	}

	public Integer getHostId() {
		return hostId;
	}

	public void setHostId(Integer host) {
		this.hostId = host;
	}

	public InetAddress getSourceIp() {
		try {
			return InetAddress.getByName(sourceIp);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public void setSourceIp(InetAddress sourceIp) {
		this.sourceIp = sourceIp.getHostAddress();
	}

	public InetAddress getDestinationIp() {
		try {
			return InetAddress.getByName(destinationIp);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public void setDestinationIp(InetAddress destinationIp) {
		this.destinationIp = destinationIp.getHostAddress();
	}

	public Long getSourceIpRaw() {
		return sourceIpRaw;
	}

	public Long getDestinationIpRaw() {
		return destinationIpRaw;
	}

	public Integer getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(Integer sourcePort) {
		this.sourcePort = sourcePort;
	}

	public Integer getDestinationPort() {
		return destinationPort;
	}

	public void setDestinationPort(Integer destinationPort) {
		this.destinationPort = destinationPort;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public String getMessageValues() {
		return messageValues;
	}

	public void setMessageValues(String messageValues) {
		this.messageValues = messageValues;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getCve() {
		return cve;
	}

	public void setCve(String cve) {
		this.cve = cve;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isAcked() {
		return isAcked;
	}

	public void setAcked(boolean isAcked) {
		this.isAcked = isAcked;
	}

	@Override
	public String toString() {
		String source = "";
		if (sourceIp != null)
			source += sourceIp;

		if (sourcePort != null)
			source += ":" + sourcePort;

		String destination = "";
		if (destinationIp != null)
			destination += destinationIp;

		if (destinationPort != null)
			destination += ":" + destinationPort;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("key=%s, first seen=%s, last seen=%s, category=%s, severity=%d, "
				+ "source=%s, destination=%s, rule=%s, cve=%s, msg=%s, count=%d", key.toString(),
				dateFormat.format(firstSeen), dateFormat.format(lastSeen), category, severity, source, destination,
				rule, cve, messageKey, count);
	}

}
