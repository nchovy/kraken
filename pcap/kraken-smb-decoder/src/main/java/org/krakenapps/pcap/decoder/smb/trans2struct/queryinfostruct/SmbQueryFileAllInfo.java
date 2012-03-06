package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFileAllInfo implements TransStruct{

	long creationTime;
	long lastAccessTime;
	long lastWriteTime;
	long lastChangeTime;
	ExtFileAttributes extFileAttributes;
	int resrved1;
	long allocationSize;
	long endOfFile;
	int numberOfLinks;
	byte deletePending;
	byte directory;
	short reserved2;
	int eaSize;
	int fileNameLength;
	String fileName;
	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		creationTime = ByteOrderConverter.swap(b.getLong());
		lastAccessTime = ByteOrderConverter.swap(b.getLong());
		lastWriteTime = ByteOrderConverter.swap(b.getLong());
		lastChangeTime = ByteOrderConverter.swap(b.getLong());
		extFileAttributes = ExtFileAttributes.parse(ByteOrderConverter.swap(b.getInt()));
		resrved1 = ByteOrderConverter.swap(b.getInt());
		allocationSize = ByteOrderConverter.swap(b.getLong());
		endOfFile = ByteOrderConverter.swap(b.getLong());
		numberOfLinks = ByteOrderConverter.swap(b.getInt());
		deletePending = b.get();
		directory = b.get();
		reserved2 = ByteOrderConverter.swap(b.getShort());
		eaSize = ByteOrderConverter.swap(b.getInt());
		fileNameLength = ByteOrderConverter.swap(b.getInt());
		fileName = NetBiosNameCodec.readSmbUnicodeName(b, fileNameLength);
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
