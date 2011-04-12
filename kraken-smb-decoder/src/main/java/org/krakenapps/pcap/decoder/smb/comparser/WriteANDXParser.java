package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteANDXExtensionResponse;
import org.krakenapps.pcap.decoder.smb.response.WriteANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
// 0x2F
public class WriteANDXParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		WriteANDXRequest data = new WriteANDXRequest();
		byte []buff;
		data.setWordCount(b.get());
		data.setAndxCommand(b.get());
		data.setAndxReserved(b.get());
		data.setAndxOffset(ByteOrderConverter.swap(b.getShort()));
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setOffset(ByteOrderConverter.swap(b.getInt()));
		data.setTimeout(ByteOrderConverter.swap(b.getInt()));
		data.setWriteMode(ByteOrderConverter.swap(b.getShort()));
		data.setRemaining(ByteOrderConverter.swap(b.getShort()));
		data.setReserved(ByteOrderConverter.swap(b.getShort()));
		data.setDataLength(ByteOrderConverter.swap(b.getShort()));
		data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
		if(data.getWordCount() == 0x0C){
			data.setOffsetHigh(ByteOrderConverter.swap(b.getInt()));
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		data.setPad(b.get());
		buff = new byte[data.getDataLength()];
		b.gets(buff);
		data.setData(buff);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		
		SmbData data;
		if( /*((NegotiateResponse)session.getNegotiateResponseData()).isCapLargeReadx()*/session.isCapLargeReadx() )
		{
			data = new WriteANDXExtensionResponse();
			((WriteANDXExtensionResponse)data).setWordCount(b.get());
			if(((WriteANDXExtensionResponse)data).getWordCount() != 0){
				((WriteANDXExtensionResponse)data).setAndxCommand(b.get());
				((WriteANDXExtensionResponse)data).setAndxReserved(b.get());
				((WriteANDXExtensionResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((WriteANDXExtensionResponse)data).setCount(ByteOrderConverter.swap(b.getShort()));
				((WriteANDXExtensionResponse)data).setAvailable(ByteOrderConverter.swap(b.getShort()));
				((WriteANDXExtensionResponse)data).setCountHigh(ByteOrderConverter.swap(b.getShort()));
				((WriteANDXExtensionResponse)data).setReserved(ByteOrderConverter.swap(b.getShort()));
			}
			((WriteANDXExtensionResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
		}
		else
		{
			data = new WriteANDXResponse();
			((WriteANDXResponse)data).setWordCount(b.get());
			((WriteANDXResponse)data).setAndxCommand(b.get());
			((WriteANDXResponse)data).setAndxReserved(b.get());
			((WriteANDXResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
			((WriteANDXResponse)data).setCount(ByteOrderConverter.swap(b.getShort()));
			((WriteANDXResponse)data).setAvailable(ByteOrderConverter.swap(b.getShort()));
			((WriteANDXResponse)data).setReserved(ByteOrderConverter.swap(b.getInt()));
			((WriteANDXResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
		}
		return data;
	}
}
