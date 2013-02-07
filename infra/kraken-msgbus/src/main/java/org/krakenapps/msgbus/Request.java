/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.msgbus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Request {
	private Session session;
	private Message msg;

	public Request(Session session, Message msg) {
		this.session = session;
		this.msg = msg;
	}

	public Session getSession() {
		return session;
	}

	public String getOrgDomain() {
		return session.getOrgDomain();
	}

	public String getAdminLoginName() {
		return session.getAdminLoginName();
	}

	public String getMethod() {
		return msg.getMethod();
	}

	public String getSource() {
		return msg.getSource();
	}

	public boolean has(String key) {
		return msg.getParameters().containsKey(key);
	}

	public Map<String, Object> getParams() {
		return msg.getParameters();
	}

	public Object get(String key) {
		return msg.getParameters().get(key);
	}

	public String getString(String key) {
		return msg.getStringParameter(key);
	}

	public Integer getInteger(String key) {
		return msg.getIntParameter(key);
	}

	public Boolean getBoolean(String key) {
		return msg.getBooleanParameter(key);
	}

	public Date getDate(String key) {
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String value = getString(key);

		if (has(key) && value != null) {
			try {
				return f1.parse(value);
			} catch (ParseException e) {
				try {
					return f2.parse(value);
				} catch (ParseException e1) {
				}
			}
		}

		return null;
	}
}
