package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.CreateNewRequest;
import org.krakenapps.pcap.decoder.smb.response.CreateNewResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class CreateNewParser implements SmbDataParser{
	byte resWordCount;
	@Override
	public SmbData parseRequest(SmbHeader h,Buffer b , SmbSession session) {
		CreateNewRequest data = new  CreateNewRequest();
		data.setWordCount(b.get());
		data.setFileAttributes(FileAttributes.parse(ByteOrderConverter.swap(b.getShort()) &0xffff));
		data.setCreateionTime(ByteOrderConverter.swap(ByteOrderConverter.swap(b.getInt())));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
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
	public SmbData parseResponse(SmbHeader h,Buffer b ,SmbSession session) {
		CreateNewResponse data = new  CreateNewResponse();
		data.setWordCount(b.get());
		if(data.getWordCount() !=0){
			data.setFid(ByteOrderConverter.swap(b.getShort()));
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
