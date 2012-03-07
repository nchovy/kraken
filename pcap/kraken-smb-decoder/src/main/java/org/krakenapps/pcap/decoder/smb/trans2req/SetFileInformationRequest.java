package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;

public class SetFileInformationRequest implements TransData{
	short subcommand;
	short fid;
	short informaiotnLevel;
	short reserved;
	TransStruct struct;
	
	public TransStruct getStruct() {
		return struct;
	}
	public void setStruct(TransStruct struct) {
		this.struct = struct;
	}
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
	public short getInformaiotnLevel() {
		return informaiotnLevel;
	}
	public void setInformaiotnLevel(short informaiotnLevel) {
		this.informaiotnLevel = informaiotnLevel;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
	}
	// data is reserved type each Information types
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Set File Information Request\n" +
				"subCommand = 0x%s\n" +
				"fid = 0x%s , informationLevel = 0x%s, reserved = 0x%s\n",
				Integer.toHexString(this.subcommand),
				Integer.toHexString(this.fid), Integer.toHexString(this.informaiotnLevel) , Integer.toHexString(this.reserved));
	}
}
