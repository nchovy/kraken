package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.FindRequest;
import org.krakenapps.pcap.decoder.smb.response.FindResponse;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbDirectoryInfo;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.decoder.smb.structure.SmbResumeKey;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x82
public class FindParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		FindRequest data = new FindRequest();
		SmbResumeKey []key;
		byte []serverState = new byte[16];
		byte []clientState = new byte[4];
		data.setWordCount(b.get());
		data.setMaxCount(ByteOrderConverter.swap(b.getShort()));
		data.setSearchAttributes(FileAttributes.parse(ByteOrderConverter.swap(b.getShort()) & 0xffff));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		data.setBufferFormat1(b.get());
		if(h.isFlag2Unicode()){
			data.setFileName(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setFileName(NetBiosNameCodec.readOemName(b));
		}
		data.setBufferFormat2(b.get());
		data.setResumeKeyLength(ByteOrderConverter.swap(b.getShort()));
		key = new SmbResumeKey[(data.getResumeKeyLength())/21];
		for(int i =0; i< (data.getResumeKeyLength())/21;i++){
			key[i] = new SmbResumeKey();
			key[i].setReserved(b.get());
			b.gets(serverState);
			key[i].setServerState(serverState);
			b.gets(clientState);
			key[i].setClientState(clientState);
		}
		data.setResumeKey(key);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		//this section don't match CIFS document
		// word count is must be 0x01
		// but real packet exist 0x00
		FindResponse data = new FindResponse();
		SmbResumeKey []key;
		SmbDirectoryInfo []info;
		byte []serverState = new byte[16];
		byte []clientState = new byte[4];
		data.setWordCount(b.get());
		// this section is temporary routine because wordcount(0x00)
		if(data.getWordCount() != 0){
			data.setCount(ByteOrderConverter.swap(b.getShort()));
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(data.getByteCount() == 0 ){
			return data;
		}
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		data.setBufferFormat(b.get());
		data.setDataLength(ByteOrderConverter.swap(b.getShort()));
		key = new SmbResumeKey[(data.getDataLength())/43];
		info = new SmbDirectoryInfo[(data.getDataLength())/43];
		for(int i=0; i<(data.getDataLength())/43;i++){
			key[i] = new SmbResumeKey();
			info[i] = new SmbDirectoryInfo();
			//resume key
			key[i].setReserved(b.get());
			b.gets(serverState);
			key[i].setServerState(serverState);
			b.gets(clientState);
			key[i].setClientState(clientState);
			//set directory info
			info[i].setResumeKey(key[i]);
			info[i].setFileAttributes(FileAttributes.parse(b.get() & 0xff));
			info[i].setLastWriteTime(ByteOrderConverter.swap(b.getShort()));
			info[i].setLastWriteDate(ByteOrderConverter.swap(b.getShort()));
			info[i].setFileSize(ByteOrderConverter.swap(b.getInt()));
			if(h.isFlag2Unicode()){
				info[i].setFilename(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else{
				info[i].setFilename(NetBiosNameCodec.readOemName(b));
			}
		}
		data.setDirectoryInformationData(info);
		return data;
	}

}
