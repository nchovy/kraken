package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class PortAny {

	private short length;
	private byte []portSpec; // size of length
	public void parse(Buffer b){
		length = ByteOrderConverter.swap(b.getShort());
		portSpec = new byte[length];
		b.gets(portSpec);
	}
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
	public byte[] getPortSpec() {
		return portSpec;
	}
	public void setPortSpec(byte[] portSpec) {
		this.portSpec = portSpec;
	}
}
