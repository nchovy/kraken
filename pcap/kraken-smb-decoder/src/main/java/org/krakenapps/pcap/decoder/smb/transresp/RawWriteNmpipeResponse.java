package org.krakenapps.pcap.decoder.smb.transresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class RawWriteNmpipeResponse implements TransData{
	short bytesWritten;

	public short getBytesWritten() {
		return bytesWritten;
	}

	public void setBytesWritten(short bytesWritten) {
		this.bytesWritten = bytesWritten;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
