package org.krakenapps.sonar.passive.fingerprint;

public class HttpApplicationMetaData {
	private String vendor;
	private String name;
	private String version;

	public HttpApplicationMetaData(String vendor, String name, String version) {
		this.vendor = vendor;
		this.name = name;
		this.version = version;
	}

	public String getVendor() {
		return vendor;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return String.format("vendor=%s, name=%s, version=%s", vendor, name, version);
	}
}
