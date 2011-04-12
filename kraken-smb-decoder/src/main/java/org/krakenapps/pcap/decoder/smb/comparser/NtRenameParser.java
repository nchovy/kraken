package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NtRenameRequest;
import org.krakenapps.pcap.decoder.smb.response.NtRenameResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0xA5
public class NtRenameParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NtRenameRequest data = new NtRenameRequest();
		data.setWordCount(b.get());
		data.setSearchAttributes(FileAttributes.parse(ByteOrderConverter.swap(b.getShort())));
		data.setInformationLevel(b.getShort());
		data.setReserved(ByteOrderConverter.swap(b.getInt()));
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
			data.setNewFileName(NetBiosNameCodec.readOemName(b));
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		NtRenameResponse data = new NtRenameResponse();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	
}
