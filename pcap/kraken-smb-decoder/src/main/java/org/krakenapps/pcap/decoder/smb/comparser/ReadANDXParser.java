package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.ReadANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.ReadANDXExtensionResponse;
import org.krakenapps.pcap.decoder.smb.response.ReadANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class ReadANDXParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		ReadANDXRequest data = new ReadANDXRequest();
		data.setWordCount(b.get());
		data.setAndXCommand(b.get());
		data.setAndXReserved(b.get());
		data.setAndXOffset(ByteOrderConverter.swap(b.getShort()));
		data.setFID(ByteOrderConverter.swap(b.getShort()));
		data.setOffset(ByteOrderConverter.swap(b.getInt()));
		data.setMaxCountOfBytesToReturn(ByteOrderConverter.swap(b.getShort()));
		data.setMinCountOfBytesToReturn(ByteOrderConverter.swap(b.getShort()));
		data.setTimeout(ByteOrderConverter.swap(b.getInt()));
		data.setRemaining(ByteOrderConverter.swap(b.getShort()));
		
		if(data.getWordCount() == 0x0C){
			data.setOffsetHigh(ByteOrderConverter.swap(b.getInt()));
		}
		
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SmbData data;
		byte []reserved2;
		byte []buff;
		if(session.isCapLargeReadx())
		{
			reserved2 = new byte[8];
			byte []pad;
			data = new ReadANDXExtensionResponse();
			((ReadANDXExtensionResponse)data).setWordCount(b.get());
			if(((ReadANDXExtensionResponse)data).getWordCount() != 0){
				((ReadANDXExtensionResponse)data).setAndxCommand(b.get());
				((ReadANDXExtensionResponse)data).setAndxReserved(b.get());
				((ReadANDXExtensionResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((ReadANDXExtensionResponse)data).setAvailable(ByteOrderConverter.swap(b.getShort()));
				((ReadANDXExtensionResponse)data).setDataCompactionMode(ByteOrderConverter.swap(b.getShort()));
				((ReadANDXExtensionResponse)data).setReserved1(ByteOrderConverter.swap(b.getShort()));
				((ReadANDXExtensionResponse)data).setDataLength(ByteOrderConverter.swap(b.getShort()));
				((ReadANDXExtensionResponse)data).setDataOffset(ByteOrderConverter.swap(b.getShort()));
				((ReadANDXExtensionResponse)data).setDataLengthHigh(ByteOrderConverter.swap(b.getShort()));
				b.gets(reserved2);
				((ReadANDXExtensionResponse)data).setReserved2(reserved2);
			}
			((ReadANDXExtensionResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(((ReadANDXExtensionResponse)data).getByteCount()!=0){
	//			System.out.println("ByteCount = " + ((ReadANDXExtensionResponse)data).getByteCount());
	//			System.out.println("Datalength = " + ((ReadANDXExtensionResponse)data).getDataLength());
	//			System.out.println("DatalengthHigh = " + ((ReadANDXExtensionResponse)data).getDataLengthHigh());
				pad = new byte[((ReadANDXExtensionResponse)data).getByteCount()-((ReadANDXExtensionResponse)data).getDataLength() -((ReadANDXExtensionResponse)data).getDataLengthHigh()];
				b.gets(pad);
				((ReadANDXExtensionResponse)data).setPad(pad);
				buff = new byte[((ReadANDXExtensionResponse)data).getDataLength() + ((ReadANDXExtensionResponse)data).getDataLengthHigh()];
				((ReadANDXExtensionResponse)data).setData(buff);
			}
		}
		else
		{
			data = new ReadANDXResponse();
			reserved2 = new byte[10];
			((ReadANDXResponse)data).setWordCount(b.get());
			((ReadANDXResponse)data).setAndxCommand(b.get());
			((ReadANDXResponse)data).setAndxReserved(b.get());
			((ReadANDXResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
			((ReadANDXResponse)data).setAvailable(ByteOrderConverter.swap(b.getShort()));
			((ReadANDXResponse)data).setDataCompactionMode(ByteOrderConverter.swap(b.getShort()));
			((ReadANDXResponse)data).setReserved1(ByteOrderConverter.swap(b.getShort()));
			((ReadANDXResponse)data).setDataLength(ByteOrderConverter.swap(b.getShort()));
			((ReadANDXResponse)data).setDataOffset(ByteOrderConverter.swap(b.getShort()));
			b.gets(reserved2);
			((ReadANDXResponse)data).setReserved2(reserved2);
			((ReadANDXResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != ((ReadANDXResponse)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			if(((ReadANDXResponse)data).getByteCount() == ((ReadANDXResponse)data).getDataLength()+1){
				((ReadANDXResponse)data).setPad(b.get()); // use NT LAN Manger dialect
			}
			
			buff = new byte[((ReadANDXResponse)data).getDataLength()];
			b.gets(buff);
			((ReadANDXResponse)data).setData(buff);
		}
		return data;
	}
}
