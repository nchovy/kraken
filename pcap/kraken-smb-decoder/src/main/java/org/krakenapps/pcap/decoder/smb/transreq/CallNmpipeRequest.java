package org.krakenapps.pcap.decoder.smb.transreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class CallNmpipeRequest implements TransData{
	short subcommand;
	short priority;
	byte []writeData;
	public short getSubcommand() {
		return subcommand;
	}
	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}
	public short getPriority() {
		return priority;
	}
	public void setPriority(short priority) {
		this.priority = priority;
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
