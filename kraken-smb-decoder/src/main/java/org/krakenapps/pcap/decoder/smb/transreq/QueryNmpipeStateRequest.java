package org.krakenapps.pcap.decoder.smb.transreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class QueryNmpipeStateRequest implements TransData{
	short subcommand;
	short fid;
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
	@Override
	public String toString(){
		return String.format("");
	}
}
