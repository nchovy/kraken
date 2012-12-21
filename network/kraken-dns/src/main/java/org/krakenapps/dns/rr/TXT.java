/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
