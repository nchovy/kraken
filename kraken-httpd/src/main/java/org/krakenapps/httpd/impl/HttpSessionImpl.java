package org.krakenapps.httpd.impl;

import java.nio.channels.Channel;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class HttpSessionImpl implements HttpSession {
	/**
	 * session key
	 */
	private String id;

	/**
	 * creation time
	 */
	private Date created;

	/**
	 * last accessed time
	 */
	private Date lastHit;

	private int maxInactiveInterval;

	private Map<String, Object> attributes;

	public HttpSessionImpl(String id, Channel channel) {
		this.id = id;
		this.created = new Date();
		this.attributes = new HashMap<String, Object>();
		attributes.put("netty.channel", channel);
	}

	@Override
	public long getCreationTime() {
		return created.getTime();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return lastHit.getTime();
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public String[] getValueNames() {
		String[] names = new String[attributes.keySet().size()];
		int i = 0;
		for (String key : attributes.keySet())
			names[i++] = key;
		return names;
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public boolean isNew() {
		return false;
	}
}
