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
