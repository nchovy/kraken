package org.krakenapps.msgbus;

public class MsgbusException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String groupId;
	private String errorCode;
	private String[] properties;

	public MsgbusException(String groupId, String errorCode) {
		this.groupId = groupId;
		this.errorCode = errorCode;
	}

	public MsgbusException(String groupId, String errorCode, String[] properties) {
		this.groupId = groupId;
		this.errorCode = errorCode;
		this.properties = properties;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String[] getProperties() {
		return properties;
	}

	public void setProperties(String[] properties) {
		this.properties = properties;
	}

}
