package org.krakenapps.pcap.decoder.smb.structure;

public class SmbDialect {

	byte bufferFormat;
	String dialectString = null;
	public byte getBufferFormat() {
		return bufferFormat;
	}
	public void setBufferFormat(byte bufferFormat) {
		this.bufferFormat = bufferFormat;
	}
	public String getDialectString() {
		return dialectString;
	}
	public String getString()
	{
		return dialectString;
	}
	public void setDialectString(String dialectString) {
		this.dialectString = dialectString;
	}
	@Override
	public String toString(){
		return String.format("Structure : SmbDialect\n"+
				"bufferFormat = 0x%s , dialectString = %s\n",
				Integer.toHexString(this.bufferFormat),this.dialectString);
		}
}
