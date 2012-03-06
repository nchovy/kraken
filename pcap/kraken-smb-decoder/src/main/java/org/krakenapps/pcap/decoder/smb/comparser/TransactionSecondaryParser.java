package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.TransactionSecondaryRequest;
import org.krakenapps.pcap.decoder.smb.response.TransactionSecondaryResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x26
public class TransactionSecondaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		TransactionSecondaryRequest data = new TransactionSecondaryRequest();
		byte []pad1;
		byte []pad2;
		byte []parameter;
		byte []buff;
		data.setWordCount(b.get());
		data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
		data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
		data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
		data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
		data.setParameterDisplacement(ByteOrderConverter.swap(b.getShort()));
		data.setDataCount(ByteOrderConverter.swap(b.getShort()));
		data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
		data.setDataDisplacement(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
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
			data.setTransParameters(parameter);
		}
		pad2 = new byte[4-(parameter.length%4)];
		if(pad2.length != 0){
			b.gets(pad2);
			data.setPad2(pad2);
		}
		buff = new byte[data.getDataCount()];
		b.gets(buff);
		data.setTransData(buff);
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		TransactionSecondaryResponse data = new TransactionSecondaryResponse();
		//there is no response
		return data;
	}

	//there is no response 
}
