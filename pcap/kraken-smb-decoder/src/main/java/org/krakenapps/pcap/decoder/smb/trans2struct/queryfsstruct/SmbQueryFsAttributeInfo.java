package org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFsAttributeInfo implements TransStruct{

	int fileSystemAttributes;
	int maxFileNameLengthInBytes;
	int lengthOfFileSystemName;
	String fileSystemName;
	public int getFileSystemAttributes() {
		return fileSystemAttributes;
	}
	public void setFileSystemAttributes(int fileSystemAttributes) {
		this.fileSystemAttributes = fileSystemAttributes;
	}
	public int getMaxFileNameLengthInBytes() {
		return maxFileNameLengthInBytes;
	}
	public void setMaxFileNameLengthInBytes(int maxFileNameLengthInBytes) {
		this.maxFileNameLengthInBytes = maxFileNameLengthInBytes;
	}
	public String getFileSystemName() {
		return fileSystemName;
	}
	public void setFileSystemName(String fileSystemName) {
		this.fileSystemName = fileSystemName;
	}
	public TransStruct parse(Buffer b , SmbSession session){
		fileSystemAttributes = ByteOrderConverter.swap(b.getInt());
		maxFileNameLengthInBytes = ByteOrderConverter.swap(b.getInt());
		lengthOfFileSystemName =ByteOrderConverter.swap(b.getInt());
		fileSystemName = NetBiosNameCodec.readSmbUnicodeName(b , lengthOfFileSystemName);
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Info Fs Attribute Info\n" +
				"fileSystemAttributes = 0x%s , maxFileNameLengthInBytes = 0x%s , lengthOfFileSystemName = 0x%s\n" +
				"fileSystemName = %s",
				Integer.toHexString(this.fileSystemAttributes) ,Integer.toHexString(this.maxFileNameLengthInBytes) , Integer.toHexString(this.lengthOfFileSystemName),
				this.fileSystemName);
	}
}
