package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.OpenPrintFileRequest;
import org.krakenapps.pcap.decoder.smb.response.OpenPrintFileResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0xC0
public class OpenPrintFileParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		OpenPrintFileRequest data = new OpenPrintFileRequest();
		data.setWordCount(b.get());
		data.setSetupLength(ByteOrderConverter.swap(b.getShort()));
		data.setMode(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		data.setBufferFormat(b.get());
		if(h.isFlag2Unicode()){
			data.setIdentifier(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setIdentifier(NetBiosNameCodec.readOemName(b));
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		
		OpenPrintFileResponse data = new OpenPrintFileResponse();
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
