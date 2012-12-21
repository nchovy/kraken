/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.dns;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DnsCacheEntry {
	private DnsMessage response;
	private int ttl;
	private Date created = new Date();

	public DnsCacheEntry() {
	}

	public DnsCacheEntry(DnsMessage response, int ttl) {
		this.response = response;
		this.ttl = ttl;
	}

	public DnsMessage getResponse() {
		return response;
	}

	public void setResponse(DnsMessage response) {
		this.response = response;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return response + ", ttl=" + ttl + ", created=" + dateFormat.format(created);
	}

}
