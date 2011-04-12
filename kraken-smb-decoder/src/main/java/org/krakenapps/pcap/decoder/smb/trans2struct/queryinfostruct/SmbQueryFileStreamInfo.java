package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFileStreamInfo implements TransStruct{

	int nextEnntryOffset;
	int streamNameLength;
	long streamSize;
	long streamAllocationSize;
	String streamName; // streamNameLength *2 bytes;
	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		nextEnntryOffset = ByteOrderConverter.swap(b.getInt());
		streamNameLength = ByteOrderConverter.swap(b.getInt());
		streamSize = ByteOrderConverter.swap(b.getLong());
		streamAllocationSize = ByteOrderConverter.swap(b.getLong());
		streamName = NetBiosNameCodec.readSmbUnicodeName(b, streamNameLength);
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
