package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;

public class QueryFileInformationRequest implements TransData{

	short subcommand;
	short fid;
	short informationLevel;
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
	public short getInformationLevel() {
		return informationLevel;
	}
	public void setInformationLevel(short informationLevel) {
		this.informationLevel = informationLevel;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Query File Information Request\n" +
				"subCommand = 0x%s\n" +
				"fid = 0x%s,  informationLevel = 0x%s\n",
				Integer.toHexString(this.subcommand),
				Integer.toHexString(this.fid), Integer.toHexString(this.informationLevel));
	}
}
