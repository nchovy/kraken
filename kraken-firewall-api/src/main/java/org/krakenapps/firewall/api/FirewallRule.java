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
package org.krakenapps.firewall.api;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FirewallRule {
	private InetAddress sourceIp;
	private Date expire;

	public FirewallRule(InetAddress sourceIp, Date expire) {
		this.sourceIp = sourceIp;
		this.expire = expire;
	}

	public InetAddress getSourceIp() {
		return sourceIp;
	}

	public Date getExpire() {
		return expire;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "block " + sourceIp.getHostAddress() + ", expire=" + dateFormat.format(expire) + "]";
	}
}
