package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.NtTransSubCommandMapper;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NtTransactRequest;
import org.krakenapps.pcap.decoder.smb.response.NtTransactResponse;
import org.krakenapps.pcap.decoder.smb.rr.NtTransactCommand;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;

public class NtTransactParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NtTransactRequest data = new NtTransactRequest();
		NtTransSubCommandMapper mapper = new NtTransSubCommandMapper();
		byte []setup;
		byte []pad1;
		byte []pad2;
		byte []parameter;
		byte []buff;
		data.setWordCount(b.get());
		data.setMaxCount(b.get());
		data.setReserved1(ByteOrderConverter.swap(b.getShort()));
		data.setTotalparameterCount(ByteOrderConverter.swap(b.getInt()));
		data.setTotalDataCount(ByteOrderConverter.swap(b.getInt()));
		data.setMaxParameterCount(ByteOrderConverter.swap(b.getInt()));
		data.setMaxDataCount(ByteOrderConverter.swap(b.getInt()));
		data.setParameterCount(ByteOrderConverter.swap(b.getInt()));
		data.setParameterOffset(ByteOrderConverter.swap(b.getInt()));
		data.setDataCount(ByteOrderConverter.swap(b.getInt()));
		data.setDataOffset(ByteOrderConverter.swap(b.getInt()));
		data.setSetupCount(b.get());
		data.setFunction(ByteOrderConverter.swap(b.getShort()));
		setup = new byte[data.getSetupCount()*2];
		b.gets(setup);
		data.setSetup(setup);
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		
		if(data.getParameterCount() != 0){
			pad1 = new byte[data.getParameterOffset()-b.position()];
			if(pad1.length != 0){
				b.gets(pad1);
				data.setPad1(pad1);
			}
			parameter = new byte[data.getParameterCount()];
			b.gets(parameter);
			data.setNtTransParameters(parameter);
		}
		if(data.getDataCount() !=0){
			pad2 = new byte[data.getDataOffset()-b.position()];
			if(pad2.length != 0){
				b.gets(pad2);
				data.setPad2(pad2);
			}
			buff = new byte[data.getDataCount()];
			b.gets(buff);
			data.setNtTransData(buff);
		}
		Buffer setupBuffer = new ChainBuffer();
		Buffer parameterBuffer = new ChainBuffer();
		Buffer dataBuffer = new ChainBuffer();
		setupBuffer.addLast(data.getSetup());
		parameterBuffer.addLast(data.getNtTransParameters());
		dataBuffer.addLast(data.getNtTransData());
		if(setupBuffer.readableBytes() !=0){
			setupBuffer.mark();
		 	TransParser parser = mapper.getParser(NtTransactCommand.parse(data.getFunction()));
		 	setupBuffer.rewind();
			data.setNtTransactionData(parser.parseRequest(setupBuffer , parameterBuffer , dataBuffer));
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		NtTransactResponse data = new NtTransactResponse();
		NtTransSubCommandMapper mapper = new NtTransSubCommandMapper();
		byte []reserved1 = new byte[3];
		byte []setup;
		byte []pad1;
		byte []pad2;
		byte []parameter;
		byte []buff;
		data.setWordCount(b.get());
		if(data.getWordCount() >= 0x12){
			b.gets(reserved1);
			data.setReserved1(reserved1);
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getInt()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getInt()));
			data.setParameterCount(ByteOrderConverter.swap(b.getInt()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getInt()));
			data.setParameterDisplacement(ByteOrderConverter.swap(b.getInt()));
			data.setDataCount(ByteOrderConverter.swap(b.getInt()));
			data.setDataOffset(ByteOrderConverter.swap(b.getInt()));
			data.setDataDisplacement(ByteOrderConverter.swap(b.getInt()));
			data.setSetupCount(b.get());
			setup = new byte[data.getSetupCount()*2];
			b.gets(setup);
			data.setSetup(setup);
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(data.getByteCount() !=0){
			if(data.getParameterCount() != 0){
				pad1 = new byte[data.getParameterOffset()-b.position()];
				if(pad1.length != 0){
					b.gets(pad1);
					data.setPad1(pad1);
				}
				parameter = new byte[data.getParameterCount()];
				b.gets(parameter);
				data.setNtTransParameters(parameter);
			}
			if(data.getDataCount() !=0){
				pad2 = new byte[data.getDataOffset()-b.position()];
				if(pad2.length != 0){
					b.gets(pad2);
					data.setPad2(pad2);
				}
				buff = new byte[data.getDataCount()];
				b.gets(buff);
				data.setNtTransData(buff);
			}
			Buffer setupBuffer = new ChainBuffer();
			Buffer parameterBuffer = new ChainBuffer();
			Buffer dataBuffer = new ChainBuffer();
			setupBuffer.addLast(data.getSetup());
			parameterBuffer.addLast(data.getNtTransParameters());
			dataBuffer.addLast(data.getNtTransData());
			if(setupBuffer.readableBytes() + parameterBuffer.readableBytes() + dataBuffer.readableBytes() == 0){
				return data;
			}
			//TransParser parser = mapper.getParser(session.getLastNtTransCommand()NtTransactCommand.parse(ByteOrderConverter.swap(setupBuffer.getShort())));
			TransParser parser = mapper.getParser(session.getSessionNtTransCommand());
			if(parser ==null){
				return data;
			}
			data.setNtTransactionData(parser.parseResponse(setupBuffer , parameterBuffer , dataBuffer, session));
		}
		return data;
	}
}
