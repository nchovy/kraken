package org.krakenapps.logdb.impl;

import java.util.Map;

import org.krakenapps.logdb.DataSource;

public class RpcDataSource implements DataSource {
	private String nodeGuid;
	private String name;
	private Map<String, Object> metadata;

	public RpcDataSource(String nodeGuid, String name) {
		this(nodeGuid, name, null);
	}

	public RpcDataSource(String nodeGuid, String name, Map<String, Object> metadata) {
		this.nodeGuid = nodeGuid;
		this.name = name;
		this.metadata = metadata;
	}

	@Override
	public String getNodeGuid() {
		return nodeGuid;
	}

	@Override
	public String getType() {
		return "rpc";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeGuid == null) ? 0 : nodeGuid.hashCode());
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
		RpcDataSource other = (RpcDataSource) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeGuid == null) {
			if (other.nodeGuid != null)
				return false;
		} else if (!nodeGuid.equals(other.nodeGuid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "rpc: node=" + nodeGuid + ", name=" + name;
	}

}
