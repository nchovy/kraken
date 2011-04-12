package org.krakenapps.pcap.decoder.smb.trans2struct.findinfolevelstruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.request.Transaction2Request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.rr.Transaction2Command;
import org.krakenapps.pcap.decoder.smb.trans2req.FindFirst2Request;
import org.krakenapps.pcap.decoder.smb.trans2req.FindNext2Request;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;

public class SmbInfoStandard implements TransStruct {
	int resumekey; // optional
	int creationDate;
	int creationTime;
	int lastAccessDate;
	int lastAccessTime;
	int lastWriteDate;
	int lastWriteTime;
	int fileDataSize;
	int allocationSize;
	FileAttributes attributes;
	byte fileNameLength;
	String fileName;

	public int getResumekey() {
		return resumekey;
	}

	public void setResumekey(int resumekey) {
		this.resumekey = resumekey;
	}

	public int getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(int creationDate) {
		this.creationDate = creationDate;
	}

	public int getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(int creationTime) {
		this.creationTime = creationTime;
	}

	public int getLastAccessDate() {
		return lastAccessDate;
	}

	public void setLastAccessDate(int lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}

	public int getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(int lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public int getLastWriteDate() {
		return lastWriteDate;
	}

	public void setLastWriteDate(int lastWriteDate) {
		this.lastWriteDate = lastWriteDate;
	}

	public int getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(int lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public int getFileDataSize() {
		return fileDataSize;
	}

	public void setFileDataSize(int fileDataSize) {
		this.fileDataSize = fileDataSize;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}

	public FileAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(FileAttributes attributes) {
		this.attributes = attributes;
	}

	public byte getFileNameLength() {
		return fileNameLength;
	}

	public void setFileNameLength(byte fileNameLength) {
		this.fileNameLength = fileNameLength;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public TransStruct parse(Buffer b, SmbSession session) {
		Buffer tmp = new ChainBuffer();
		tmp.addLast(((Transaction2Request) session.getUseSessionData())
				.getSetup());
		short subCommand = ByteOrderConverter.swap(tmp.getShort());
		if(Transaction2Command.parse(subCommand)==Transaction2Command.TRANS2_FIND_FIRST2){
			if(((FindFirst2Request)((Transaction2Request) session.getUseSessionData()).getTransaction2Data()).isFindReturnResumeKeys()){
				resumekey = ByteOrderConverter.swap(b.getInt());
			}
		}
		else if(Transaction2Command.parse(subCommand)==Transaction2Command.TRANS2_FIND_NEXT2){
			if(((FindNext2Request)((Transaction2Request) session.getUseSessionData()).getTransaction2Data()).isFindReturnResumeKeys()){
				resumekey = ByteOrderConverter.swap(b.getInt());
			}
		}
		creationDate = ByteOrderConverter.swap(b.getShort());
		creationTime = ByteOrderConverter.swap(b.getShort());
		lastAccessDate = ByteOrderConverter.swap(b.getShort());
		lastAccessTime = ByteOrderConverter.swap(b.getShort());
		lastWriteDate = ByteOrderConverter.swap(b.getShort());
		lastWriteTime = ByteOrderConverter.swap(b.getShort());
		fileDataSize = ByteOrderConverter.swap(b.getInt());
		allocationSize = ByteOrderConverter.swap(b.getInt());
		attributes = FileAttributes.parse(ByteOrderConverter.swap(b.getShort()));
		fileNameLength = b.get();
		fileName = NetBiosNameCodec.readSmbUnicodeName(b, fileNameLength);
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Info Standard\n" +
				"resumeKey = %s\n" +
				"creationData = 0x%s , creationTime = 0x%s, lastAccessDate = 0x%s\n" +
				"lastAccessTime = 0x%s , lastWriteDate = 0x%s , lastWriteTime = 0x%s\n" +
				"fileDataSize = 0x%s , allocationSize = 0x%s, attributes = %s\n" +
				"fileNameLength = 0x%s" +
				"fileName = %s\n",
				this.resumekey,
				Integer.toHexString(this.creationDate),Integer.toHexString(this.creationDate),Integer.toHexString(this.lastAccessDate),
				Integer.toHexString(this.lastAccessTime) , Integer.toHexString(this.lastWriteDate) , Integer.toHexString(this.lastWriteTime),
				Integer.toHexString(this.fileDataSize) , Integer.toHexString(this.allocationSize) , this.attributes,
				Integer.toHexString(this.fileNameLength),
				this.fileName);
	}
}
