package org.krakenapps.pcap.decoder.smb.structure;

public class SmbGea {
	byte attributeNameLengthBytes;
	String attributeName;
	
	public byte getAttributeNameLengthBytes() {
		return attributeNameLengthBytes;
	}

	public void setAttributeNameLengthBytes(byte attributeNameLengthBytes) {
		this.attributeNameLengthBytes = attributeNameLengthBytes;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public String toString(){
		return String.format("Structure : SmbGea\n" +
				"attributeNameLengthBytes = 0x%s\n" +
				"attributeName = %s\n",
				Integer.toHexString(this.attributeNameLengthBytes),
				this.attributeName);
	}
}
