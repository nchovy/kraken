package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.CreateTemporaryRequest;
import org.krakenapps.pcap.decoder.smb.response.CreateTemporaryResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class CreateTemporaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h,Buffer b , SmbSession session) {
		CreateTemporaryRequest data = new CreateTemporaryRequest();
		data.setWordCount(b.get());
		data.setFileAttributes(FileAttributes.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
		data.setCreationTime(ByteOrderConverter.swap(b.getInt()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		data.setBufferFormat(b.get());
		if(h.isFlag2Unicode()){
			data.setDirectoryName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setDirectoryName(NetBiosNameCodec.readOemName(b));
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
			CreateTemporaryResponse data = new CreateTemporaryResponse();
			data.setWordCount(b.get());
			data.setFid(ByteOrderConverter.swap(b.getShort()));
			data.setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() == data.getByteCount()){
				data.setMalformed(true);
				return data;
			}
			data.setBufferFormat(b.get());
			if(h.isFlag2Unicode()){
				data.setTemporaryFileName(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else{
				data.setTemporaryFileName(NetBiosNameCodec.readOemName(b));
			}
		return data;
	}
}
