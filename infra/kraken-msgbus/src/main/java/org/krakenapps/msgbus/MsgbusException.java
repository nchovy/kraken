package org.krakenapps.msgbus;

import java.util.Map;

public class MsgbusException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String groupId;
	private String errorCode;
	@Deprecated
	private String[] properties;
	private Map<String, Object> parameters;

	public MsgbusException(String groupId, String errorCode) {
		this.groupId = groupId;
		this.errorCode = errorCode;
	}

	@Deprecated
	public MsgbusException(String groupId, String errorCode, String[] properties) {
		this.groupId = groupId;
		this.errorCode = errorCode;
		this.properties = properties;
	}

	public MsgbusException(String groupId, String errorCode, Map<String, Object> parameters) {
		this.groupId = groupId;
		this.errorCode = errorCode;
		this.parameters = parameters;
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

	@Deprecated
	public String[] getProperties() {
		return properties;
	}

	@Deprecated
	public void setProperties(String[] properties) {
		this.properties = properties;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getMessage() {
		return groupId + ", " + errorCode + ", params " + parameters;
	}

}
