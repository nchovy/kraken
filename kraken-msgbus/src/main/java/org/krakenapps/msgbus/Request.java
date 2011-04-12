package org.krakenapps.msgbus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		try {
			if (has(key))
				return dateFormat.parse(getString(key));
		} catch (ParseException e) {
		}
		return null;
	}
}
