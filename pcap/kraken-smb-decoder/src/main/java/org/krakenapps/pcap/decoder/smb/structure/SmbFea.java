package org.krakenapps.pcap.decoder.smb.structure;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbFea {
	byte extendedAttrbuteFlag;
	byte attributeNameLengthInBytes;
	short attributeValueLengthInBytes;
	String attributeName; // atributeNameLengthInBytes+1 null terminate extended ASCII
	byte[] attributeValue;// attributeValueLengthInBytes extended ASCII
	public SmbFea parse(Buffer b){
		extendedAttrbuteFlag = b.get();
		attributeNameLengthInBytes =b.get();
		attributeValueLengthInBytes = ByteOrderConverter.swap(b.getShort());
		attributeName = NetBiosNameCodec.readOemName(b,attributeNameLengthInBytes+1);
		System.out.println("attributeValuelengthInBytes = " + attributeValueLengthInBytes);
		attributeValue = new byte[attributeValueLengthInBytes];
		b.gets(attributeValue);
		return this;
	}
	public byte getExtendedAttrbuteFlag() {
		return extendedAttrbuteFlag;
	}
	public void setExtendedAttrbuteFlag(byte extendedAttrbuteFlag) {
		this.extendedAttrbuteFlag = extendedAttrbuteFlag;
	}
	public byte getAttributeNameLengthInBytes() {
		return attributeNameLengthInBytes;
	}
	public void setAttributeNameLengthInBytes(byte attributeNameLengthInBytes) {
		this.attributeNameLengthInBytes = attributeNameLengthInBytes;
	}
	public short getAttributeValueLengthInBytes() {
		return attributeValueLengthInBytes;
	}
	public void setAttributeValueLengthInBytes(short attributeValueLengthInBytes) {
		this.attributeValueLengthInBytes = attributeValueLengthInBytes;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public byte[] getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(byte[] attributeValue) {
		this.attributeValue = attributeValue;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
