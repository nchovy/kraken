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

import org.krakenapps.ipmanager.model.IpEventLog;

public class LogQueryCondition {
	private int orgId;
	private Integer agentId;
	private int page;
	private int pageSize;
	private Date from;
	private Date to;
	private IpEventLog.Type type;
	private String ip;
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

}
