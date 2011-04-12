package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;


import org.krakenapps.pcap.decoder.smb.rr.TransactionCommand;
import org.krakenapps.pcap.decoder.smb.transparser.CallNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.PeekNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.QueryNmpipeInfoParser;
import org.krakenapps.pcap.decoder.smb.transparser.RawReadNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.RawWriteNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.ReadNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.decoder.smb.transparser.TransactNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.WaitNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.WriteNmpipeParser;
import org.krakenapps.pcap.decoder.smb.transparser.setNmpipeStateParser;

public class TransSubcommandMapper {
	private Map<TransactionCommand, TransParser> parsers;

	public TransSubcommandMapper() {
		parsers = new HashMap<TransactionCommand, TransParser>();
		map(TransactionCommand.TRANS_SET_NMPIPE_STATE , new setNmpipeStateParser());
		map(TransactionCommand.TRANS_CALL_NMPIPE, new CallNmpipeParser());
		//map(TransactionCommand.TRANS_MAILSLOT_WRITE, new CallNmpipeParser()); // now not use this
		//TRANS_MAILSLOT_WRITE(0x0001),
		map(TransactionCommand.TRANS_RAW_READ_NMPIPE , new RawReadNmpipeParser() );
		map(TransactionCommand.TRANS_QUERY_NMPIPE_INFO, new QueryNmpipeInfoParser());
		map(TransactionCommand.TRANS_PEEK_NMPIPE, new PeekNmpipeParser());
		map(TransactionCommand.TRANS_TRANSACT_NMPIPE , new TransactNmpipeParser());
		map(TransactionCommand.TRANS_RAW_WRITE_NMPIPE , new RawWriteNmpipeParser());
		map(TransactionCommand.TRANS_READ_NMPIPE , new ReadNmpipeParser());
		map(TransactionCommand.TRANS_WRITE_NMPIPE, new WriteNmpipeParser());
		map(TransactionCommand.TRANS_WAIT_NMPIPE , new WaitNmpipeParser() );
		map(TransactionCommand.TRANS_CALL_NMPIPE , new CallNmpipeParser());
	}
	
	private void map(TransactionCommand command, TransParser parser) {
		parsers.put(command, parser);
	}
	public TransParser getParser(TransactionCommand code){
//		System.out.println("this command = " + code);
		return parsers.get(code);
	}
}
