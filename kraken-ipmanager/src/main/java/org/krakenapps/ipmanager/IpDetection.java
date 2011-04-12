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

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class IpDetection {
	private String agentGuid;
	private Date date;
	private MacAddress mac;
	private InetAddress ip;
	private String hostName;
	private String workGroup;
	private String category;
	private String vendor;

	public IpDetection(String agentGuid, Date date, MacAddress mac, InetAddress ip) {
		this(agentGuid, date, mac, ip, null, null, null, null);
	}

	public IpDetection(String agentGuid, Date date, MacAddress mac, InetAddress ip, String hostName, String workGroup,
			String category, String vendor) {
		this.agentGuid = agentGuid;
		this.date = date;
		this.mac = mac;
		this.ip = ip;
		this.hostName = hostName;
		this.workGroup = workGroup;
		this.category = category;
		this.vendor = vendor;
	}

	public String getAgentGuid() {
		return agentGuid;
	}

	public Date getDate() {
		return date;
	}

	public MacAddress getMac() {
		return mac;
	}

	public InetAddress getIp() {
		return ip;
	}

	public String getHostName() {
		return hostName;
	}

	public String getWorkGroup() {
		return workGroup;
	}

	public String getCategory() {
		return category;
	}

	public String getVendor() {
		return vendor;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("date=%s, ip=%s, mac=%s, agent=%s, host=%s, workgroup=%s, category=%s, vendor=%s",
				dateFormat.format(date), ip, mac, agentGuid, hostName, workGroup, category, vendor);
	}

}
