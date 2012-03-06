package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NtTransactSecondaryRequest;
import org.krakenapps.pcap.decoder.smb.response.NtTransactSecondaryResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class NtTransactSecondaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NtTransactSecondaryRequest data = new NtTransactSecondaryRequest();
		byte []pad1;
//		byte []pad2;
		byte []parameter;
		byte []ntData;
		byte []reserved1 = new byte[3];
		data.setWordCount(b.get());
		b.gets(reserved1);
		data.setReserved1(reserved1);
		data.setTotalParameterCount(ByteOrderConverter.swap(b.getInt()));
		data.setTotalDataCount((ByteOrderConverter.swap(b.getInt())));
		data.setParameterCount((ByteOrderConverter.swap(b.getInt())));
		data.setParameterOffset((ByteOrderConverter.swap(b.getInt())));
		data.setParameterDisplacement((ByteOrderConverter.swap(b.getInt())));
		data.setDataCount((ByteOrderConverter.swap(b.getInt())));
		data.setDataOffset((ByteOrderConverter.swap(b.getInt())));
		data.setDataDisplacement((ByteOrderConverter.swap(b.getInt())));
		data.setReserved2(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		pad1 = new byte[data.getParameterOffset() - 32 - 2 - 1 -(data.getWordCount()*2)];
		b.gets(pad1);
		data.setPad1(pad1);
		parameter = new byte[data.getParameterCount()];
		b.gets(parameter);
		data.setParameters(parameter);
		ntData = new byte[data.getDataCount()];
		b.gets(ntData);
		data.setData(ntData);
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		NtTransactSecondaryResponse data = new NtTransactSecondaryResponse();
		return data;
		//there is no response
	}

}
