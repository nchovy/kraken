package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.IOCTLRequest;
import org.krakenapps.pcap.decoder.smb.response.IOCTLResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x27
public class IOCTLParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		IOCTLRequest data = new IOCTLRequest();
		byte []pad1;
		byte []parameter;
		byte []pad2;
		byte []buff;
		int offset;
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x0e){
			data.setFid(ByteOrderConverter.swap(b.getShort()));
			data.setCategory(ByteOrderConverter.swap(b.getShort()));
			data.setFunction(ByteOrderConverter.swap(b.getShort()));
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setMaxDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setMaxParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTimeout(b.getInt());
			data.setReserved(ByteOrderConverter.swap(b.getShort()));
			data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
			data.setDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
		}
		else
		{
			data.setMalformed(true);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		offset = data.getParameterOffset();
		if(offset <0 || offset >0){
			if(offset<0)
			{
				offset = (~offset +1)& 0x0000ffff;
			}
		}
		pad1 = new byte[offset - (data.getWordCount()*2)-2];
		if(pad1.length!=0){
			b.gets(pad1);
			data.setPad1(pad1);
		}
		parameter = new byte[data.getParameterCount()];
		if(parameter.length != 0){
			b.gets(parameter);
			data.setParameters(parameter);
		}
		pad2 = new byte[4-(data.getParameterCount()%4)];
		if(pad2.length !=0){
			b.gets(pad2);
			data.setPad2(pad2);
		}
		else if(pad2.length == 4){
			//do nothing
		}
		buff = new byte[data.getDataCount()];
		b.gets(buff);
		data.setData(buff);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		IOCTLResponse data = new IOCTLResponse();
		byte []pad1;
		byte []parameter;
		byte []pad2;
		byte []buff;
		int offset;
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x08){
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
			data.setParameterDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
			data.setDataDisplacement(ByteOrderConverter.swap(b.getShort()));
		}
		else{
			data.setMalformed(true);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		else if(b.readableBytes() ==0){
			return data;
		}
		offset = data.getParameterOffset();
		if(offset <0 || offset >0){
			if(offset<0)
			{
				offset = (~offset +1)& 0x0000ffff;
			}
		}
		pad1 = new byte[offset - data.getWordCount()-2];
		if(pad1.length!=0){
			b.gets(pad1);
			data.setPad1(pad1);
		}
		parameter = new byte[data.getParameterCount()];
		if(parameter.length != 0){
			b.gets(parameter);
			data.setParameters(parameter);
		}
		pad2 = new byte[4-(data.getParameterCount()%4)];
		if(pad2.length !=0){
			b.gets(pad2);
			data.setPad2(pad2);
		}
		else if(pad2.length == 4){
			//do nothing
		}
		buff = new byte[data.getDataCount()];
		b.gets(buff);
		data.setData(buff);
		return data;
	}
}