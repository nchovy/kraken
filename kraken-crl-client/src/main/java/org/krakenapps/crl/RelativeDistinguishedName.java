package org.krakenapps.crl;

public class RelativeDistinguishedName {
	private String oid;
	private X500Attribute attr;
	private String value;

	public RelativeDistinguishedName(String oid, X500Attribute attr, String value) {
		this.oid = oid;
		this.attr = attr;
		this.value = value;
	}
	
	public String getOid() {
		return oid;
	}

	public X500Attribute getAttr() {
		return attr;
	}

	public String getValue() {
		return value;
	}
}
