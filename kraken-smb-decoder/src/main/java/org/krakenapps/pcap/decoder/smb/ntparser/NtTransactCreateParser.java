package org.krakenapps.pcap.decoder.smb.ntparser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.ntreq.NtTransactCreateRequest;
import org.krakenapps.pcap.decoder.smb.ntresp.NtTransactCreateResponse;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.decoder.smb.rr.NamedPipeStatus;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class NtTransactCreateParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer) {
		NtTransactCreateRequest transData = new NtTransactCreateRequest();
		//setup
		//this packet Type that setupBuffer is null
		// start to parameter parse
		transData.setFlags(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setRootDirectoryFid(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setDesiredAccess(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setAllocationSize(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setExtFileAttributes(ExtFileAttributes.parse(ByteOrderConverter.swap(parameterBuffer.getInt())));
		transData.setShareAccess(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setCreateDisposition(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setCreateOptions(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setSecurityDescriptorLength(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setEaLength(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setNameLength(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setImpersonationLevel(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setSecurityFlags(parameterBuffer.get());
		byte []name = new byte[transData.getNameLength()];		// if UNICODE align 2byte
		parameterBuffer.gets(name);
		transData.setName(NetBiosNameCodec.SmbNameConvertToString(name));
		//end of parameter parse
		//start to dataBuffer parse
		byte []securityDescriptor = new byte[transData.getSecurityDescriptorLength()];
		parameterBuffer.gets(securityDescriptor);
		transData.setSecurityDescriptor(securityDescriptor);
		byte []extendedAttribytes = new byte[parameterBuffer.readableBytes()];
		parameterBuffer.gets(extendedAttribytes);
		transData.setExtendedAttribytes(extendedAttribytes);
		//
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer , SmbSession session) {
		NtTransactCreateResponse transData = new NtTransactCreateResponse();
		//there is no use setupBuffer because setupCount must be 0x00
		transData.setOpLockLevel(parameterBuffer.get());
		transData.setReserved(parameterBuffer.get());
		transData.setFid(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setCreateAction(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setEaErrorOffset(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setCreationTime(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setLastAccessTime(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setLastWriteTime(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setLastChangeTime(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setExtFileAttributes(ExtFileAttributes.parse(ByteOrderConverter.swap(parameterBuffer.getInt())));
		transData.setAllocationSize(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setEndOfFile(ByteOrderConverter.swap(parameterBuffer.getLong()));
		transData.setResourceType(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setNmPipeStatus(NamedPipeStatus.parse(ByteOrderConverter.swap(parameterBuffer.getShort())));
		transData.setDirectory(parameterBuffer.get());
		// there is no use dataBuffer
		return transData;
	}

}
