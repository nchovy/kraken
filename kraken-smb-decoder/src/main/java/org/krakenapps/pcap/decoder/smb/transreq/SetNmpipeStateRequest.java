package org.krakenapps.pcap.decoder.smb.transreq;

import org.krakenapps.pcap.decoder.smb.TransData;

public class SetNmpipeStateRequest implements TransData{
	short subCommand;
	short fid;
	short pipeState;
	public short getSubCommand() {
		return subCommand;
	}
	public void setSubCommand(short subCommand) {
		this.subCommand = subCommand;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getPipeState() {
		return pipeState;
	}
	public void setPipeState(short pipeState) {
		this.pipeState = pipeState;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
