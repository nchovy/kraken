package org.krakenapps.pcap.decoder.smb.comparser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.Trans2SubcommandMapper;
import org.krakenapps.pcap.decoder.smb.request.Transaction2Request;
import org.krakenapps.pcap.decoder.smb.response.Transaction2Response;
import org.krakenapps.pcap.decoder.smb.rr.Transaction2Command;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;
//0x32
public class Transaction2Parser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		Transaction2Request data = new Transaction2Request();
		Trans2SubcommandMapper mapper = new Trans2SubcommandMapper();
		byte []setup;
		byte []pad1;
		byte []pad2;
		byte []parameters;
		byte []buff;
		data.setWordCount(b.get());
		if(data.getWordCount() >= 0x0E){
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setMaxParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setMaxDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setMaxSetupCount(b.get());
			data.setReserved1(b.get());
			data.setFlags(ByteOrderConverter.swap(b.getShort()));
			data.setTimeout(ByteOrderConverter.swap(b.getInt()));
			data.setReserved2(ByteOrderConverter.swap(b.getShort()));
			data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
			data.setDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
			data.setSetupCount(b.get());
			data.setReserved3(b.get());
			setup = new byte[data.getSetupCount()*2];
			b.gets(setup);
			data.setSetup(setup);
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		else if(data.isMalformed()){
			return data;
		}
		data.setName(NetBiosNameCodec.readOemName(b));
		
		if(data.getParameterCount() !=0){
			pad1 = new byte[data.getParameterOffset()-b.position()];
			if(pad1.length != 0){
				b.gets(pad1);
				data.setPad1(pad1);
			}
			parameters = new byte[data.getParameterCount()];
			b.gets(parameters);
			data.setTrans2Parameters(parameters);
		}
		if(data.getDataCount() !=0){
			pad2 = new byte[data.getDataOffset()-b.position()];
			if(pad2.length != 0){
				b.gets(pad2);
				data.setPad2(pad2);
			}
			buff = new byte[data.getDataCount()];
			b.gets(buff);
			data.setTrans2Data(buff);
		}
		
		Buffer setupBuffer = new ChainBuffer();
		Buffer parameterBuffer = new ChainBuffer();
		Buffer dataBuffer = new ChainBuffer();
		setupBuffer.addLast(data.getSetup()); 
		parameterBuffer.addLast(data.getTrans2Parameters());
		dataBuffer.addLast(data.getTrans2Data());
		setupBuffer.mark();
		TransParser parser = mapper.getParser(Transaction2Command.parse(ByteOrderConverter.swap(setupBuffer.getShort())));
		setupBuffer.reset();
		data.setTransaction2Data(parser.parseRequest( setupBuffer , parameterBuffer , dataBuffer));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		Transaction2Response data = new Transaction2Response();
		Trans2SubcommandMapper mapper = new Trans2SubcommandMapper();
		byte []setup;
		byte []pad1;
		byte []pad2;
		byte []parameters;
		byte []buff;
		data.setWordCount(b.get());
		if(data.getWordCount() != 0x00){
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setReserved1(ByteOrderConverter.swap(b.getShort()));
			data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
			data.setParameterDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
			data.setDataDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setSetupCount(b.get());
			data.setResreved2(b.get());
			setup = new byte[data.getSetupCount()*2];
			b.gets(setup);
			data.setSetup(setup);
		} // it final response
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		if(data.getByteCount() != 0x00){
			if(data.getParameterCount() !=0){
				pad1 = new byte[data.getParameterOffset()-b.position()];
				if(pad1.length != 0){
					b.gets(pad1);
					data.setPad1(pad1);
				}
				parameters = new byte[data.getParameterCount()];
				b.gets(parameters);
				data.setTrans2Parameter(parameters);
			}
			if(data.getDataCount() !=0){
				pad2 = new byte[data.getDataOffset()-b.position()];
				if(pad2.length != 0){
					b.gets(pad2);
					data.setPad2(pad2);
				}
				buff = new byte[data.getDataCount()];
				b.gets(buff);
				data.setTrans2Data(buff);
			}
		}
		Buffer setupBuffer = new ChainBuffer();
		Buffer parameterBuffer = new ChainBuffer();
		Buffer dataBuffer = new ChainBuffer();
		setupBuffer.addLast(data.getSetup()); 
		parameterBuffer.addLast(data.getTrans2Parameter());
		dataBuffer.addLast(data.getTrans2Data());
		if(setupBuffer.readableBytes() + parameterBuffer.readableBytes() + dataBuffer.readableBytes() == 0){
			return data;
		}
		//setupBuffer.mark();
		TransParser parser = mapper.getParser(session.getSessionTrans2Command());
		if(parser ==null){
			return data;
		}
		
		//add buffer;
		if(data.getTotalDataCount() != dataBuffer.readableBytes()){
			if(session.getTransBuffer().readableBytes() ==0){
				session.setParameterBuffer(parameterBuffer);
				session.setTransBuffer(dataBuffer);
			}
			else if(session.getTransBuffer().readableBytes() != 0){
				session.addTransBuffer(dataBuffer);
				if(data.getTotalDataCount() == session.getTransBuffer().readableBytes()){
					data.setTransaction2Data(parser.parseResponse( setupBuffer , session.getParameterBuffer() , session.getTransBuffer() , session));
					session.getTransBuffer().skip(session.getTransBuffer().readableBytes());
					session.getParameterBuffer().discardReadBytes();
				}
			}
			return data;
		}
		else{
			data.setTransaction2Data(parser.parseResponse( setupBuffer , parameterBuffer , dataBuffer , session));
		}
		return data;
	}
}
