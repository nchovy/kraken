/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.log.api;

import java.util.Date;
import java.util.HashMap;

public class IpsLog extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public IpsLog() {
		put("category", "ips");
	}

	public Date getDate() {
		return (Date) get("date");
	}

	public void setDate(Date date) {
		put("date", date);
	}

	public int getSeverity() {
		return (Integer) get("severity");
	}

	public void setSeverity(int severity) {
		put("severity", severity);
	}

	public String getSrc() {
		return (String) get("src_ip");
	}

	public void setSrc(String src) {
		put("src_ip", src);
	}

	public String getDst() {
		return (String) get("dst_ip");
	}

	public void setDst(String dst) {
		put("dst_ip", dst);
	}

	public Integer getSrcPort() {
		return (Integer) get("src_port");
	}

	public void setSrcPort(Integer srcPort) {
		put("src_port", srcPort);
	}

	public Integer getDstPort() {
		return (Integer) get("dst_port");
	}

	public void setDstPort(Integer dstPort) {
		put("dst_port", dstPort);
	}

	public String getProtocol() {
		return (String) get("protocol");
	}

	public void setProtocol(String protocol) {
		put("protocol", protocol);
	}

	public String getRule() {
		return (String) get("rule");
	}

	public void setRule(String rule) {
		put("rule", rule);
	}

	public String getDetail() {
		return (String) get("detail");
	}

	public void setDetail(String detail) {
		put("detail", detail);
	}

	public String getCve() {
		return (String) get("cve");
	}

	public void setCve(String cve) {
		put("cve", cve);
	}

	public int getCount() {
		if (!containsKey("count"))
			return 0;
		return (Integer) get("count");
	}

	public void setCount(int count) {
		put("count", count);
	}

}
