package org.krakenapps.pcap.decoder.smb.structure;

import org.krakenapps.pcap.decoder.smb.rr.ErrorClass;
import org.krakenapps.pcap.decoder.smb.rr.ErrorCode;

public class SmbStatus {
	ErrorClass errorClass;
	byte reserved;
	ErrorCode errorCode;
	public ErrorClass getErrorClass() {
		return errorClass;
	}
	public void setErrorClass(ErrorClass errorClass) {
		this.errorClass = errorClass;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	@Override
	public String toString(){
		/*return String.format("Structure : SmbStatus\n" +
				"errorClass = %s\n" +
				"reserved = 0x%s\n" +
				"errorCode = %s\n",
				this.errorClass,
				Integer.toHexString(this.reserved),
				this,errorCode);*/
		return "this is Status\n";
	}
}
