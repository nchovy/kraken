package org.krakenapps.dns.rr;

import java.nio.ByteBuffer;

import org.krakenapps.dns.DnsResourceRecord;

public class TXT extends DnsResourceRecord {
	private String text;

	public static DnsResourceRecord decode(ByteBuffer bb, boolean isQuestion) {
		TXT txt = new TXT();

		if (isQuestion)
			return txt;

		int len = bb.getShort() & 0xffff;
		byte[] b = new byte[len];
		bb.get(b);
		txt.text = new String(b);
		return txt;
	}

	@Override
	public void encode(ByteBuffer bb) {
		byte[] b = text.getBytes();
		bb.putShort((short) b.length);
		bb.put(b);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "TXT " + text;
	}
}
