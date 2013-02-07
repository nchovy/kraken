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

public class PushCondition {
	private Key k;
	private Map<String, Object> options;

	public PushCondition(Integer orgId, int sessionId, int processId, String callback, Map<String, Object> options) {
		this.k = new Key(orgId, Integer.toString(sessionId), processId, callback);
		this.options = options;
	}

	public PushCondition(String orgDomain, int sessionId, int processId, String callback, Map<String, Object> options) {
		this.k = new Key(orgDomain, Integer.toString(sessionId), processId, callback);
		this.options = options;
	}

	public PushCondition(String orgDomain, String sessionId, int processId, String callback, Map<String, Object> options) {
		this.k = new Key(orgDomain, sessionId, processId, callback);
		this.options = options;
	}

	public Key getKey() {
		return k;
	}

	@Deprecated
	public Integer getOrgId() {
		return k.orgId;
	}

	public String getOrgDomain() {
		return k.orgDomain;
	}

	public void setOrgId(int orgId) {
		this.k.orgId = orgId;
	}

	public String getSessionId() {
		return k.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.k.sessionId = sessionId;
	}

	public int getProcessId() {
		return k.processId;
	}

	public void setProcessId(int processId) {
		this.k.processId = processId;
	}

	public String getCallback() {
		return k.callback;
	}

	public void setCallback(String callback) {
		this.k.callback = callback;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	public static class Key {
		private Integer orgId;
		private String orgDomain;
		private String sessionId;
		private int processId;
		private String callback;

		public Key(Integer orgId, String sessionId, int processId, String callback) {
			this.orgId = orgId;
			this.sessionId = sessionId;
			this.processId = processId;
			this.callback = callback;
		}

		public Key(String orgDomain, String sessionId, int processId, String callback) {
			this.orgDomain = orgDomain;
			this.sessionId = sessionId;
			this.processId = processId;
			this.callback = callback;
		}

		@Deprecated
		public Integer getOrgId() {
			return orgId;
		}

		public String getOrgDomain() {
			return orgDomain;
		}

		public String getSessionId() {
			return sessionId;
		}

		public int getProcessId() {
			return processId;
		}

		public String getCallback() {
			return callback;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((callback == null) ? 0 : callback.hashCode());
			result = prime * result + ((orgDomain == null) ? 0 : orgDomain.hashCode());
			result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
			result = prime * result + processId;
			result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (callback == null) {
				if (other.callback != null)
					return false;
			} else if (!callback.equals(other.callback))
				return false;
			if (orgDomain == null) {
				if (other.orgDomain != null)
					return false;
			} else if (!orgDomain.equals(other.orgDomain))
				return false;
			if (orgId == null) {
				if (other.orgId != null)
					return false;
			} else if (!orgId.equals(other.orgId))
				return false;
			if (processId != other.processId)
				return false;
			if (sessionId != other.sessionId)
				return false;
			return true;
		}
	}
}
