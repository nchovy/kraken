package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteAndCloseRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteAndCloseResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x2C
public class WriteAndCloseParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		WriteAndCloseRequest data = new WriteAndCloseRequest();
		byte []reserved = new byte[12];
		byte []buff;
		data.setWordCount(b.get());
		if( (data.getWordCount() == (0x06)) || (data.getWordCount() == 0x0c ))
		{
			data.setFid(ByteOrderConverter.swap(b.getShort()));
			data.setCountOfBytesToWrite(ByteOrderConverter.swap(b.getShort()));
			data.setWriteOffsetInBytes(ByteOrderConverter.swap(b.getInt()));
			if(data.getWordCount() == 0x0C){
				b.gets(reserved);
				data.setReserved(reserved); // optional
			}
		}
		else{
			b.skip(data.getWordCount()*2);
			data.setMalformed(true);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		else if(b.readableBytes() ==0){
			data.setMalformed(true);
			return data;
		}
		data.setPad(b.get());
		buff = new byte[data.getCountOfBytesToWrite()];
		b.gets(buff);
		data.setData(buff);
		
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		WriteAndCloseResponse data = new WriteAndCloseResponse();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x01){
			data.setCountOfBytesWritten(ByteOrderConverter.swap(b.getShort()));
		}
		else{
			b.skip(data.getWordCount()*2);
			data.setMalformed(true);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
