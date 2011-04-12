package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteMPXRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteMPXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class WriteMPXParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		WriteMPXRequest data = new WriteMPXRequest();
		byte []pad;
		byte []buffer;
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setTotalByteCount(ByteOrderConverter.swap(b.getShort()));
		data.setReserved(ByteOrderConverter.swap(b.getShort()));
		data.setByteOffsetToBeginwrite(ByteOrderConverter.swap(b.getInt()));
		data.setTimeout(ByteOrderConverter.swap(b.getInt()));
		data.setWriteMode(ByteOrderConverter.swap(b.getShort()));
		data.setReqMask(ByteOrderConverter.swap(b.getInt()));
		data.setDataLength(ByteOrderConverter.swap(b.getShort()));
		data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		pad = new byte[data.getDataOffset()-25-32];
		buffer = new byte[data.getDataLength()];
		if(pad.length !=0){
			b.gets(pad);
			data.setPad(pad);
		}
		b.gets(buffer);
		data.setBuffer(buffer);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		WriteMPXResponse data = new WriteMPXResponse();
		data.setWordCount(b.get());
		data.setResMask(ByteOrderConverter.swap(b.getInt()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
