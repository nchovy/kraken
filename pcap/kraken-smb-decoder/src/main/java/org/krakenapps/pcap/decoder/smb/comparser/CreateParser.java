package org.krakenapps.pcap.decoder.smb.comparser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.CreateRequest;
import org.krakenapps.pcap.decoder.smb.response.CreateResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class CreateParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h,Buffer b , SmbSession session) {
		CreateRequest data = new CreateRequest();
		data.setWordCount(b.get());
		data.setFileattr(FileAttributes.parse(ByteOrderConverter.swap(ByteOrderConverter.swap(b.getShort())) & 0xffff));
		data.setCreateTime(ByteOrderConverter.swap(b.getInt()));
		data.setByteCount(ByteOrderConverter.swap(ByteOrderConverter.swap(b.getShort())));
		data.setBufferFormat(b.get());
		if(h.isFlag2Unicode()){
			data.setFileName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setFileName(NetBiosNameCodec.readOemName(b));
		}
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		CreateResponse data = new CreateResponse();
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(ByteOrderConverter.swap(ByteOrderConverter.swap(b.getShort()))));
		data.setByteCount(ByteOrderConverter.swap(ByteOrderConverter.swap(ByteOrderConverter.swap(b.getShort()))));
		return data;
	}
	

}
