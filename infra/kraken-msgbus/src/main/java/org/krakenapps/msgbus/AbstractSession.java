package org.krakenapps.msgbus;

import java.net.InetAddress;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSession implements Session {
	private String guid;
	private Map<String, Object> params;
	private Date lastAccessTime = new Date();

	public AbstractSession() {
		this.guid = UUID.randomUUID().toString();
		this.params = new ConcurrentHashMap<String, Object>();
	}

	@Override
	public Locale getLocale() {
		if (!params.containsKey("locale"))
			return new Locale("en");

		return new Locale((String) params.get("locale"));
	}

	@Override
	public String getOrgDomain() {
		return getString("org_domain");
	}

	@Override
	public String getAdminLoginName() {
		return getString("admin_login_name");
	}

	@Override
	public boolean has(String key) {
		return params.containsKey(key);
	}

	public Object get(String key) {
		return params.get(key);
	}

	public String getString(String key) {
		return (String) params.get(key);
	}

	public Integer getInt(String key) {
		return (Integer) params.get(key);
	}

	public void setProperty(String key, Object value) {
		params.put(key, value);
	}

	public void unsetProperty(String key) {
		params.remove(key);
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public int getId() {
		return guid.hashCode();
	}

	@Override
	public InetAddress getLocalAddress() {
		return null;
	}

	@Override
	public InetAddress getRemoteAddress() {
		return null;
	}

	@Override
	public void send(Message msg) {
	}

	@Override
	public void close() {
	}

	@Override
	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	@Override
	public void setLastAccessTime() {
		this.lastAccessTime = new Date();
	}
}
