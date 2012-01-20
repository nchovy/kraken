package org.krakenapps.httpd.impl;

import javax.servlet.SessionCookieConfig;

public class SessionCookieConfigImpl implements SessionCookieConfig {

	private String name;
	private String domain;
	private String path = "/";
	private String comment;
	private boolean secure;
	private boolean httpOnly;
	private int maxAge;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	@Override
	public boolean isHttpOnly() {
		return httpOnly;
	}

	@Override
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	@Override
	public int getMaxAge() {
		return maxAge;
	}
}
