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
package org.krakenapps.ipmanager.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "ipm_logs")
public class IpEventLog implements Marshalable {
	public static enum Type {
		NewIpDetected(1), NewMacDetected(2), IpChanged(3), MacChanged(4), IpConflict(5);

		private int code;

		Type(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "org_id", nullable = false)
	private int orgId;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	private Agent agent;

	@Column(nullable = false)
	private Date date;

	@Column(nullable = false)
	private int type;

	@Column(length = 60)
	private String ip1;

	@Column
	private long ip1long;

	@Column(length = 60)
	private String ip2;

	@Column
	private long ip2long;

	@Column(length = 20)
	private String mac1;

	@Column(length = 20)
	private String mac2;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrgId() {
		return orgId;
	}

	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getIp1() {
		return ip1;
	}

	public void setIp1(String ip1) {
		this.ip1 = ip1;
		this.ip1long = 0;
		String[] t = ip1.split("\\.");
		for (String s : t)
			ip1long = (ip1long << 8) + Integer.parseInt(s);
	}

	public String getIp2() {
		return ip2;
	}

	public void setIp2(String ip2) {
		this.ip2 = ip2;
		this.ip2long = 0;
		String[] t = ip2.split("\\.");
		for (String s : t)
			ip2long = (ip2long << 8) + Integer.parseInt(s);
	}

	public String getMac1() {
		return mac1;
	}

	public void setMac1(String mac1) {
		this.mac1 = mac1;
	}

	public String getMac2() {
		return mac2;
	}

	public void setMac2(String mac2) {
		this.mac2 = mac2;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("agent_id", agent.getId());
		m.put("date", dateFormat.format(date));
		m.put("type", type);
		m.put("ip1", ip1);
		m.put("ip2", ip2);
		m.put("mac1", mac1);
		m.put("mac2", mac2);
		return m;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("id=%d, date=%s, type=%s, ip1=%s, ip2=%s, mac1=%s, mac2=%s", id, dateFormat.format(date),
				Type.values()[type - 1], ip1, ip2, mac1, mac2);
	}
}
