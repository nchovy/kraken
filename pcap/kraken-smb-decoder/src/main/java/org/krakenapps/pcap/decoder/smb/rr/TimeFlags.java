package org.krakenapps.pcap.decoder.smb.rr;

public enum TimeFlags {
	HOUR(0xF800), // 0 to 23
	MINUTES(0x07E0),//
	SECONDS(0x001F);//
	static public TimeFlags getHourFlag() {
		return HOUR;
	}
	static public TimeFlags getMinuteFlag(){
		return MINUTES;
	}
	static public TimeFlags getSecondFlag(){
		return SECONDS;
	}
	TimeFlags(int code){
		this.code = code;
	}
	
	private int code;
}
