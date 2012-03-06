package org.krakenapps.pcap.decoder.smb.rr;

public enum DateFlags {

	YEAR(0xFE00), // start 1980
	MONTH(0x01E0), // 1 to 12
	DAY(0x001F); // 1 to 31
	DateFlags(int flag)
	{
		this.flag = flag;
	}
	private int flag; 
	static public DateFlags getYearFlag(){
		return YEAR;
	}
	static public DateFlags getMonthFlag(){
		return MONTH;
	}
	static public DateFlags getDatFlag(){
		return DAY;
	}
}
