package org.krakenapps.logdb.mapreduce;

public class RemoteQueryKey {
	private String nodeGuid;
	private int id;

	public RemoteQueryKey(String nodeGuid, int id) {
		this.nodeGuid = nodeGuid;
		this.id = id;
	}

	public String getNodeGuid() {
		return nodeGuid;
	}

	public void setNodeGuid(String nodeGuid) {
		this.nodeGuid = nodeGuid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		RemoteQueryKey other = (RemoteQueryKey) obj;
		if (id != other.id)
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
		return "node=" + nodeGuid + ", id=" + id;
	}

}
