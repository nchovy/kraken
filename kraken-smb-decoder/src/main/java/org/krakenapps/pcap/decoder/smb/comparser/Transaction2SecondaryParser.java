package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.Transaction2SecondaryRequest;
import org.krakenapps.pcap.decoder.smb.response.Transaction2SecondaryResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x33
public class Transaction2SecondaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		Transaction2SecondaryRequest data = new Transaction2SecondaryRequest();
		byte []pad1;
		byte []pad2;
		byte []parameter;
		byte []buff;
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x09){
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
			data.setParameterDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
			data.setDataDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setFid(ByteOrderConverter.swap(b.getShort()));
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
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
		pad1 = new byte[data.getParameterOffset() - 32 - 17 -2];
		if(pad1.length != 0){
			b.gets(pad1);
			data.setPad1(pad1);
		}
		parameter = new byte[data.getParameterCount()];
		if(parameter.length !=0){
			b.gets(parameter);
			data.setTrans2Parameters(parameter);
		}
		pad2 = new byte[4-(parameter.length%4)];
		if(pad2.length != 0){
			b.gets(pad2);
			data.setPad2(pad2);
		}
		buff = new byte[data.getDataCount()];
		b.gets(buff);
		data.setTrans2Data(buff);
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		Transaction2SecondaryResponse data = new Transaction2SecondaryResponse();
		// there is no response
		return data;
	}

	
	// there is no response , no error code;
}
