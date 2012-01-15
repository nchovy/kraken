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
package org.krakenapps.httpd.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class HttpSessionImpl implements HttpSession {
	/**
	 * 30min timeout by default
	 */
	private static final int DEFAULT_IDLE_TIMEOUT = 1800;

	/**
	 * http response will send Set-Cookie header if session is new
	 */
	private boolean isNew;

	/**
	 * session key
	 */
	private String id;

	/**
	 * creation time
	 */
	private Date created;

	/**
	 * last accessed time before current access
	 */
	private Date lastHit;

	/**
	 * max idle timeout in seconds
	 */
	private int maxInactiveInterval;

	/**
	 * session can be concurrently accessed by multiple threads
	 */
	private ConcurrentMap<String, Object> attributes;

	/**
	 * TODO: it should have request object for setcookie operation (header flush
	 * check)
	 */
	public HttpSessionImpl(String id) {
		this.id = id;
		this.isNew = true;
		this.created = new Date();
		this.lastHit = new Date();
		this.maxInactiveInterval = DEFAULT_IDLE_TIMEOUT;
		this.attributes = new ConcurrentHashMap<String, Object>();
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
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public void setLastAccess(Date lastAccess) {
		this.lastHit = lastAccess;
	}
}
