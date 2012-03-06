package org.krakenapps.pcap.decoder.smb.structure;

import java.nio.charset.Charset;

import org.krakenapps.pcap.decoder.smb.rr.ErrorClass;
import org.krakenapps.pcap.decoder.smb.rr.ErrorCode;
import org.krakenapps.pcap.decoder.smb.rr.SmbCommand;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbHeader {
	//header flag
	public static final byte SMB_FLAGS_LOCK_AND_READ_OK = 0x01;
	public static final byte SMB_FLAGS_BUF_AVAIL = 0x02;
	public static final byte Reserved = 0x04;
	public static final byte SMB_FLAGS_CASE_INSENSITIVE = 0x08;
	public static final byte SMB_FLAGS_CANONICALIZED_PATHS = 0x10;
	public static final byte SMB_FLAGS_OPLOCK = 0x20;
	public static final byte SMB_FLAGS_OPBATCH = 0x40;
	public static final byte SMB_FLAGS_REPLY = (byte)0x80;
	// end of header flag
	// header flag2
	public static final short SMB_FLAGS2_LONG_NAMES = 0x0001;
	public static final short SMB_FLAGS2_EAS = 0x0002;
	public static final short SMB_FLAGS2_SMB_SECURITY_SIGNATURE =0x0004;
	//added extension
	public static final short SMB_FLAGS2_COMPRESSED = 0x0008;
	public static final short SMB_FLAGS2_SMB_SECURITY_SIGNATURE_REQUIRED = 0x0010;
	public static final short SMB_FLAGS2_REPARSE_PATH = 0x0400;
	public static final short SMB_FLAGS2_EXTENDED_SECURITY = 0x0800;
	//added extension
	public static final short SMB_FLAGS2_IS_LONG_NAME = 0x0040;
	public static final short SMB_FLAGS2_DFS = 0x1000;
	public static final short SMB_FLAGS2_PAGING_IO = 0x2000;
	public static final short SMB_FLAGS2_NT_STATUS = 0x4000;
	public static final short SMB_FLAGS2_UNICODE = (short) 0x8000;
	// end of header flag2
	// 0x01 
	// 0x02
	// 0x03
	// 0x04
	String protocol;
	SmbCommand command; // in ComCodes
	SmbStatus status = new SmbStatus();
	byte flags;
	short flags2;
	short pidHigh;
	byte []securityFeatures = new byte[8];
	short reserved = 0x00;
	short tid;
	short pidLow;
	short uid;
	short mid;
	// 23byte
	private SmbHeader(){
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(byte[] protocol) {
		this.protocol = new String(protocol , Charset.forName("utf-8"));
	}
	public SmbCommand getCommand() {
		return command;
	}
	public void setCommand(SmbCommand command) {
		this.command = command;
	}
	public byte getFlags() {
		return flags;
	}
	public void setFlags(byte flags) {
		this.flags = flags;
	}
	public short getFlags2() {
		return flags2;
	}
	public void setFlags2(short s) {
		this.flags2 = s;
	}
	public short getPidHigh() {
		return pidHigh;
	}
	public void setPidHigh(short pidHigh) {
		this.pidHigh = pidHigh;
	}
	public byte[] getSecurityFeatures() {
		return securityFeatures;
	}
	public void setSecurityFeatures(byte[] securityFeatures) {
		this.securityFeatures = securityFeatures;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
	}
	public short getTid() {
		return tid;
	}
	public void setTid(short tid) {
		this.tid = tid;
	}
	public short getPidLow() {
		return pidLow;
	}
	public void setPidLow(short pidLow) {
		this.pidLow = pidLow;
	}
	public short getUid() {
		return uid;
	}
	public void setUid(short uid) {
		this.uid = uid;
	}
	public short getMid() {
		return mid;
	}
	public void setMid(short mid) {
		this.mid = mid;
	}
	
	public SmbStatus getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status.setErrorClass(ErrorClass.parse(status & 0xff000000>> 24 )) ;
		this.status.setReserved((byte)((status & 0x00ff0000)>>16));
		this.status.setErrorCode(ErrorCode.parse((status &0xff000000) >> 24, status & 0x0000ffff));
	}
	public boolean isFlagsLockAndReadOK(){
		if((this.flags & SMB_FLAGS_LOCK_AND_READ_OK)== SMB_FLAGS_LOCK_AND_READ_OK)
			return true;
		else
			return false;
	}
	public boolean isFlagsBufAvail(){
		if((this.flags & SMB_FLAGS_BUF_AVAIL)==SMB_FLAGS_BUF_AVAIL)
			return true;
		else
			return false;
			
	}
	public boolean isReserved(){
		if((this.flags & Reserved) == Reserved)
			return true;
		else
			return false;
	}
	public boolean isFlagsCaseInsensitive()
	{
		if((this.flags & SMB_FLAGS_CASE_INSENSITIVE) == SMB_FLAGS_CASE_INSENSITIVE)
			return true;
		else
			return false;
	}
	public boolean isFlagsCanonicalizedPaths()
	{
		if((this.flags & SMB_FLAGS_CANONICALIZED_PATHS) == SMB_FLAGS_CANONICALIZED_PATHS)
			return true;
		else
			return false;
	}
	
	public boolean isFlagsOplock()
	{
		if((this.flags & SMB_FLAGS_OPLOCK) == SMB_FLAGS_OPLOCK)
			return true;
		else
			return false;
	}
	public boolean isFlagsOpBatch()
	{
		if((this.flags & SMB_FLAGS_OPBATCH) == SMB_FLAGS_OPBATCH)
			return true;
		else
			return false;
	}
	public boolean isFlagsReply()
	{
		if((this.flags & SMB_FLAGS_REPLY) == SMB_FLAGS_REPLY)
			return true;
		else
			return false;
	}
	// end of header flag
	// header flag2
	public boolean isFlag2LongNames()
	{
		if((this.flags2 & SMB_FLAGS2_LONG_NAMES) == SMB_FLAGS2_LONG_NAMES)
			return true;
		else
			return false;
	}
	public boolean isFlag2EAS()
	{
		if((this.flags2 & SMB_FLAGS2_EAS) == SMB_FLAGS2_EAS)
			return true;
		else
			return false;
	}
	public boolean isFlag2SMBSecuritySignature()
	{
		if((this.flags2 & SMB_FLAGS2_SMB_SECURITY_SIGNATURE) == SMB_FLAGS2_SMB_SECURITY_SIGNATURE)
			return true;
		else
			return false;
	}
	public boolean isFlag2IsLongName()
	{
		if((this.flags2 & SMB_FLAGS2_IS_LONG_NAME) == SMB_FLAGS2_IS_LONG_NAME)
			return true;
		else
			return false;
	}
	public boolean isFlag2DFS()
	{
		if((this.flags2 & SMB_FLAGS2_DFS) == SMB_FLAGS2_DFS)
			return true;
		else
			return false;
	}
	public boolean isFlag2PagingIo()
	{
		if((this.flags2 & SMB_FLAGS2_PAGING_IO) == SMB_FLAGS2_PAGING_IO)
			return true;
		else
			return false;
	}
	public boolean isFlag2NtStatus()
	{
		if((this.flags2 & SMB_FLAGS2_NT_STATUS) == SMB_FLAGS2_NT_STATUS)
			return true;
		else
			return false;
	}
	public boolean isFlag2Unicode()
	{
		if((this.flags2 & SMB_FLAGS2_UNICODE) == SMB_FLAGS2_UNICODE)
			return true;
		else
			return false;
	}
	// end of header flag2
	public boolean isFlag2Compressed()
	{
		if((this.flags2 & SMB_FLAGS2_COMPRESSED) == SMB_FLAGS2_COMPRESSED)
			return true;
		else
			return false;
	}
	public boolean isFlag2SecuritySignatureRequired()
	{
		if((this.flags2 & SMB_FLAGS2_SMB_SECURITY_SIGNATURE_REQUIRED) == SMB_FLAGS2_SMB_SECURITY_SIGNATURE_REQUIRED)
			return true;
		else
			return false;
	}
	public boolean isFlag2ReparsePath()
	{
		if((this.flags2 & SMB_FLAGS2_REPARSE_PATH) == SMB_FLAGS2_REPARSE_PATH)
			return true;
		else
			return false;
	}
	public boolean isFlag2ExtendedSecurity()
	{
		if((this.flags2 & SMB_FLAGS2_EXTENDED_SECURITY) == SMB_FLAGS2_EXTENDED_SECURITY)
			return true;
		else
			return false;
	}
	
	
	public static SmbHeader parse(Buffer b){
		SmbHeader header = new SmbHeader();
		byte []buff = new byte[4];
		byte []sec = new byte[8];
		b.gets(buff);
		header.setProtocol(buff);
		header.setCommand(SmbCommand.parse(b.get() & 0xff));
		header.setStatus(ByteOrderConverter.swap(b.getInt()));
		header.setFlags(b.get());
		header.setFlags2(ByteOrderConverter.swap(b.getShort()));
		header.setPidHigh(ByteOrderConverter.swap(b.getShort()));
		b.gets(sec);
		header.setSecurityFeatures(sec);
		header.setReserved(ByteOrderConverter.swap(b.getShort()));
		header.setTid(ByteOrderConverter.swap(b.getShort()));
		header.setPidLow(ByteOrderConverter.swap(b.getShort()));
		header.setUid(ByteOrderConverter.swap(b.getShort()));
		header.setMid(ByteOrderConverter.swap(b.getShort()));
		return header; 
	}
	@Override
	public String toString() {
		
		return String.format("Smb Header: protocol = %s\n command=%s\n,status=%s\n" +
				"flags=0x%x\n flags2=0x%x\n pidHigh=%d\n securityFeatures=%s" +
				"reserved=%d\n tid=%d\n pidlow=%s\n uid=%s\n mid=%s\n"
				,this.protocol , this.command, this.status, 
				 this.flags,this.flags2 , this.pidHigh , this.securityFeatures,
				 this.reserved,this.tid, this.pidLow, this.uid , this.mid);	}
}
