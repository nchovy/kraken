package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.QueryInfoRequest;
import org.krakenapps.pcap.decoder.smb.response.QueryInfoResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
// 0x08
public class QueryInfoParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		QueryInfoRequest data = new QueryInfoRequest();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		data.setBufferFormat(b.get());
		if(h.isFlag2Unicode()){
			data.setFileName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setFileName(NetBiosNameCodec.readOemName(b));
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		QueryInfoResponse data = new QueryInfoResponse();
		byte[]reserved = new byte[10];
		data.setWordCount(b.get());
		data.setFileAttributes( FileAttributes.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
		data.setLastWriteTime(ByteOrderConverter.swap(b.getInt()));
		data.setFileSize(ByteOrderConverter.swap(b.getInt()));
		b.gets(reserved);
		data.setReserved(reserved);
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
