package org.krakenapps.pcap.decoder.smb.trans2struct.findinfolevelstruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbFindFileNamesInfo implements TransStruct{

	int nextEntryOffset;
	int fileIndex;
	int fileNameLength;
	String fileName;
	public int getNextEntryoffset() {
		return nextEntryOffset;
	}
	public void setNextEntryoffset(int nextEntryoffset) {
		this.nextEntryOffset = nextEntryoffset;
	}
	public int getFileIndex() {
		return fileIndex;
	}
	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}
	public int getFileNameLength() {
		return fileNameLength;
	}
	public void setFileNameLength(int fileNameLength) {
		this.fileNameLength = fileNameLength;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public TransStruct parse(Buffer b , SmbSession session){
		nextEntryOffset = ByteOrderConverter.swap(b.getInt());
		fileIndex = ByteOrderConverter.swap(b.getInt());
		fileNameLength = ByteOrderConverter.swap(b.getInt());
		fileName = NetBiosNameCodec.readSmbUnicodeName(b, fileNameLength);
		b.reset();
		b.skip(nextEntryOffset);
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Find File Name info\n" +
				"nextEntryOffset = 0x%s , fileIndex = 0x%s , fileNameLength = 0x%s\n" +
				"fileName = %s\n",
				Integer.toHexString(this.nextEntryOffset) , Integer.toHexString(this.fileIndex) , Integer.toHexString(this.fileNameLength),
				this.fileName);
	}
}
