package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteRawRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteRawResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class WriteRawParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		WriteRawRequest data = new WriteRawRequest();
		byte []pad;
		byte []bytes;
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setCountOfBytes(ByteOrderConverter.swap(b.getShort()));
		data.setReserved1(ByteOrderConverter.swap(b.getShort()));
		data.setOffset(ByteOrderConverter.swap(b.getInt()));
		data.setTimeout(ByteOrderConverter.swap(b.getInt()));
		data.setWriteMode(ByteOrderConverter.swap(b.getShort()));
		data.setReserved2(ByteOrderConverter.swap(b.getShort()));
		data.setDataLength(ByteOrderConverter.swap(b.getInt()));
		data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
		if(data.getWordCount() == 0x0E)
		{
			data.setOffsetHigh(ByteOrderConverter.swap(b.getInt()));
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		pad = new byte [data.getDataOffset()-data.getWordCount()-2-32];
		bytes = new byte [data.getDataLength()];
		if(pad.length !=0){
		b.gets(pad);
		data.setPad(pad);
		}
		b.gets(bytes);
		data.setData(bytes);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		WriteRawResponse data = new WriteRawResponse();
		data.setWordCount(b.get());
		data.setAvailable(ByteOrderConverter.swap(b.getShort())); // or Count
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
