package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;

public class SetFsInformationRequest implements TransData{
//not implement
	short fid;
	short  informatoinLevel;
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getInformatoinLevel() {
		return informatoinLevel;
	}
	public void setInformatoinLevel(short informatoinLevel) {
		this.informatoinLevel = informatoinLevel;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Set Fs Information Request\n" +
				"fid = 0x%s , informationLevel = 0x%s\n",
				Integer.toHexString(this.fid), Integer.toHexString(this.informatoinLevel));
	}
}
