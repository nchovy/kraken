package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.decoder.netbios.NetBiosDatagramType;
import org.krakenapps.pcap.util.Buffer;

public class ErrorRRHeader extends DatagramHeader {

	private ErrorRRHeader() {

	}

	public static ErrorRRHeader parse(Buffer b) {
		ErrorRRHeader header = new ErrorRRHeader();
		byte[] buff = new byte[4];
		b.rewind();
		header.setMsgType(NetBiosDatagramType.parse(b.get() & 0xff));
		header.setFlags(b.get());
		header.setDgmID(b.getShort());
		b.gets(buff);
		try {
			header.setAddresses(InetAddress.getByAddress(buff));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		header.setPort(b.getShort());
		return header;
	}

	@Override
	public String toString() {
		return String.format("Datagram Header ErrorRRHeader"+
				"type=%s, flag=0x%s, dgmid=0x%s\n",
				this.msgType, Integer.toHexString(this.flags), Integer.toHexString(this.dgmID));
	}

}
