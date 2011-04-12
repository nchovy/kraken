package org.krakenapps.pcap.decoder.smb.structure;

public class SmbGeaList {
	int sizeOfListinbytes;
	SmbGea []geaList;
	@Override
	public String toString(){
		return String.format("Structure : SmbGeaList" +
				"sizeOfListInBytes = 0x%s\n" +
				"smbGea = %s\n",
				Integer.toHexString(this.sizeOfListinbytes),
				this.geaList);
	}
}
