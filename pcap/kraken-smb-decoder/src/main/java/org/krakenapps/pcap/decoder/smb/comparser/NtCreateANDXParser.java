package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NtCreateANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.NtCreateANDXExtentionResponse;
import org.krakenapps.pcap.decoder.smb.response.NtCreateANDXResponse;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.decoder.smb.rr.NamedPipeStatus;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;


//0xA2
public class NtCreateANDXParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NtCreateANDXRequest data = new NtCreateANDXRequest();
		data.setWordCount(b.get());
		data.setAndxCommand(b.get());
		data.setAndxReserved(b.get());
		data.setAndxOffset(ByteOrderConverter.swap(b.getShort()));
		data.setReserved(b.get());
		data.setNameLength(ByteOrderConverter.swap(b.getShort()));
		data.setFlags(ByteOrderConverter.swap(b.getInt()));
		data.setRootDirectoryFID(ByteOrderConverter.swap(b.getInt()));
		data.setDesiredAccess(ByteOrderConverter.swap(b.getInt()));
		data.setAllocationSize(ByteOrderConverter.swap(b.getLong()));
		data.setExtFileAttributes(ExtFileAttributes.parse(ByteOrderConverter.swap(b.getInt())));
		data.setShareAccess(ByteOrderConverter.swap(b.getInt()));
		data.setCreateDisposition(ByteOrderConverter.swap(b.getInt()));
		data.setCreateOptions(ByteOrderConverter.swap(b.getInt()));
		data.setImpersonationLevel(ByteOrderConverter.swap(b.getInt()));
		data.setSecurityFlags(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		//TODO : padding 
		//b.get();
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
		SmbData data;
		if(((NtCreateANDXRequest)(session).getUseSessionData()).isNtCreateRequestExtendedResponse())
		{
			byte []volumeGUID = new byte[16];
			data = new NtCreateANDXExtentionResponse();
			((NtCreateANDXExtentionResponse)data).setWordCount(b.get());
			if(((NtCreateANDXExtentionResponse)data).getWordCount() !=0){
				((NtCreateANDXExtentionResponse)data).setAndxCommand(b.get());
				((NtCreateANDXExtentionResponse)data).setAndxReserved(b.get());
				((NtCreateANDXExtentionResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((NtCreateANDXExtentionResponse)data).setOpLockLevel(b.get());
				((NtCreateANDXExtentionResponse)data).setFid(ByteOrderConverter.swap(b.getShort()));
				((NtCreateANDXExtentionResponse)data).setCreationAction(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXExtentionResponse)data).setCreateTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXExtentionResponse)data).setLastAccessTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXExtentionResponse)data).setLastChangeTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXExtentionResponse)data).setExtFileAttributes(ExtFileAttributes.parse(ByteOrderConverter.swap(b.getInt())));
				((NtCreateANDXExtentionResponse)data).setAllocationSize(ByteOrderConverter.swap(b.getLong()));
				((NtCreateANDXExtentionResponse)data).setEndOfFile(ByteOrderConverter.swap(b.getLong()));
				((NtCreateANDXExtentionResponse)data).setResourceType(ByteOrderConverter.swap(b.getShort()));
				((NtCreateANDXExtentionResponse)data).setNmPipeStatus_or_FileStatusFlag(ByteOrderConverter.swap(b.getShort()));
				((NtCreateANDXExtentionResponse)data).setDirectory(b.get());
				b.gets(volumeGUID);
				((NtCreateANDXExtentionResponse)data).setVolumeGUID(volumeGUID);
				((NtCreateANDXExtentionResponse)data).setFileID(ByteOrderConverter.swap(b.getLong()));
				((NtCreateANDXExtentionResponse)data).setMaximalAccessRight(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXExtentionResponse)data).setGuestMaximalAccessRight(ByteOrderConverter.swap(b.getInt()));
			}
			((NtCreateANDXExtentionResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
		}
		else
		{
			data = new NtCreateANDXResponse();
			((NtCreateANDXResponse)data).setWordCount(b.get());
			if(((NtCreateANDXResponse)data).getWordCount() !=0){
				((NtCreateANDXResponse)data).setAndxCommand(b.get());
				((NtCreateANDXResponse)data).setAndxReserved(b.get());
				((NtCreateANDXResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((NtCreateANDXResponse)data).setOpLockLevel(b.get());
				((NtCreateANDXResponse)data).setFid(ByteOrderConverter.swap(b.getShort()));
				((NtCreateANDXResponse)data).setCreateDisposition(b.getInt());
				((NtCreateANDXResponse)data).setCreateTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXResponse)data).setLastAccessTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXResponse)data).setLastWriteTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXResponse)data).setLastChangeTime(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXResponse)data).setExtFileAttributes(ExtFileAttributes.parse(ByteOrderConverter.swap(b.getInt())));
				((NtCreateANDXResponse)data).setAllocationSize(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXResponse)data).setEndOfFile(ByteOrderConverter.swap(b.getInt()));
				((NtCreateANDXResponse)data).setResourceType(b.getShort());
				((NtCreateANDXResponse)data).setNmPipestatus(NamedPipeStatus.parse(ByteOrderConverter.swap(b.getShort())));
				((NtCreateANDXResponse)data).setDirectory(b.get());
			}
			((NtCreateANDXResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
		}
		return data;
	}
}
