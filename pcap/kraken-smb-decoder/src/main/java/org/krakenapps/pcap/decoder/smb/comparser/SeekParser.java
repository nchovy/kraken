package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.SeekRequest;
import org.krakenapps.pcap.decoder.smb.response.SeekResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SeekParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		SeekRequest data = new SeekRequest();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x04){
			data.setFid(ByteOrderConverter.swap(b.getShort()));
			data.setMode(ByteOrderConverter.swap(b.getShort()));
			data.setOffset(ByteOrderConverter.swap(b.getInt()));
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
		SeekResponse data = new SeekResponse();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x02){
			data.setOffset(ByteOrderConverter.swap(b.getInt()));
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
