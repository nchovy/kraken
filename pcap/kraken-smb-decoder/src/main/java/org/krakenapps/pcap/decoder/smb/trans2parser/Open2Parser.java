package org.krakenapps.pcap.decoder.smb.trans2parser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.trans2req.Open2Resquest;
import org.krakenapps.pcap.decoder.smb.trans2resp.Open2Response;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class Open2Parser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		Open2Resquest transData = new Open2Resquest();
		byte []reserved = new byte[10];
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setAccessMode(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setFlags(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setSearchAttribute(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setFileAttribute(FileAttributes.parse(ByteOrderConverter.swap(parameterBuffer.getShort())&0xffff));
		transData.setCreationTime(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setOpenMode(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setAllocationSize(ByteOrderConverter.swap(parameterBuffer.getInt()));
		parameterBuffer.gets(reserved);
		transData.setReserved(reserved);
		transData.setFileName(NetBiosNameCodec.readSmbUnicodeName(parameterBuffer));
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		Open2Response transData = new Open2Response();
		transData.setFid(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setFileAttribute(FileAttributes.parse(ByteOrderConverter.swap(parameterBuffer.getShort())& 0xffff));
		transData.setCreationTime(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setFiledataSize(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setAccessMode(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setResourceType(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setNmPipeStatus(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setActionToken(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setReserved(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setExtendedAttributeErrorOffset(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setExtendedAttributeLength(ByteOrderConverter.swap(parameterBuffer.getInt()));
		// TODO : transData 
		return transData;
	}
}
