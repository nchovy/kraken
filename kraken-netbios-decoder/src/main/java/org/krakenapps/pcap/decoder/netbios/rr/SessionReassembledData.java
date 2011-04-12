package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionReassembledData implements NetBiosSessionData{

	private Buffer buffer ;
	private SessionReassembledData() {
	
	}
	private SessionReassembledData(Buffer buffer){
		
	}
	public static SessionReassembledData parse(Buffer buffer)
	{
		return new SessionReassembledData(buffer);
	}
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return buffer;
	}
	public void setBuffer(Buffer buffer) {
		this.buffer = buffer;
	}
}
