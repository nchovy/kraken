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
