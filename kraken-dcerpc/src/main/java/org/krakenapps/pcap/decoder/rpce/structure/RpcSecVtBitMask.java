package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RpcSecVtBitMask {

	private short command;
	private short length;
	private int bits;
	public void parse(Buffer b){
		command = ByteOrderConverter.swap(b.getShort());
		length = ByteOrderConverter.swap(b.getShort());
		bits = ByteOrderConverter.swap(b.getInt());
	}
	
	public short getCommand() {
		return command;
	}
	public void setCommand(short command) {
		this.command = command;
	}
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
	public int getBits() {
		return bits;
	}
	public void setBits(int bits) {
		this.bits = bits;
	}
}
