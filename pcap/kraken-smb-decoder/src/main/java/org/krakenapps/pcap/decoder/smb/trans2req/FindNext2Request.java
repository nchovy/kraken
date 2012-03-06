package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.structure.SmbGeaList;

public class FindNext2Request implements TransData {

	short subcommand;

	short sid;
	short searchCount;
	short informationLevel;
	int resumekey; // diffrent from SmbResumeKey
	short flags;
	String fileName;
	SmbGeaList getExtendedAttributesList;
	// this is data
	TransStruct struct;

	public TransStruct getStruct() {
		return struct;
	}

	public void setStruct(TransStruct struct) {
		this.struct = struct;
	}

	public SmbGeaList getGetExtendedAttributesList() {
		return getExtendedAttributesList;
	}

	public void setGetExtendedAttributesList(
			SmbGeaList getExtendedAttributesList) {
		this.getExtendedAttributesList = getExtendedAttributesList;
	}

	public short getSubcommand() {
		return subcommand;
	}

	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}

	public short getSid() {
		return sid;
	}

	public void setSid(short sid) {
		this.sid = sid;
	}

	public short getSearchCount() {
		return searchCount;
	}

	public void setSearchCount(short searchCount) {
		this.searchCount = searchCount;
	}

	public short getInformationLevel() {
		return informationLevel;
	}

	public void setInformationLevel(short informationLevel) {
		this.informationLevel = informationLevel;
	}

	public int getResumekey() {
		return resumekey;
	}

	public void setResumekey(int resumekey) {
		this.resumekey = resumekey;
	}

	public short getFlags() {
		return flags;
	}

	public void setFlags(short flags) {
		this.flags = flags;
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
		return String.format("Trans2 Second Level : Find Next2 Request\n" +
				"subCommand = 0x%s\n" +
				"sid = 0x%s,  searchCount = 0x%s, informationLevel = 0x%s\n" +
				"resumeKey = 0x%s, flags = 0x%s\n" +
				"fileName = %s\n" +
				"getExtendedAttributesList = 0x%s\n",
				Integer.toHexString(this.subcommand),
				Integer.toHexString(this.sid), Integer.toHexString(this.searchCount) , Integer.toHexString(this.informationLevel),
				Integer.toHexString(this.resumekey) , Integer.toHexString(this.flags),
				this.fileName,
				this.getExtendedAttributesList);
	}
}
