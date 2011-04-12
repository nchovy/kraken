package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.comparser.SmbDataParser;
import org.krakenapps.pcap.decoder.smb.rr.SmbCommand;
import org.krakenapps.pcap.decoder.smb.udp.UdpTransactionParser;

public class UdpCommandMapper {

	private Map<SmbCommand, SmbDataParser> parsers;
	public UdpCommandMapper() {
		parsers = new HashMap<SmbCommand, SmbDataParser>();
		map(SmbCommand.SMB_COM_TRANSACTION, new UdpTransactionParser());
	}
	public void map(SmbCommand command , SmbDataParser parser){
		parsers.put(command, parser);
	}
	public SmbDataParser getParser(SmbCommand parse) {
		return parsers.get(parse);
	}
}
