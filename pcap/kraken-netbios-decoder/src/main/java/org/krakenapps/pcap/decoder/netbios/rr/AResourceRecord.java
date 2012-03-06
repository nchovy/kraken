package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.util.Buffer;

public class AResourceRecord extends ResourceRecord {
	private int ttl;
	private short rdLength;
	private InetAddress[] addresses;

	public AResourceRecord(String name) {
		super(name);
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdLength() {
		return rdLength;
	}

	public void setRdLength(short rdLength) {
		this.rdLength = rdLength;
	}

	public InetAddress[] getRData() {
		return addresses;
	}

	public void setIpAddresses(InetAddress[] addresses) {
		this.addresses = addresses;
	}

	@Override
	public void parse(Buffer b, int type) {
		this.setType(Type.parse(type));
		this.setCls(b.getShort());
		this.setTtl(b.getInt());
		this.setRdLength(b.getShort());
		this.setIpAddresses(parseRdata(b));
	}

	private InetAddress[] parseRdata(Buffer b) {
		int length = this.getRdLength();
		int count = 0;
		count = length / 4;

		InetAddress[] addresses = new InetAddress[count];
		byte[] ipBuffer = new byte[4];
		for (int i = 0; i < count; i++) {
			b.gets(ipBuffer);
			try {
				addresses[i] = InetAddress.getByAddress(ipBuffer);
			} catch (UnknownHostException e) {
			}
		}
		return addresses;
	}

}
