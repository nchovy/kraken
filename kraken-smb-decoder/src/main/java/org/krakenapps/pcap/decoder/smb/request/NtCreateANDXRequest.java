package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;


//0xA2
public class NtCreateANDXRequest implements SmbData{

	boolean malformed = false;
	//flags
	public static final int NT_CREATE_REQUEST_OPLOCK = 0x00000002;
	public static final int NT_CREATE_REQUEST_OPBATCH = 0x00000004;
	public static final int NT_CREATE_OPEN_TARGET_DIR = 0x00000008;
	public static final int NT_CREATE_REQUEST_EXTENDED_RESPONSE = 0x00000010;
	//flags
	
	//impresionationlevel
	public static final int SECURITY_ANONYMOUS = 0x00000000;
	public static final int SECURITY_IDENTIFICATION = 0x00000001;
	public static final int SECURITY_IMPRESIONATION = 0x00000002;
	public static final int SECURITY_DELEGATION = 0x00000003;
	//impresionationlevel
	
	//Desired Access
	public static final int FILE_READ_DATA = 0x00000001;
	public static final int FILE_WRITE_DATA = 0x00000002;
	public static final int FILE_APPEND_DATA = 0x00000004;
	public static final int FILE_READ_EA = 0x00000008;
	public static final int FILE_WIRTE_EA = 0x00000010;
	public static final int FILE_EXECUTE = 0x00000020;
	public static final int FILE_READ_ATTRIBUTES = 0x00000080;
	public static final int FILE_WRITE_ATTRIBUTES = 0x00000100;
	public static final int DELETE = 0x00010000;
	public static final int READ_CONTROL = 0x00020000;
	public static final int WRITE_DAC = 0x00040000;
	public static final int WRITE_OWNER = 0x00080000;
	public static final int SYNCHRONIZE = 0x00100000;
	public static final int ACCESS_SYSTEM_SECURITY = 0x01000000;
	public static final int MAXIMUM_ALLOWED = 0x02000000;
	public static final int GENERIC_ALL = 0x10000000;
	public static final int GENERIC_EXECUTE = 0x20000000;
	public static final int GENERIC_WRITE = 0x40000000;
	public static final int GENERIC_READ = 0x80000000;
	//Desired Access
	
	//shareAccess
	public static final int FILE_SHARE_NONE = 0x00000000;
	public static final int FILE_SHARE_READ = 0x00000001;
	public static final int FILE_SHARE_WRITE = 0x00000002;
	public static final int FILE_SHARE_DELETE = 0x00000004;
	//shareAccess
	//CreateDisposition
	public static final int FILE_SUPERSEDE = 0x00000000;
	public static final int FILE_OPEN = 0x00000001;
	public static final int FILE_CREATE = 0x00000002;
	public static final int FILE_OPEN_IF = 0x00000003;
	public static final int FILE_OVERWIRTE = 0x00000004;
	public static final int FILE_OVERWRITE_IF = 0x00000005;
	//CreateDisposition
	//CreateOption
	public static final int FILE_DIRECTORY_FILE =0x00000001;
	public static final int FILE_WIRTE_THOUGH = 0x00000002;
	public static final int FILE_SEQUENCE_ONLY = 0x00000004;
	public static final int FILE_NO_INTERMEDIATE_BUFFERING = 0x00000008;
	public static final int FILE_SYNCHRONOUS_IO_ALERT = 0x00000010;
	public static final int FILE_SYNCHRONOUS_IO_NONALERT = 0x00000020;
	public static final int FILE_NON_DIRECTORY_FILE = 0x00000040;
	public static final int FILE_CREATE_TREE_CONNECTION = 0x00000080;
	public static final int FILE_COMPLETE_IF_OPLOCKED = 0x00000100;
	public static final int FILE_NO_EA_KNOWLEDGE = 0x00000200;
	public static final int FILE_OPEN_FOR_RECOVERY = 0x00000400;
	public static final int FILE_RANDOM_ACCESS = 0x00000800;
	public static final int FILE_DELETE_ON_CLOSE = 0x00001000;
	public static final int FILE_OPEN_FILE_ID = 0x00002000;
	public static final int FILE_OPEN_FOR_BACKUP_INTENT = 0x00004000;
	public static final int FILE_NO_COMPRESSION = 0x00008000;
	public static final int FILE_RESERVE_OPFILTER = 0x00100000;
	public static final int FILE_OPEN_NO_RECALL = 0x00400000;
	public static final int FILE_OPEN_FOR_FREE_SPACE_QUEURY = 0x00800000;
	//CreateOption
	public static final byte SMB_SECURITY_CONTEXT_TRACKING = 0x01;
	public static final byte SMB_SECURITY_EFFECTIVE_ONLY = 0x02;
	//SecurityFlag
	
	//SEcurityFlag
	//param
	byte wordCount;
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	byte reserved;
	short nameLength;
	int flags;
	int rootDirectoryFID;
	int desiredAccess;
	long allocationSize;
	ExtFileAttributes extFileAttributes;
	int shareAccess;
	int createDisposition;
	int createOptions;
	int impersonationLevel;
	byte securityFlags;
	//data
	short byteCount;
	String fileName;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public byte getAndxCommand() {
		return andxCommand;
	}
	public void setAndxCommand(byte andxCommand) {
		this.andxCommand = andxCommand;
	}
	public byte getAndxReserved() {
		return andxReserved;
	}
	public void setAndxReserved(byte andxReserved) {
		this.andxReserved = andxReserved;
	}
	public short getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(short andxOffset) {
		this.andxOffset = andxOffset;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public short getNameLength() {
		return nameLength;
	}
	public void setNameLength(short nameLength) {
		this.nameLength = nameLength;
	}
	public int getFlags() {
		return flags;
	}
	public void setFlags(int flags) {
		this.flags = flags;
	}
	public int getRootDirectoryFID() {
		return rootDirectoryFID;
	}
	public void setRootDirectoryFID(int rootDirectoryFID) {
		this.rootDirectoryFID = rootDirectoryFID;
	}
	public int getDesiredAccess() {
		return desiredAccess;
	}
	public void setDesiredAccess(int desiredAccess) {
		this.desiredAccess = desiredAccess;
	}
	public long getAllocationSize() {
		return allocationSize;
	}
	public void setAllocationSize(long allocationSize) {
		this.allocationSize = allocationSize;
	}
	public ExtFileAttributes getExtFileAttributes() {
		return extFileAttributes;
	}
	public void setExtFileAttributes(ExtFileAttributes extFileAttributes) {
		this.extFileAttributes = extFileAttributes;
	}
	public int getShareAccess() {
		return shareAccess;
	}
	public void setShareAccess(int shortAccess) {
		this.shareAccess = shortAccess;
	}
	public int getCreateDisposition() {
		return createDisposition;
	}
	public void setCreateDisposition(int createDisposition) {
		this.createDisposition = createDisposition;
	}
	public int getCreateOptions() {
		return createOptions;
	}
	public void setCreateOptions(int createOptions) {
		this.createOptions = createOptions;
	}
	public int getImpersonationLevel() {
		return impersonationLevel;
	}
	public void setImpersonationLevel(int impersonationLevel) {
		this.impersonationLevel = impersonationLevel;
	}
	public byte getSecurityFlags() {
		return securityFlags;
	}
	public void setSecurityFlags(byte sercurityFlags) {
		this.securityFlags = sercurityFlags;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean isNtCreateRequestOplock()
	{
		if((this.flags & NT_CREATE_REQUEST_OPLOCK) == NT_CREATE_REQUEST_OPLOCK)
			return true;
		else
			return false;
	}
	public boolean isNtCreateRequestOpbatch()
	{
		if((this.flags & NT_CREATE_REQUEST_OPBATCH) == NT_CREATE_REQUEST_OPBATCH)
			return true;
		else
			return false;
	}
	public boolean isNtCreateOpenTargetDir()
	{
		if((this.flags & NT_CREATE_OPEN_TARGET_DIR) == NT_CREATE_OPEN_TARGET_DIR)
			return true;
		else
			return false;
	}
	
	public boolean isNtCreateRequestExtendedResponse()
	{
		if((this.flags & NT_CREATE_REQUEST_EXTENDED_RESPONSE)==NT_CREATE_REQUEST_EXTENDED_RESPONSE)
			return true;
		else
			return false;
	}
	public boolean isDesireFileReadData()
	{
		if((this.desiredAccess & FILE_READ_DATA) == FILE_READ_DATA)
			return true;
		else
			return false;
	}
	public boolean isDesireFileWriteData()
	{
		if((this.desiredAccess & FILE_WRITE_DATA) ==FILE_READ_DATA)
			return true;
		else
			return false;
	}
	public boolean isDesireFileAppendData()
	{
		if((this.desiredAccess & FILE_APPEND_DATA) == FILE_APPEND_DATA)
			return true;
		else
			return false;
	}
	public boolean isDesireFileReadEa()
	{
		if((this.desiredAccess & FILE_READ_EA) == FILE_READ_EA)
			return true;
		else
			return false;
	}
	public boolean isDesireFileWirteEa()
	{
		if((this.desiredAccess & FILE_WIRTE_EA) == FILE_WIRTE_EA)
			return true;
		else
			return false;
	}
	public boolean isDesireFileExecute()
	{
		if((this.desiredAccess & FILE_EXECUTE) == FILE_EXECUTE)
			return true;
		else
			return false;
	}
	public boolean isDesireFileReadAttributes()
	{
		if((this.desiredAccess & FILE_READ_ATTRIBUTES) == FILE_READ_ATTRIBUTES)
			return true;
		else
			return false;
	}
	public boolean isDesireFileWriteAttiributes()
	{
		if((this.desiredAccess & FILE_WRITE_ATTRIBUTES) == FILE_WRITE_ATTRIBUTES)
			return true;
		else
			return false;
		
	}
	public boolean isDesiredDelete()
	{
		if((this.desiredAccess & DELETE) == DELETE)
			return true;
		else
			return false;
	}
	public boolean isDesireReadContorl()
	{
		if((this.desiredAccess & READ_CONTROL) == READ_CONTROL)
			return true;
		else
			return false;
	}
	public boolean isDesireWriteDac()
	{
		if((this.desiredAccess & WRITE_DAC) == WRITE_DAC)
			return true;
		else
			return false;
	}
	public boolean isDesireWriteOwner()
	{
		if((this.desiredAccess & WRITE_OWNER) == WRITE_OWNER)
			return true;
		else
			return false;
	}
	public boolean isDesireSynchronize()
	{
		if((this.desiredAccess & SYNCHRONIZE) == SYNCHRONIZE)
			return true;
		else
			return false;
	}
	public boolean isDesireAccessSystemSecurity()
	{
		if((this.desiredAccess & ACCESS_SYSTEM_SECURITY) == ACCESS_SYSTEM_SECURITY)
			return true;
		else
			return false;
	}
	public boolean isDesireMaximumAllowed()
	{
		if((this.desiredAccess & MAXIMUM_ALLOWED) == MAXIMUM_ALLOWED)
			return true;
		else
			return false;
	}
	public boolean isDesireGenericAll()
	{
		if((this.desiredAccess & GENERIC_ALL) == GENERIC_ALL)
			return true;
		else
			return false;
	}
	public boolean isDesireGenericExecute()
	{
		if((this.desiredAccess & GENERIC_EXECUTE) == GENERIC_EXECUTE)
			return true;
		else
			return false;
	}
	public boolean isDesireGenericWrite()
	{
		if((this.desiredAccess & GENERIC_WRITE) == GENERIC_WRITE)
			return true;
		else
			return false;
	}
	public boolean isDesireGenericRead()
	{
		if((this.desiredAccess & GENERIC_READ) == GENERIC_READ)
			return true;
		else
			return false;
	}
	//shareAccess
	
	public boolean isShareFileShareNone()
	{
		if((this.shareAccess & FILE_SHARE_NONE) == FILE_SHARE_NONE)
			return true;
		else
			return false;
	}
	public boolean isShareFileShareRead()
	{
		if((this.shareAccess & FILE_SHARE_READ) == FILE_SHARE_READ)
			return true;
		else
			return false;
	}
	public boolean isShareFileShareWrite()
	{
		if((this.shareAccess & FILE_SHARE_WRITE) == FILE_SHARE_WRITE)
			return true;
		else
			return false;
	}
	public boolean isShareFileShareDelete()
	{
		if((this.shareAccess & FILE_SHARE_DELETE)== FILE_SHARE_DELETE)
			return true;
		else
			return false;
				
	}
	//shareAccess
	//CreateDisposition
	public boolean isDispositionFileSupersede()
	{
		if((this.createDisposition & FILE_SUPERSEDE) == FILE_SUPERSEDE)
			return true;
		else
			return false;
	}
	public boolean isDispositionFileOpen()
	{
		if((this.createDisposition & FILE_OPEN) == FILE_OPEN)
			return true;
		else
			return false;
	}
	public boolean isDispositionFileCreate()
	{
		if((this.createDisposition & FILE_CREATE) == FILE_CREATE)
			return true;
		else
			return false;
	}
	public boolean isDispositionFileOpenIf()
	{
		if((this.createDisposition & FILE_OPEN_IF) == FILE_OPEN_IF)
			return true;
		else
			return false;
	}
	public boolean isDispositionFileoverWrite()
	{
		if((this.createDisposition & FILE_OVERWIRTE) == FILE_OVERWIRTE)
			return true;
		else
			return false;
	}
	public boolean isDispositionFileOverWirteIf()
	{
		if((this.createDisposition & FILE_OVERWRITE_IF) == FILE_OVERWRITE_IF)
			return true;
		else
			return false;
	}
	//CreateDisposition
	//CreateOption
	public boolean isCreateFileDirectoryFile()
	{
		if((this.createOptions & FILE_DIRECTORY_FILE) == FILE_DIRECTORY_FILE)
			return true;
		else
			return false;
	}
	public boolean isCreateFileWriteThough()
	{
		if((this.createOptions & FILE_WIRTE_THOUGH) == FILE_WIRTE_THOUGH)
			return true;
		else
			return false;
	}
	public boolean isCreateFileSequenceOnly()
	{
		if((this.createOptions & FILE_SEQUENCE_ONLY)==FILE_SEQUENCE_ONLY)
			return true;
		else
			return false;
	}
	public boolean isCreateFileNoInterMediateBuffering()
	{
		if((this.createOptions & FILE_NO_INTERMEDIATE_BUFFERING) == FILE_NO_INTERMEDIATE_BUFFERING)
			return true;
		else
			return false;
	}
	public boolean isCreateFileSynchronousIoAlert()
	{
		if((this.createOptions & FILE_SYNCHRONOUS_IO_ALERT) == FILE_SYNCHRONOUS_IO_ALERT)
			return true;
		else
			return false;
	}
	public boolean isCreateFileSynchronousIoNonalert()
	{
		if((this.createOptions & FILE_SYNCHRONOUS_IO_NONALERT) == FILE_SYNCHRONOUS_IO_NONALERT)
			return true;
		else
			return false;
	}
	public boolean isCreateFileNonDirectoryFile()
	{
		if((this.createOptions & FILE_NON_DIRECTORY_FILE) == FILE_NON_DIRECTORY_FILE)
			return true;
		else
			return false;
	}
	public boolean isCreateFileCreateTreeConnection()
	{
		if((this.createOptions & FILE_CREATE_TREE_CONNECTION) == FILE_CREATE_TREE_CONNECTION)
			return true;
		else
			return false;
	}
	public boolean isCreateFileCompleteIfOplocked()
	{
		if((this.createOptions & FILE_COMPLETE_IF_OPLOCKED) == FILE_COMPLETE_IF_OPLOCKED)
			return true;
		else 
			return false;
	}
	public boolean isCreateFileNoEaKnowledge()
	{
		if((this.createOptions & FILE_NO_EA_KNOWLEDGE) == FILE_NO_EA_KNOWLEDGE)
			return true;
		else
			return false;
	}
	public boolean isCreateFileOpenForRecovery()
	{
		if((this.createOptions & FILE_OPEN_FOR_RECOVERY) == FILE_OPEN_FOR_RECOVERY)
			return true;
		else
			return false;
	}
	public boolean isCreateFileRandomAccess()
	{
		if((this.createOptions & FILE_RANDOM_ACCESS) == FILE_RANDOM_ACCESS)
			return true;
		else
			return false;
	}
	public boolean isCreateFileDeleteOnClose()
	{
		if((this.createOptions & FILE_DELETE_ON_CLOSE) == FILE_DELETE_ON_CLOSE)
			return true;
		else
			return false;
	}
	public boolean isCreateFileOpenFileId()
	{
		if((this.createOptions & FILE_OPEN_FILE_ID) == FILE_OPEN_FILE_ID)
			return true;
		else
			return false;
	}
	public boolean isCreateFileOpenForBackupIntent()
	{
		if((this.createOptions & FILE_OPEN_FOR_BACKUP_INTENT) == FILE_OPEN_FOR_BACKUP_INTENT)
			return true;
		else
			return false;
	}
	public boolean isCreateFilenoCompresssion()
	{
		if((this.createOptions & FILE_NO_COMPRESSION) == FILE_NO_COMPRESSION)
			return true;
		else
			return false;
	}
	public boolean isCreateFileReserveOpfilter()
	{
		if((this.createOptions & FILE_RESERVE_OPFILTER) == FILE_RESERVE_OPFILTER)
			return true;
		else
			return false;
	}
	public boolean isCreateFileOpennoRecall()
	{
		if((this.createOptions & FILE_OPEN_NO_RECALL) == FILE_OPEN_NO_RECALL)
			return true;
		else
			return false;
	}
	public boolean isCreateFileOpenForfreeSpaceQuery()
	{
		if((this.createOptions & FILE_OPEN_FOR_FREE_SPACE_QUEURY) == FILE_OPEN_FOR_FREE_SPACE_QUEURY)
			return true;
		else
			return false;
	}
	//CreateOption
	public boolean isSercurityContectTracking()
	{
		if((this.securityFlags & SMB_SECURITY_CONTEXT_TRACKING) == SMB_SECURITY_CONTEXT_TRACKING)
			return true;
		else
			return false;
	}
	public boolean isSecurityEffectiveOnly()
	{
		if((this.securityFlags & SMB_SECURITY_EFFECTIVE_ONLY) == SMB_SECURITY_EFFECTIVE_ONLY)
			return true;
		else
			return false;
	}
	@Override
	public boolean isMalformed() {
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	//SecurityFlag
	@Override
	public String toString(){
		return String.format("First Level : Nt Create Andx Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n"+
				"reserved = 0x%s , nameLength = 0x%s , flags = 0x%s\n"+
				"rootDirectoryFid = 0x%s , desiredAccess = 0x%s , allocationSize = 0x%s\n"+
				"extFileAttribute = %s , shareAccess = 0x%s , createDisposition 0x%s\n"+
				"createOptions = 0x%s , impersonationLevel = 0x%s , securityFlags = 0x%s\n"+
				"byteCount = 0x%s\n"+
				"fileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.reserved) , Integer.toHexString(this.nameLength) , Integer.toHexString(this.flags),
				Integer.toHexString(this.rootDirectoryFID) , Integer.toHexString(this.desiredAccess) , Long.toHexString(this.allocationSize),
				this.extFileAttributes , Integer.toHexString(this.shareAccess) , Integer.toHexString(this.createDisposition),
				Integer.toHexString(this.createOptions) , Integer.toHexString(this.impersonationLevel) , Integer.toHexString(this.securityFlags),
				Integer.toHexString(this.byteCount),
				this.fileName);
		
	}
}
