package org.krakenapps.dns.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsResourceRecord;

/**
 * DNS Extensions to Support IP Version 6 {@link http
 * ://tools.ietf.org/html/rfc3596}
 * 
 * @author xeraph
 * 
 */
public class AAAA extends DnsResourceRecord {
	private InetAddress address;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		AAAA aaaa = new AAAA();
		if (isQuestion)
			return aaaa;

		int rdlen = bb.getShort();
		if (rdlen != 16)
			throw new IllegalStateException("type AAAA record's rdlen (" + rdlen + ") should be 16 (128bit)");

		byte[] b = new byte[16];
		bb.get(b);
		try {
			aaaa.address = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
		}

		return aaaa;
	}

	@Override
	public void encode(ByteBuffer bb) {
		bb.putShort((short) 16);
		bb.put(address.getAddress());
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	@Override
	public String toString() {
		if (address == null)
			return "AAAA " + name;
		else
			return "AAAA " + address.getHostAddress();
	}
}
