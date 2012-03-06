package org.krakenapps.pcap.decoder.smb.transreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class WriteNmpipeRequest implements TransData{
	short subcommand;
	short fid;
	byte []WriteData;
	public short getSubcommand() {
		return subcommand;
	}
	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public byte[] getWriteData() {
		return WriteData;
	}
	public void setWriteData(byte[] writeData) {
		WriteData = writeData;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
