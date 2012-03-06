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
package org.krakenapps.dhcp.model;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.dhcp.MacAddress;

public class DhcpIpLease {
	private String groupName;
	private InetAddress ip;
	private MacAddress mac;
	private String hostName;
	private Date expire;
	private Date created;
	private Date updated;

	public DhcpIpLease(String groupName, InetAddress ip, MacAddress mac, String hostName, int leaseDuration) {
		this(groupName, ip, mac, hostName, new Date(new Date().getTime() + leaseDuration * 1000));
	}

	public DhcpIpLease(String groupName, InetAddress ip, MacAddress mac, String hostName, Date expire) {
		this(groupName, ip, mac, hostName, expire, new Date(), new Date());
	}

	public DhcpIpLease(String groupName, InetAddress ip, MacAddress mac, String hostName, Date expire, Date created,
			Date updated) {
		this.groupName = groupName;
		this.ip = ip;
		this.mac = mac;
		this.hostName = hostName;
		this.created = created;
		this.updated = updated;
		this.expire = expire;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public MacAddress getMac() {
		return mac;
	}

	public void setMac(MacAddress mac) {
		this.mac = mac;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}

	public void setNewExpire(int duration) {
		this.expire = new Date(new Date().getTime() + duration * 1000);
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("ip=%s, mac=%s, name=%s, expire=%s",
				ip.getHostAddress(), mac, hostName, dateFormat.format(expire));
	}

}
