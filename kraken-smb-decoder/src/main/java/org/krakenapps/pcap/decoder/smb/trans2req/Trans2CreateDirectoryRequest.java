package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbFeaList;

public class Trans2CreateDirectoryRequest implements TransData{
	short subcommand;
	int reserved;
	String directoryName;
	SmbFeaList extendedAttributeList;
	public short getSubcommand() {
		return subcommand;
	}
	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	public String getDirectoryName() {
		return directoryName;
	}
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}
	public SmbFeaList getExtendedAttributeList() {
		return extendedAttributeList;
	}
	public void setExtendedAttributeList(SmbFeaList extendedAttributeList) {
		this.extendedAttributeList = extendedAttributeList;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Trans2 Create Directory Request\n" +
				"subCommand = 0x%s\n" +
				"reserved = 0x%s\n" +
				"directoryName = %s\n" +
				"extendedAtrributeList = %s\n",
				Integer.toHexString(this.subcommand),
				Integer.toHexString(this.reserved),
				this.directoryName,
				this.extendedAttributeList);
	}
}
