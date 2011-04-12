package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum BufferFormat {

	DataBuffer(0x01),
	DialectString(0x02),
	PathName(0x03),
	SmbString(0x04),
	VariableBlcok(0x05);
	BufferFormat(int code){
		this.code = code;
	}
	private int code;
	public int getFormat(){
		return this.code;
	}
	private static Map<Integer , BufferFormat> FormatMap = new HashMap<Integer, BufferFormat>();
	static{
		for(BufferFormat format : BufferFormat.values())
		{
			FormatMap.put(format.getFormat(), format);
		}
	}
	public BufferFormat parse(int format){
		return FormatMap.get(format);
	}
}
