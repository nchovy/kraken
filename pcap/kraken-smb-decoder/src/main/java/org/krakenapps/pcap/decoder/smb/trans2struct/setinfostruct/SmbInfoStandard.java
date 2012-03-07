package org.krakenapps.pcap.decoder.smb.trans2struct.setinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbInfoStandard implements TransStruct{

	short creationDate;
	short creationTime;
	short lastAccessDate;
	short lastaccessTime;
	short lastWriteDate; 
	short lastWriteTime;
	byte []reserved;// new byte[10];
	
	public short getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(short creationDate) {
		this.creationDate = creationDate;
	}

	public short getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(short creationTime) {
		this.creationTime = creationTime;
	}

	public short getLastAccessDate() {
		return lastAccessDate;
	}

	public void setLastAccessDate(short lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}

	public short getLastaccessTime() {
		return lastaccessTime;
	}

	public void setLastaccessTime(short lastaccessTime) {
		this.lastaccessTime = lastaccessTime;
	}

	public short getLastWriteDate() {
		return lastWriteDate;
	}

	public void setLastWriteDate(short lastWriteDate) {
		this.lastWriteDate = lastWriteDate;
	}

	public short getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(short lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public byte[] getReserved() {
		return reserved;
	}

	public void setReserved(byte[] reserved) {
		this.reserved = reserved;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		creationDate = ByteOrderConverter.swap(b.getShort());
		creationTime = ByteOrderConverter.swap(b.getShort());
		lastAccessDate = ByteOrderConverter.swap(b.getShort());
		lastaccessTime = ByteOrderConverter.swap(b.getShort());
		lastWriteDate = ByteOrderConverter.swap(b.getShort()); 
		lastWriteTime = ByteOrderConverter.swap(b.getShort());
		reserved = new byte[10];
		b.gets(reserved);
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
