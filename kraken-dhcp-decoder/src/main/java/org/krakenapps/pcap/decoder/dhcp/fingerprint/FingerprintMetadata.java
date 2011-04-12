package org.krakenapps.pcap.decoder.dhcp.fingerprint;

public class FingerprintMetadata {
	private String category;
	private String vendor;
	private String family;
	private String description;

	public FingerprintMetadata(String category, String vendor, String family, String description) {
		this.category = category;
		this.vendor = vendor;
		this.family = family;
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public String getVendor() {
		return vendor;
	}

	public String getFamily() {
		return family;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return String.format("category=%s, vendor=%s, family=%s, description=%s", category, vendor, family, description);
	}

}