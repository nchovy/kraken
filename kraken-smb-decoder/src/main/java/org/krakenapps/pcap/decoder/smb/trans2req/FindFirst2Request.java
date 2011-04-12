package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbFeaList;

public class FindFirst2Request implements TransData {
	short subcommand;
	FileAttributes searchattrbibutes;
	short searchCount;
	short flags;
	short informationLevel;
	int searchStorageType;
	String fileName;
	SmbFeaList getExtendedAttributeList;
	TransStruct struct;

	public TransStruct getStruct() {
		return struct;
	}

	public void setStruct(TransStruct struct) {
		this.struct = struct;
	}

	public SmbFeaList getGetExtendedAttributeList() {
		return getExtendedAttributeList;
	}

	public void setGetExtendedAttributeList(SmbFeaList getExtendedAttributeList) {
		this.getExtendedAttributeList = getExtendedAttributeList;
	}

	public short getSubcommand() {
		return subcommand;
	}

	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}

	public FileAttributes getSearchattrbibutes() {
		return searchattrbibutes;
	}

	public void setSearchattrbibutes(FileAttributes searchattrbibutes) {
		this.searchattrbibutes = searchattrbibutes;
	}

	public short getSearchCount() {
		return searchCount;
	}

	public void setSearchCount(short searchCount) {
		this.searchCount = searchCount;
	}

	public short getFlags() {
		return flags;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	public short getInformationLevel() {
		return informationLevel;
	}

	public void setInformationLevel(short informationLevel) {
		this.informationLevel = informationLevel;
	}

	public int getSearchStorageType() {
		return searchStorageType;
	}

	public void setSearchStorageType(int searchStorageType) {
		this.searchStorageType = searchStorageType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isFindCloseAfterRequest() {
		return (this.flags & SMB_FIND_CLOSE_AFTER_REQUEST) == SMB_FIND_CLOSE_AFTER_REQUEST;
	}

	public boolean isFindCloseAtEos() {
		return (this.flags & SMB_FIND_CLOSE_AT_EOS) == SMB_FIND_CLOSE_AT_EOS;
	}

	public boolean isFindReturnResumeKeys() {
		return (this.flags & SMB_FIND_RETURN_RESUME_KEYS) == SMB_FIND_RETURN_RESUME_KEYS;
	}

	public boolean isFindContinueFromLast() {
		return (this.flags & SMB_FIND_CONTINUE_FROM_LAST) == SMB_FIND_CONTINUE_FROM_LAST;
	}

	public boolean isFindWithBackupIntent() {
		return (this.flags & SMB_FIND_WITH_BACKUP_INTENT) == SMB_FIND_WITH_BACKUP_INTENT;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Find First2 Request\n" +
				"subCommand = 0x%s\n" +
				"searchAttributes = %s , searchCount = 0x%s, flags = 0x%s\n" +
				"informationLevel  = 0x%s , searchStorageType = 0x%s\n" +
				"fileName = %s\n" +
				"getExtendedAttribyteList = %s\n" +
				"struct = %s\n",
				Integer.toHexString(this.subcommand),
				this.searchattrbibutes , Integer.toHexString(this.searchCount) , Integer.toHexString(this.flags),
				Integer.toHexString(this.informationLevel) , Integer.toHexString(this.searchStorageType),
				this.fileName,
				this.getExtendedAttributeList,
				this.struct);
	}
}
