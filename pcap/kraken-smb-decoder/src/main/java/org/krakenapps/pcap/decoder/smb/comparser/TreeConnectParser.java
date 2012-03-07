package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.TreeConnectRequest;
import org.krakenapps.pcap.decoder.smb.response.TreeConnectResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x70
public class TreeConnectParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		TreeConnectRequest data = new TreeConnectRequest();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		// TODO: buffer format
		data.setBufferFormat1(b.get());
		data.setPath(NetBiosNameCodec.readSmbUnicodeName(b));
		data.setBufferFormat2(b.get());
		data.setPassword(NetBiosNameCodec.readSmbUnicodeName(b));
		data.setBufferFormat3(b.get());
		data.setService(NetBiosNameCodec.readSmbUnicodeName(b));
		return null;
	}
	@Override
	public SmbData parseResponse(SmbHeader h,Buffer b ,SmbSession session) {
		TreeConnectResponse data = new TreeConnectResponse();
		data.setWordCount(b.get());
		data.setMaxBufferSize(ByteOrderConverter.swap(b.getShort()));
		data.setTid(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return null;
	}

}
