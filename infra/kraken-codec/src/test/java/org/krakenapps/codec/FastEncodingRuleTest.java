/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.codec;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class FastEncodingRuleTest {
	private ByteBuffer fastEncode(Object o) {
		FastEncodingRule r = new FastEncodingRule();
		BinaryForm bf = r.preencode(o);
		ByteBuffer bb = ByteBuffer.allocate(bf.totalLength);
		r.encode(bb, bf);
		bb.flip();
		return bb;
	}

	@Test
	public void encodeDecodePrimitiveList() {
		int[] i = new int[] { 1, 2, 3 };
		long[] l = new long[] { 1, 2, 3 };
		short[] s = new short[] { 1, 2, 3 };
		double[] d = new double[] { 1.11, 2.22, 3.33 };
		float[] f = new float[] { 1.1f, 2.2f, 3.3f };
		boolean[] b = new boolean[] { true, false, true };
		byte[] by = new byte[] { 1, 2, 3 };

		// int array
		ByteBuffer bb = fastEncode(i);

		Object[] o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(i[index], o[index]);

		// long array
		bb = fastEncode(l);
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(l[index], o[index]);

		// short array
		bb = fastEncode(s);
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(s[index], o[index]);

		// double array
		bb = fastEncode(d);
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(d[index], o[index]);

		// float array
		bb = fastEncode(f);
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(f[index], o[index]);

		// boolean array
		bb = fastEncode(b);
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(b[index], o[index]);

		// byte array
		bb = fastEncode(by);
		byte[] barr = (byte[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(by[index], barr[index]);
	}

	@Test(expected = UnsupportedTypeException.class)
	public void encodeCharArrayTest() {
		char[] c = new char[] { 'a', 'b', 'c' };
		fastEncode(c);
	}

	@Test
	public void encodeDecodeList() {
		List<String> i = new ArrayList<String>();
		i.add("a");
		i.add("b");
		i.add("c");

		ByteBuffer bb = fastEncode(i);
		Object[] o = (Object[]) EncodingRule.decode(bb);
		assertEquals("a", o[0]);
		assertEquals("b", o[1]);
		assertEquals("c", o[2]);

		Object[] l = new Object[] { "String Test", (long) -14231231, (int) -200, (short) -10 };
		bb = fastEncode(l);

		Object[] decodedL = (Object[]) EncodingRule.decode(bb);
		assertEquals("String Test", decodedL[0]);
		assertEquals((long) -14231231, decodedL[1]);
		assertEquals((int) -200, decodedL[2]);
		assertEquals((short) -10, decodedL[3]);
	}

	@Test
	public void encodeDecodeNumberBoundary() {
		ByteBuffer bb = fastEncode(Short.MAX_VALUE);
		assertEquals(Short.MAX_VALUE, EncodingRule.decode(bb));

		bb = fastEncode(Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, EncodingRule.decode(bb));

		bb = fastEncode(Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, EncodingRule.decode(bb));

		bb = fastEncode(Short.MIN_VALUE);
		assertEquals(Short.MIN_VALUE, EncodingRule.decode(bb));

		bb = fastEncode(Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, EncodingRule.decode(bb));

		bb = fastEncode(Long.MIN_VALUE);
		assertEquals(Long.MIN_VALUE, EncodingRule.decode(bb));

	}

	@Test
	public void encodeDecodeNumber() {
		ByteBuffer bb = fastEncode(0x0);
		assertEquals(0x0, EncodingRule.decode(bb));

		bb = fastEncode(0x2121212121212121L);
		assertEquals(0x2121212121212121L, EncodingRule.decode(bb));

		bb = fastEncode(-0x2121212121212121L);
		assertEquals(-0x2121212121212121L, EncodingRule.decode(bb));

		bb = fastEncode(-0x21212);
		assertEquals(-0x21212, EncodingRule.decode(bb));

		bb = fastEncode((short) -0x212);
		assertEquals((short) -0x212, EncodingRule.decode(bb));
	}

	@Test
	public void encodeDecodeString() {
		// ordinary string test
		ByteBuffer bb = fastEncode("xeraph");
		assertEquals("xeraph", EncodingRule.decodeString(bb));

		// empty string test
		bb = fastEncode("");
		assertEquals("", EncodingRule.decodeString(bb));
	}

	@Test
	public void encodeDecodeBoolean() {
		ByteBuffer bb = fastEncode(true);
		assertEquals(true, EncodingRule.decode(bb));
	}

	@Test
	public void encodeDecodeArray() {
		Object[] array = new Object[] { 1234, "xeraph" };

		ByteBuffer bb = fastEncode(array);
		Object[] decodedArray = (Object[]) EncodingRule.decode(bb);
		assertEquals(2, decodedArray.length);
		assertEquals(1234, decodedArray[0]);
		assertEquals("xeraph", decodedArray[1]);
	}

	@Test
	public void encodeDecodeIp() throws UnknownHostException {
		// case ip4
		Inet4Address ip4 = (Inet4Address) Inet4Address.getByAddress(new byte[] { 10, 0, 0, 1 });
		ByteBuffer bb = fastEncode(ip4);

		assertEquals(ip4, EncodingRule.decodeIp4(bb));

		// case ip6
		Inet6Address ip6 = (Inet6Address) Inet6Address.getByAddress(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
				15, 16 });
		bb = fastEncode(ip6);

		assertEquals(ip6, EncodingRule.decodeIp6(bb));
	}

	@Test
	public void encodeDecodeMapIp() throws UnknownHostException {
		Map<String, Object> m = new HashMap<String, Object>();
		InetAddress local = Inet4Address.getByAddress(new byte[] { 127, 0, 0, 1 });
		m.put("ip", local);

		ByteBuffer bb = fastEncode(m);

		Map<String, Object> dm = EncodingRule.decodeMap(bb);
		assertEquals(local, dm.get("ip"));
	}

	@Test
	public void encodeDecodeMap() throws UnknownHostException {
		Calendar cal = Calendar.getInstance();
		cal.set(2009, 1, 21, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date since = cal.getTime();

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", "nchovy");
		m.put("since", since);
		m.put("employee", 8);
		m.put("capital", 30000000);
		m.put("servers", new Object[] { "soul", "navi" });
		m.put("null", null);

		ByteBuffer bb = fastEncode(m);
		Map<String, Object> dm = EncodingRule.decodeMap(bb);
		assertEquals(6, m.size());
		assertEquals("nchovy", dm.get("name"));
		assertEquals(since, dm.get("since"));
		assertEquals(8, dm.get("employee"));
		assertEquals(30000000, dm.get("capital"));
		assertNull(dm.get("null"));

		Object[] decodedServers = (Object[]) dm.get("servers");
		assertEquals(2, decodedServers.length);
		assertEquals("soul", decodedServers[0]);
		assertEquals("navi", decodedServers[1]);
	}

	@Test
	public void encodeDecodeBlob() {
		byte[] blob = new byte[] { 0x42, (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
		ByteBuffer bb = fastEncode(blob);

		byte[] decodedBlob = EncodingRule.decodeBlob(bb);

		assertEquals(blob.length, decodedBlob.length);
		for (int i = 0; i < blob.length; i++) {
			assertEquals(blob[i], decodedBlob[i]);
		}
	}

}
