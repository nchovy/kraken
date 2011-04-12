package org.krakenapps.api;

public class BundleDescriptor {
	private long id;
	private String symbolicName;
	private String version;

	public BundleDescriptor(long id, String symbolicName, String version) {
		this.id = id;
		this.symbolicName = symbolicName;
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		BundleDescriptor other = (BundleDescriptor) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public long getBundleId() {
		return id;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getVersion() {
		return version;
	}

}
