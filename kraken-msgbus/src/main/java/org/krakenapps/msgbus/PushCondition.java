package org.krakenapps.msgbus;

import java.util.Map;

public class PushCondition {
	private Key k;
	private Map<String, Object> options;

	public PushCondition(int orgId, int sessionId, int processId, String callback, Map<String, Object> options) {
		this.k = new Key(orgId, sessionId, processId, callback);
		this.options = options;
	}

	public Key getKey() {
		return k;
	}

	public int getOrgId() {
		return k.orgId;
	}

	public void setOrgId(int orgId) {
		this.k.orgId = orgId;
	}

	public int getSessionId() {
		return k.sessionId;
	}

	public void setSessionId(int sessionId) {
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
		private int orgId;
		private int sessionId;
		private int processId;
		private String callback;

		public Key(int orgId, int sessionId, int processId, String callback) {
			this.orgId = orgId;
			this.sessionId = sessionId;
			this.processId = processId;
			this.callback = callback;
		}

		public int getOrgId() {
			return orgId;
		}

		public int getSessionId() {
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
			result = prime * result + orgId;
			result = prime * result + processId;
			result = prime * result + sessionId;
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
			if (orgId != other.orgId)
				return false;
			if (processId != other.processId)
				return false;
			if (sessionId != other.sessionId)
				return false;
			return true;
		}
	}
}
