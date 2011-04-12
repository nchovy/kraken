package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.ntparser.*;
import org.krakenapps.pcap.decoder.smb.rr.NtTransactCommand;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;

public class NtTransSubCommandMapper {

	private Map<NtTransactCommand, TransParser> parsers;

	public NtTransSubCommandMapper() {
		parsers = new HashMap<NtTransactCommand, TransParser>();
		
		map(NtTransactCommand.NT_TRANSACT_IOCTL , new NtIoctlParser());
		map(NtTransactCommand.NT_TRANSACT_CREATE , new NtTransactCreateParser() );
		map(NtTransactCommand.NT_TRANSACT_NOTIFY_CHANGE, new NtTransactNotifyChangeParser());
		map(NtTransactCommand.NT_TRANSACT_QUERY_SECURITY_DESC, new NtTransactQuerySecurityDescParser());
		map(NtTransactCommand.NT_TRANSACT_RENAME , new NtTransactRenameParser());
		map(NtTransactCommand.NT_TRANSACT_SET_SECURITY_DESC , new NtTransactSetSecurityDescParser());
		map(NtTransactCommand.NT_TRANSACT_QUERY_QUOTA , new NtTransactQueryQuotaParser());
		map(NtTransactCommand.NT_TRANSACT_SET_QUOTA , new NtTransactSetQuotaParser());
//		map(NtTransactCommand.NT_TRANSACT_CREATE2 , new );
	}
	
	private void map(NtTransactCommand command, TransParser parser) {
		parsers.put(command, parser);
	}
	public TransParser getParser(NtTransactCommand code){
		return parsers.get(code);
	}
}
