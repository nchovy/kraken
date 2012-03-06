package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.LogoffANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.LogoffANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class LogoffANDXParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		LogoffANDXRequest data = new LogoffANDXRequest();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x02){
			data.setAndxCommand(b.get());
			data.setAndxResrved(b.get());
			data.setAndxOffset(ByteOrderConverter.swap(b.getShort()));
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		LogoffANDXResponse data = new LogoffANDXResponse();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x02){
			data.setAndXCommand(b.get());
			data.setAndXReserved(b.get());
			data.setAndXOffset(ByteOrderConverter.swap(b.getShort()));
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
