package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.EchoRequest;
import org.krakenapps.pcap.decoder.smb.response.EchoResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x2B
public class EchoParser implements SmbDataParser{


	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		EchoRequest data = new EchoRequest();
		byte []buff;
		data.setWordCount(b.get());
		data.setEchoCount(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		buff = new byte[data.getByteCount()];
		b.gets(buff);
		data.setData(buff);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		EchoResponse data = new EchoResponse();
		byte []buff;
		data.setWordCount(b.get());
		data.setSequenceNumber(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		buff = new byte[data.getByteCount()];
		b.gets(buff);
		data.setData(buff);
		return data;
	}
}
