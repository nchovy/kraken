package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.rr.Transaction2Command;
import org.krakenapps.pcap.decoder.smb.trans2parser.*;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;

public class Trans2SubcommandMapper {
	private Map<Transaction2Command, TransParser> parsers;
	
	public Trans2SubcommandMapper(){
	parsers = new HashMap<Transaction2Command, TransParser>();
	map(Transaction2Command.TRANS2_OPEN2 , new Open2Parser());
	map(Transaction2Command.TRANS2_FIND_FIRST2 , new FindFirst2Parser());
	map(Transaction2Command.TRANS2_FIND_NEXT2, new FindNext2Parser());
	map(Transaction2Command.TRANS_QUERY_FS_INFORMATION, new QueryFsInformationParser());
	map(Transaction2Command.TRANS2_SET_FS_INFORMATION , new SetFsInformationParser());
	map(Transaction2Command.TRANS2_QUERY_PATH_INFORMATION, new QueryPathInformationParser());
	map(Transaction2Command.TRANS2_SET_PATH_INFORMATION, new SetPathInformationParser());
	map(Transaction2Command.TRANS2_QUERY_FILE_INFORMATION, new QueryFileInformationParser());
	map(Transaction2Command.TRANS2_SET_FILE_INFORMATION, new SetFileInformationParser());
	map(Transaction2Command.TRANS2_FSCTL,new FsctlParser());
	map(Transaction2Command.TRANS2_IOCTL2,new Ioctl2Parser());
	map(Transaction2Command.TRANS2_FIND_NOTIFY_FIRST, new FindNotifyFirstParser());
	map(Transaction2Command.TRANS2_FIND_NOTIFY_NEXT, new FindNotifyNextParser());
	map(Transaction2Command.TRANS2_CREATE_DIRECTORY , new Trans2CreateDirectoryParser());
	map(Transaction2Command.TRANS2_SESSION_SETUP, new SessionSetupParser());
	map(Transaction2Command.TRANS2_GET_DFS_REFERRAL, new GetDfsReferralParser());
	map(Transaction2Command.TRANS2_REPORT_DFS_INCONSITENCY , new ReportDfsInconsistencyParser());
	}
	public TransParser getParser(Transaction2Command code){
		return parsers.get(code);
	}
	private void map(Transaction2Command command, TransParser parser) {
		parsers.put(command, parser);
	}
}
