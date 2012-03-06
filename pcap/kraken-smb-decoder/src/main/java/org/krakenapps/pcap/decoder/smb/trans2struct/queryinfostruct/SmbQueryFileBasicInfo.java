package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFileBasicInfo implements TransStruct{

	long creationTime;
	long lastAccessTime;
	long lastWriteTime;
	long lastChangeTime;
	ExtFileAttributes extFileAttributes;
	int reserved;
	
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public long getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public long getLastChangeTime() {
		return lastChangeTime;
	}

	public void setLastChangeTime(long lastChangeTime) {
		this.lastChangeTime = lastChangeTime;
	}

	public ExtFileAttributes getExtFileAttributes() {
		return extFileAttributes;
	}

	public void setExtFileAttributes(ExtFileAttributes extFileAttributes) {
		this.extFileAttributes = extFileAttributes;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		creationTime = ByteOrderConverter.swap(b.getLong());
		lastAccessTime = ByteOrderConverter.swap(b.getLong());
		lastWriteTime = ByteOrderConverter.swap(b.getLong());
		lastChangeTime = ByteOrderConverter.swap(b.getLong());
		extFileAttributes = ExtFileAttributes.parse(ByteOrderConverter.swap(b.getInt()));
		reserved = ByteOrderConverter.swap(b.getInt());
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
