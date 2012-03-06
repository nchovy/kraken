package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.comparser.CheckDirectoryParser;
import org.krakenapps.pcap.decoder.smb.comparser.CloseAndTreeDiscParser;
import org.krakenapps.pcap.decoder.smb.comparser.CloseParser;
import org.krakenapps.pcap.decoder.smb.comparser.ClosePrintFileParser;
import org.krakenapps.pcap.decoder.smb.comparser.CopyParser;
import org.krakenapps.pcap.decoder.smb.comparser.CreateDirectoryParser;
import org.krakenapps.pcap.decoder.smb.comparser.CreateNewParser;
import org.krakenapps.pcap.decoder.smb.comparser.CreateParser;
import org.krakenapps.pcap.decoder.smb.comparser.CreateTemporaryParser;
import org.krakenapps.pcap.decoder.smb.comparser.DeleteDirectoryParser;
import org.krakenapps.pcap.decoder.smb.comparser.DeleteParser;
import org.krakenapps.pcap.decoder.smb.comparser.EchoParser;
import org.krakenapps.pcap.decoder.smb.comparser.FindClose2Parser;
import org.krakenapps.pcap.decoder.smb.comparser.FindCloseParser;
import org.krakenapps.pcap.decoder.smb.comparser.FindNotifyCloseParser;
import org.krakenapps.pcap.decoder.smb.comparser.FindParser;
import org.krakenapps.pcap.decoder.smb.comparser.FindUniqueParser;
import org.krakenapps.pcap.decoder.smb.comparser.FlushParser;
import org.krakenapps.pcap.decoder.smb.comparser.GetPrintQueueParser;
import org.krakenapps.pcap.decoder.smb.comparser.IOCTLParser;
import org.krakenapps.pcap.decoder.smb.comparser.IOCTLSecondaryParser;
import org.krakenapps.pcap.decoder.smb.comparser.InvalidParser;
import org.krakenapps.pcap.decoder.smb.comparser.LockAndReadParser;
import org.krakenapps.pcap.decoder.smb.comparser.LockByteRangeParser;
import org.krakenapps.pcap.decoder.smb.comparser.LockingANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.LogoffANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.MoveParser;
import org.krakenapps.pcap.decoder.smb.comparser.NegotiateParser;
import org.krakenapps.pcap.decoder.smb.comparser.NewFileSizeParser;
import org.krakenapps.pcap.decoder.smb.comparser.NoANDXCommandParser;
import org.krakenapps.pcap.decoder.smb.comparser.NtCancelParser;
import org.krakenapps.pcap.decoder.smb.comparser.NtCreateANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.NtTransactParser;
import org.krakenapps.pcap.decoder.smb.comparser.NtTransactSecondaryParser;
import org.krakenapps.pcap.decoder.smb.comparser.OpenANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.OpenParser;
import org.krakenapps.pcap.decoder.smb.comparser.OpenPrintFileParser;
import org.krakenapps.pcap.decoder.smb.comparser.ProcessExitParser;
import org.krakenapps.pcap.decoder.smb.comparser.QueryInfo2Parser;
import org.krakenapps.pcap.decoder.smb.comparser.QueryInfoDiskParser;
import org.krakenapps.pcap.decoder.smb.comparser.QueryInfoParser;
import org.krakenapps.pcap.decoder.smb.comparser.QueryServerParser;
import org.krakenapps.pcap.decoder.smb.comparser.ReadANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.ReadBulkParser;
import org.krakenapps.pcap.decoder.smb.comparser.ReadMPXParser;
import org.krakenapps.pcap.decoder.smb.comparser.ReadMPXSecondaryParser;
import org.krakenapps.pcap.decoder.smb.comparser.ReadParser;
import org.krakenapps.pcap.decoder.smb.comparser.ReadRawParser;
import org.krakenapps.pcap.decoder.smb.comparser.RenameParser;
import org.krakenapps.pcap.decoder.smb.comparser.SearchParser;
import org.krakenapps.pcap.decoder.smb.comparser.SecurityPackageANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.SeekParser;
import org.krakenapps.pcap.decoder.smb.comparser.SessionSetupANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.SetInfo2Parser;
import org.krakenapps.pcap.decoder.smb.comparser.SetInfoParser;
import org.krakenapps.pcap.decoder.smb.comparser.SmbDataParser;
import org.krakenapps.pcap.decoder.smb.comparser.Transaction2Parser;
import org.krakenapps.pcap.decoder.smb.comparser.TransactionParser;
import org.krakenapps.pcap.decoder.smb.comparser.TransactionSecondaryParser;
import org.krakenapps.pcap.decoder.smb.comparser.TreeConnectANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.TreeConnectParser;
import org.krakenapps.pcap.decoder.smb.comparser.TreeDisconnectParser;
import org.krakenapps.pcap.decoder.smb.comparser.UnlockByteRangeParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteANDXParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteAndCloseParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteAndUnlockParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteBulkDataParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteCompleteParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteMPXParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteMPXSecondaryParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteParser;
import org.krakenapps.pcap.decoder.smb.comparser.WritePrintFileParser;
import org.krakenapps.pcap.decoder.smb.comparser.WriteRawParser;
import org.krakenapps.pcap.decoder.smb.rr.SmbCommand;

public class ComCommandMapper {

	private Map<SmbCommand , SmbDataParser> parsers;
	ComCommandMapper()
	{
		parsers = new HashMap<SmbCommand, SmbDataParser>();
		map(SmbCommand.SMB_COM_DELETE_DIRECTORY, new DeleteDirectoryParser());
		map(SmbCommand.SMB_COM_OPEN, new OpenParser());
		map(SmbCommand.SMB_COM_CREATE, new CreateParser());
		map(SmbCommand.SMB_COM_CLOSE, new CloseParser());
		map(SmbCommand.SMB_COM_FLUSH, new FlushParser());
		map(SmbCommand.SMB_COM_DELETE, new DeleteParser());
		map(SmbCommand.SMB_COM_RENAME, new RenameParser());
		map(SmbCommand.SMB_COM_QUERY_INFORMATION, new QueryInfoParser());
		map(SmbCommand.SMB_COM_READ, new ReadParser());
		map(SmbCommand.SMB_COM_WRITE, new WriteParser());
		map(SmbCommand.SMB_COM_LOCK_BYTE_RANGE, new LockByteRangeParser());
		map(SmbCommand.SMB_COM_CREATE_TEMPORARY, new CreateTemporaryParser());
		map(SmbCommand.SMB_COM_CREATE_NEW, new CreateNewParser());
		map(SmbCommand.SMB_COM_CREATE_DIRECTORY, new CreateDirectoryParser());
		map(SmbCommand.SMB_COM_PROCESS_EXIT, new ProcessExitParser());
		map(SmbCommand.SMB_COM_SEEK, new SeekParser());
		map(SmbCommand.SMB_COM_LOCK_AND_READ, new LockAndReadParser());
		map(SmbCommand.SMB_COM_WRITE_AND_UNLOCK, new WriteAndUnlockParser());
		map(SmbCommand.SMB_COM_READ_RAW, new ReadRawParser());
		map(SmbCommand.SMB_COM_READ_MPX, new ReadMPXParser());
		map(SmbCommand.SMB_COM_READ_MPX_SECONDARY, new ReadMPXSecondaryParser());
		map(SmbCommand.SMB_COM_WRITE_RAW, new WriteRawParser());
		map(SmbCommand.SMB_COM_WRITE_MPX, new WriteMPXParser());
		map(SmbCommand.SMB_COM_WRITE_MPX_SECONDARY,
				new WriteMPXSecondaryParser());
		map(SmbCommand.SMB_COM_UNLOCK_BYTE_RANGE , new UnlockByteRangeParser());
		map(SmbCommand.SMB_COM_WRITE_COMPLETE, new WriteCompleteParser());
		map(SmbCommand.SMB_COM_QUERY_SERVER, new QueryServerParser());
		map(SmbCommand.SMB_COM_SET_INFORMATION2, new SetInfo2Parser());
		map(SmbCommand.SMB_COM_QUERY_INFORMATION2, new QueryInfo2Parser());
		map(SmbCommand.SMB_COM_LOCKING_ANDX, new LockingANDXParser());
		map(SmbCommand.SMB_COM_TRANSACTION, new TransactionParser());
		map(SmbCommand.SMB_COM_TRANSACTION_SECONDARY,
				new TransactionSecondaryParser());
		map(SmbCommand.SMB_COM_IOCTL, new IOCTLParser());
		map(SmbCommand.SMB_COM_IOCTL_SECONDARY, new IOCTLSecondaryParser());
		map(SmbCommand.SMB_COM_COPY, new CopyParser());
		map(SmbCommand.SMB_COM_MOVE, new MoveParser());
		map(SmbCommand.SMB_COM_ECHO, new EchoParser());
		map(SmbCommand.SMB_COM_WRITE_AND_CLOSE, new WriteAndCloseParser());
		map(SmbCommand.SMB_COM_OPEN_ANDX, new OpenANDXParser());
		map(SmbCommand.SMB_COM_READ_ANDX, new ReadANDXParser());
		map(SmbCommand.SMB_COM_WRITE_ANDX, new WriteANDXParser());
		map(SmbCommand.SMB_COM_NEW_FILE_SIZE, new NewFileSizeParser());
		map(SmbCommand.SMB_COM_CLOSE_AND_TREE_DISC,
				new CloseAndTreeDiscParser());
		map(SmbCommand.SMB_COM_TRANSACTION2, new Transaction2Parser());
		map(SmbCommand.SMB_COM_TRANSACTION2_SECONDARY, new Transaction2Parser());
	//	map(SmbCommand.SMB_COM_TRANSACTION2_SECONDARY,
	//			new Transaction2SecondaryParser());
		map(SmbCommand.SMB_COM_FIND_CLOSE2, new FindClose2Parser());
		map(SmbCommand.SMB_COM_FIND_NOTIFY_CLOSE, new FindNotifyCloseParser());
		map(SmbCommand.SMB_COM_TREE_CONNECT, new TreeConnectParser());
		map(SmbCommand.SMB_COM_TREE_DISCONNECT, new TreeDisconnectParser());
		map(SmbCommand.SMB_COM_NEGOTIATE, new NegotiateParser());
		map(SmbCommand.SMB_COM_SESSION_SETUP_ANDX, new SessionSetupANDXParser());
		map(SmbCommand.SMB_COM_LOGOFF_ANDX, new LogoffANDXParser());
		map(SmbCommand.SMB_COM_TREE_CONNECT_ANDX, new TreeConnectANDXParser());
		map(SmbCommand.SMB_COM_SECURITY_PACKAGE_ANDX,
				new SecurityPackageANDXParser());
		map(SmbCommand.SMB_COM_QUERY_INFORMATION_DISK,
				new QueryInfoDiskParser());
		map(SmbCommand.SMB_COM_SEARCH, new SearchParser());
		map(SmbCommand.SMB_COM_FIND, new FindParser());
		map(SmbCommand.SMB_COM_FIND_UNIQUE, new FindUniqueParser());
		map(SmbCommand.SMB_COM_FIND_CLOSE, new FindCloseParser());
		map(SmbCommand.SMB_COM_NT_TRANSACT_SECONDARY,
				new NtTransactSecondaryParser());
		map(SmbCommand.SMB_COM_NT_TRANSACT,	new NtTransactParser());
		map(SmbCommand.SMB_COM_NT_CREATE_ANDX, new NtCreateANDXParser());
		map(SmbCommand.SMB_COM_NT_CANCEL, new NtCancelParser());
		map(SmbCommand.SMB_COM_NT_RENAME, new RenameParser());
		map(SmbCommand.SMB_COM_OPEN_PRINT_FILE, new OpenPrintFileParser());
		map(SmbCommand.SMB_COM_WRITE_PRINT_FILE, new WritePrintFileParser());
		map(SmbCommand.SMB_COM_CLOSE_PRINT_FILE, new ClosePrintFileParser());
		map(SmbCommand.SMB_COM_GET_PRINT_QUEUE, new GetPrintQueueParser());
		map(SmbCommand.SMB_COM_READ_BULK, new ReadBulkParser());
		// map(SmbCommand.SMB_COM_WRITE_BULK, new WriteBulkParser());
		map(SmbCommand.SMB_COM_WIRTE_BULK_DATA, new WriteBulkDataParser());
		map(SmbCommand.SMB_COM_INVALID, new InvalidParser());
		map(SmbCommand.SMB_COM_NO_ANDX_COMMAND, new NoANDXCommandParser());
		map(SmbCommand.SMB_COM_SET_INFORMATION , new SetInfoParser());
		map(SmbCommand.SMB_COM_CHECK_DIRECTORY , new CheckDirectoryParser());
	}

	private void map(SmbCommand command, SmbDataParser parser) {
		parsers.put(command, parser);
	}
	public SmbDataParser getComParser(SmbCommand command)
	{
		return parsers.get(command);
	}
}
