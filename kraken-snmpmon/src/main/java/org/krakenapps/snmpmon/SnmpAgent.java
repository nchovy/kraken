package org.krakenapps.snmpmon;

public class SnmpAgent {
	private int id;
	private int organizationId;
	private int hostId;
	private transient String hostGuid;
	private int snmpVersion;
	private String ip;
	private int port;
	private String community;
	private boolean isEnabled;

	public SnmpAgent(int hostId) {
		// use only when create mock object;
		this.organizationId = Integer.MAX_VALUE;
		this.hostId = hostId;
		this.id = hostId;
	}

	public SnmpAgent() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public int getHostId() {
		return hostId;
	}

	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	public String getHostGuid() {
		return hostGuid;
	}

	public void setHostGuid(String hostGuid) {
		this.hostGuid = hostGuid;
	}

	public int getSnmpVersion() {
		return snmpVersion;
	}

	public void setSnmpVersion(int snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((community == null) ? 0 : community.hashCode());
		result = prime * result + hostId;
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
		SnmpAgent other = (SnmpAgent) obj;
		if (community == null) {
			if (other.community != null)
				return false;
		} else if (!community.equals(other.community))
			return false;
		if (hostId != other.hostId)
			return false;
		return true;
	}
}
