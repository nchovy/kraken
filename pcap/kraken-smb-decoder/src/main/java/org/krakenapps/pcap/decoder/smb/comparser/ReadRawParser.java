package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.ReadRawRequest;
import org.krakenapps.pcap.decoder.smb.response.ReadRawResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class ReadRawParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		ReadRawRequest data = new ReadRawRequest();
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setOffset(ByteOrderConverter.swap(b.getInt()));
		data.setMaxCountOfBytesToReturn(ByteOrderConverter.swap(b.getShort()));
		data.setMinCountOfBytesToReturn(ByteOrderConverter.swap(b.getShort()));
		data.setTimeout(ByteOrderConverter.swap(b.getInt()));
		data.setReserved(ByteOrderConverter.swap(b.getShort()));
		if(data.getWordCount() == 0x0A)
		{
			data.setOffsetHigh(ByteOrderConverter.swap(b.getInt()));
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SmbData data = new ReadRawResponse();
		//this packet has no response
		return data;
	}
}
