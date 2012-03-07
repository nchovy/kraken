package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFileStandardInfo implements TransStruct {

	long allocationSize;
	long endOfFile;
	int numberOfLinks;
	byte deletePending;
	byte directory;

	public long getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(long allocationSize) {
		this.allocationSize = allocationSize;
	}

	public long getEndOfFile() {
		return endOfFile;
	}

	public void setEndOfFile(long endOfFile) {
		this.endOfFile = endOfFile;
	}

	public int getNumberOfLinks() {
		return numberOfLinks;
	}

	public void setNumberOfLinks(int numberOfLinks) {
		this.numberOfLinks = numberOfLinks;
	}

	public byte getDeletePending() {
		return deletePending;
	}

	public void setDeletePending(byte deletePending) {
		this.deletePending = deletePending;
	}

	public byte getDirectory() {
		return directory;
	}

	public void setDirectory(byte directory) {
		this.directory = directory;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		allocationSize = ByteOrderConverter.swap(b.getLong());
		endOfFile = ByteOrderConverter.swap(b.getLong());
		numberOfLinks = ByteOrderConverter.swap(b.getInt());
		deletePending = b.get();
		directory = b.get();
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
