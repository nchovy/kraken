package org.krakenapps.sleepproxy.impl;

import java.util.ArrayList;
import java.util.List;

public class LogMessage {
	private int version;
	private int msgType;
	private String guid;
	private String userName;
	private String hostName;
	private String domain;
	private List<NicInfo> networkAdapters = new ArrayList<NicInfo>();
	private String rawLog;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public List<NicInfo> getNetworkAdapters() {
		return networkAdapters;
	}

	public void setNetworkAdapters(List<NicInfo> networkAdapters) {
		this.networkAdapters = networkAdapters;
	}

	public String getRawLog() {
		return rawLog;
	}

	public void setRawLog(String rawLog) {
		this.rawLog = rawLog;
	}

}
