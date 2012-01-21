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
