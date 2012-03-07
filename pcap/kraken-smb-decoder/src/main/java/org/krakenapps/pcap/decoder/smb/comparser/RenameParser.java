package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.RenameRequest;
import org.krakenapps.pcap.decoder.smb.response.RenameResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RenameParser implements SmbDataParser{
	
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		RenameRequest data = new RenameRequest();
		data.setWordCount(b.get());
		data.setSearchAttributes(FileAttributes.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		data.setBufferFormat1(b.get());
		if(h.isFlag2Unicode()){
			data.setOldFileName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setOldFileName(NetBiosNameCodec.readOemName(b));
		}
		data.setBufferFormat2(b.get());
		if(h.isFlag2Unicode()){
			data.setNewFileName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setNewFileName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		RenameResponse data = new RenameResponse();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	
}
