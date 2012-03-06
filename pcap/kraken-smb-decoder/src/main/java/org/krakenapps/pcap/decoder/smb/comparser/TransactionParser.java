package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransSubcommandMapper;
import org.krakenapps.pcap.decoder.smb.request.TransactionRequest;
import org.krakenapps.pcap.decoder.smb.response.TransactionResponse;
import org.krakenapps.pcap.decoder.smb.rr.ErrorClass;
import org.krakenapps.pcap.decoder.smb.rr.TransactionCommand;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.decoder.smb.structure.SmbStatus;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;
//0x25
public class TransactionParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		TransactionRequest data = new TransactionRequest();
		TransSubcommandMapper mapper = new TransSubcommandMapper();
		byte []setup=null;
		byte []parameter=null;
		byte []pad1=null;
		byte []pad2=null;
		byte []buff=null;
		data.setWordCount(b.get());
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
		data.setSetup(setup);// short type align
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		data.setName(NetBiosNameCodec.readOemName(b));
		
		// following section is data section 
		if(data.getParameterCount() != 0){
			pad1 = new byte[data.getParameterOffset()-b.position()];
			if(pad1.length != 0){
				b.gets(pad1);
				data.setPad1(pad1);
			}
			parameter = new byte[data.getParameterCount()];
			b.gets(parameter);
			data.setTransParameters(parameter);
		}
		if(data.getDataCount() !=0){
			pad2 = new byte[data.getDataOffset()-b.position()];
			if(pad2.length != 0){
				b.gets(pad2);
				data.setPad2(pad2);
			}
			buff = new byte[data.getDataCount()];
			b.gets(buff);
			data.setTransData(buff);
		}
		// minus pad1,pad2,name,paramter length
		Buffer setupBuffer = new ChainBuffer();
		Buffer parameterBuffer = new ChainBuffer();
		Buffer dataBuffer = new ChainBuffer();
		setupBuffer.addLast(data.getSetup()); 
		parameterBuffer.addLast(data.getTransParameters());
		dataBuffer.addLast(data.getTransData());
		if(setupBuffer.readableBytes() !=0){
			setupBuffer.mark();
			TransParser parser = mapper.getParser(TransactionCommand.parse(ByteOrderConverter.swap(setupBuffer.getShort())));
			setupBuffer.rewind();
			data.setTransactionData(parser.parseRequest( setupBuffer , parameterBuffer , dataBuffer));
		}
		data.setUpper(dataBuffer);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		TransactionResponse data = new TransactionResponse();
		TransSubcommandMapper mapper = new TransSubcommandMapper();
		SmbStatus status = h.getStatus();
		if(status.getErrorClass() != ErrorClass.SUCCESS){
			data.setWordCount(b.get());
			data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		}
		else
		{
			byte []setup;
			byte []parameter;
			byte []pad1;
			byte []pad2;
			byte []buff;
			data.setWordCount(b.get());
			//System.out.println(data.getWordCount());
			data.setTotalParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setTotalDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setReserved1(ByteOrderConverter.swap(b.getShort()));
			data.setParameterCount(ByteOrderConverter.swap(b.getShort()));
			data.setParameterOffset(ByteOrderConverter.swap(b.getShort()));
			data.setParamterDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setDataCount(ByteOrderConverter.swap(b.getShort()));
			data.setDataOffset(ByteOrderConverter.swap(b.getShort()));
			data.setDataDisplacement(ByteOrderConverter.swap(b.getShort()));
			data.setSetupCount(b.get());
			data.setReserved2(b.get());
			setup = new byte[data.getSetupCount()*2];
			b.gets(setup);
			data.setSetup(setup);
			data.setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != data.getByteCount()){
				data.setMalformed(true);
				return data;
			}
			if(data.getParameterCount() != 0){
				pad1 = new byte[data.getParameterOffset()-b.position()];
				if(pad1.length != 0){
					b.gets(pad1);
					data.setPad1(pad1);
				}
				parameter = new byte[data.getParameterCount()];
				b.gets(parameter);
				data.setTransParameters(parameter);
			}
			if(data.getDataCount() !=0){
				pad2 = new byte[data.getDataOffset()-b.position()];
				if(pad2.length != 0){
					b.gets(pad2);
					data.setPad2(pad2);
				}
				buff = new byte[data.getDataCount()];
				b.gets(buff);
				data.setTransData(buff);
			}
			
			Buffer setupBuffer = new ChainBuffer();
			Buffer parameterBuffer = new ChainBuffer();
			Buffer dataBuffer = new ChainBuffer();
			setupBuffer.addLast(data.getSetup()); 
			parameterBuffer.addLast(data.getTransParameters());
			dataBuffer.addLast(data.getTransData());
			if(setupBuffer.readableBytes() + parameterBuffer.readableBytes() + dataBuffer.readableBytes() == 0){
				return data;
			}
			//TransParser parser = mapper.getParser(TransactionCommand.parse(ByteOrderConverter.swap(setupBuffer.getShort())));
			TransParser parser = mapper.getParser(session.getSessionTransCommand());
			if(parser ==null){
				return data;
			}
			data.setTransactionData(parser.parseResponse( setupBuffer , parameterBuffer , dataBuffer, session));
			data.setUpper(dataBuffer);
		}
		return data;
	}
}
