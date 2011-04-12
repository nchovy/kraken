package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.OpenANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.OpenANDXExtendedResponse;
import org.krakenapps.pcap.decoder.smb.response.OpenANDXResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.rr.NamedPipeStatus;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class OpenANDXParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		OpenANDXRequest data = new OpenANDXRequest();
		byte []reserved = new byte[4];
		data.setWordCount(b.get());
		data.setAndxCommand(b.get());
		data.setAndxReserved(b.get());
		data.setAndxOffset(ByteOrderConverter.swap(b.getShort()));
		data.setFlags(ByteOrderConverter.swap(b.getShort()));
		data.setAccessMode(ByteOrderConverter.swap(b.getShort()));
		data.setSearchAttrs(FileAttributes.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
		data.setFileAttrs(FileAttributes.parse(ByteOrderConverter.swap(b.getShort()) & 0xffff));
		data.setCreationTime(b.getInt());
		data.setOpenMode(ByteOrderConverter.swap(b.getShort()));
		data.setAllocationSize(b.getInt());
		data.setTimeout(b.getInt());
		b.gets(reserved);
		data.setReserved(reserved);
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
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
		//if() SessionMapper.extension == 0 -> not extension
		OpenANDXRequest reqData  = (OpenANDXRequest)session.getUseSessionData();
		OpenANDXResponse NotExtData;
		OpenANDXExtendedResponse ExtData;
		byte []reserved;
		if(reqData.isFlagOpenExtendedResponse())
		{
			reserved = new byte[2];
			//must 39byte
			ExtData = new OpenANDXExtendedResponse();
			ExtData.setWordCount(b.get());
			if(ExtData.getWordCount() != 0){
				ExtData.setAndxCommand(b.get());
				ExtData.setAndxReserved(b.get());
				ExtData.setAndxOffset(ByteOrderConverter.swap(b.getShort())); // use combine
				ExtData.setFid(ByteOrderConverter.swap(b.getShort()));
				ExtData.setFileAttrs(FileAttributes.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
				ExtData.setLastWriteTime(b.getInt());
				ExtData.setFileDataSize(b.getInt());
				ExtData.setAccessRights(ByteOrderConverter.swap(b.getShort()));
				ExtData.setResourceType(ByteOrderConverter.swap(b.getShort()));
				ExtData.setNmPipeStatus(NamedPipeStatus.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
				ExtData.setOpenResults(ByteOrderConverter.swap(b.getShort()));
				ExtData.setServerFid(b.getInt());
				b.gets(reserved);
				ExtData.setReserved(reserved);
				ExtData.setMaximalAccessRight(ByteOrderConverter.swap(b.getInt()));
				ExtData.setGuestMaximalAccessRight(ByteOrderConverter.swap(b.getInt()));
			}
			ExtData.setByteCount(ByteOrderConverter.swap(b.getShort()));
			return ExtData;
		}
		else
		{
			reserved = new byte[6];
			NotExtData = new OpenANDXResponse();
			NotExtData.setWordCount(b.get());
			if(NotExtData.getWordCount() !=0){
				NotExtData.setAndxCommand(b.get());
				NotExtData.setAndxReserved(b.get());
				NotExtData.setAndxOffset(ByteOrderConverter.swap(b.getShort())); // use combine
				NotExtData.setFid(ByteOrderConverter.swap(b.getShort()));
				NotExtData.setFileAttrs(FileAttributes.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
				NotExtData.setLastWriteTime(b.getInt());
				NotExtData.setFileDataSize(b.getInt());
				NotExtData.setAccessRights(ByteOrderConverter.swap(b.getShort()));
				NotExtData.setResourceType(ByteOrderConverter.swap(b.getShort()));
				NotExtData.setNmPipeStatus(NamedPipeStatus.parse(ByteOrderConverter.swap(b.getShort())&0xffff));
				NotExtData.setOpenResults(ByteOrderConverter.swap(b.getShort()));
				b.gets(reserved);
				NotExtData.setReserved(reserved);
			}
			NotExtData.setByteCount(ByteOrderConverter.swap(b.getShort()));
			return NotExtData;
		}
	}
}
