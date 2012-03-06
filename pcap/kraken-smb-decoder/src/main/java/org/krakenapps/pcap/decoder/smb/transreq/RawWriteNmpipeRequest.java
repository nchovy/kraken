package org.krakenapps.pcap.decoder.smb.transreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class RawWriteNmpipeRequest implements TransData{
	short subcommand;
	short fid;
	byte []writeData;
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
		return writeData;
	}
	public void setWriteData(byte[] writeData) {
		this.writeData = writeData;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
