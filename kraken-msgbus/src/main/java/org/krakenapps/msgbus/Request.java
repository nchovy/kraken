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

	public Integer getOrgId() {
		return session.getOrgId();
	}

	public Integer getAdminId() {
		return session.getAdminId();
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
