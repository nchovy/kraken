/*
 * Copyright 2010 NCHOVY
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

public class EncodingRuleTest {

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
		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOf(i));
		EncodingRule.encode(bb, i);
		bb.flip();

		Object[] o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(i[index], o[index]);

		// long array
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(l));
		EncodingRule.encode(bb, l);
		bb.flip();
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(l[index], o[index]);

		// short array
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(s));
		EncodingRule.encode(bb, s);
		bb.flip();
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(s[index], o[index]);

		// double array
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(d));
		EncodingRule.encode(bb, d);
		bb.flip();
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(d[index], o[index]);

		// float array
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(f));
		EncodingRule.encode(bb, f);
		bb.flip();
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(f[index], o[index]);

		// boolean array
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(b));
		EncodingRule.encode(bb, b);
		bb.flip();
		o = (Object[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(b[index], o[index]);

		// byte array
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(by));
		EncodingRule.encode(bb, by);
		bb.flip();
		byte[] barr = (byte[]) EncodingRule.decode(bb);

		for (int index = 0; index < o.length; index++)
			assertEquals(by[index], barr[index]);
	}

	@Test(expected = UnsupportedTypeException.class)
	public void lengthOfCharArrayTest() {
		char[] c = new char[] { 'a', 'b', 'c' };
		ByteBuffer.allocate(EncodingRule.lengthOf(c));
	}

	@Test(expected = UnsupportedTypeException.class)
	public void encodeCharArrayTest() {
		char[] c = new char[] { 'a', 'b', 'c' };
		ByteBuffer bb = ByteBuffer.allocate(10);
		EncodingRule.encode(bb, c);
	}

	@Test
	public void encodeDecodeList() {
		List<String> i = new ArrayList<String>();
		i.add("a");
		i.add("b");
		i.add("c");

		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOf(i));
		EncodingRule.encode(bb, i);
		bb.flip();

		Object[] o = (Object[]) EncodingRule.decode(bb);
		assertEquals("a", o[0]);
		assertEquals("b", o[1]);
		assertEquals("c", o[2]);

		Object[] l = new Object[] { "String Test", (long) -14231231, (int) -200, (short) -10 };
		bb = ByteBuffer.allocate(EncodingRule.lengthOf(l));
		EncodingRule.encode(bb, l);

		assertEquals(bb.position(), EncodingRule.lengthOf(l));
		bb.flip();
		Object[] decodedL = (Object[]) EncodingRule.decode(bb);
		assertEquals("String Test", decodedL[0]);
		assertEquals((long) -14231231, decodedL[1]);
		assertEquals((int) -200, decodedL[2]);
		assertEquals((short) -10, decodedL[3]);
	}

	@Test
	public void decodeNumber() {
		ByteBuffer bb = ByteBuffer.allocate(100);

		// no high bit case
		bb.put((byte) EncodingRule.INT32_TYPE);
		bb.put((byte) 0x7f);
		bb.flip();

		assertEquals(0x7f, EncodingRule.decodePlainInt(bb));

		// 2 byte length case
		// 30000 (10) = 1110101 00110000 (2)
		// (1)0000001 (1)1101010 (0)0110000
		bb.clear();
		bb.put((byte) EncodingRule.INT16_TYPE);
		bb.put((byte) 0x81);
		bb.put((byte) 0xEA);
		bb.put((byte) 0x30);
		bb.flip();

		assertEquals((short) 30000, EncodingRule.decodePlainShort(bb));

		// same, but dummy byte trailing test
		bb.clear();
		bb.put((byte) EncodingRule.INT32_TYPE);
		bb.put((byte) 0x81);
		bb.put((byte) 0xEA);
		bb.put((byte) 0x30);
		bb.put((byte) 0x30); // will not be read (cause of non higher bit)
		bb.flip();

		assertEquals(30000, EncodingRule.decodePlainInt(bb));
	}

	@Test
	public void encodeNumber() {
		ByteBuffer bb = ByteBuffer.allocate(100);

		// 1 byte boundary test
		EncodingRule.encode(bb, 0x3F);
		bb.flip();
		assertEquals(2, bb.remaining());
		assertEquals(0x3f, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, 0x3F + 1);
		bb.flip();
		assertEquals(3, bb.remaining());
		assertEquals(0x3f + 1, EncodingRule.decode(bb));

		// 2 byte boundary test
		bb.clear();
		EncodingRule.encode(bb, 0x1FFF);
		bb.flip();
		assertEquals(3, bb.remaining());
		assertEquals(0x1FFF, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, 0x1FFF + 1);
		bb.flip();
		assertEquals(4, bb.remaining());
		assertEquals(0x1FFF + 1, EncodingRule.decode(bb));

		// negative number test
		bb.clear();
		EncodingRule.encode(bb, -10);
		bb.flip();
		assertEquals(2, bb.remaining());
		assertEquals(-10, EncodingRule.decode(bb));
	}

	@Test
	public void encodeDecodeZigzagNumber() {
		ByteBuffer bb = ByteBuffer.allocate(100);

		bb.clear();
		EncodingRule.encode(bb, (int) 0);
		bb.flip();
		bb.get();
		assertEquals(0, EncodingRule.decodeRawNumber(bb));

		bb.clear();
		EncodingRule.encode(bb, (int) -1);
		bb.flip();
		bb.get();
		assertEquals(1, EncodingRule.decodeRawNumber(bb));

		bb.clear();
		EncodingRule.encode(bb, (int) 1);
		bb.flip();
		bb.get();
		assertEquals(2, EncodingRule.decodeRawNumber(bb));

		bb.clear();
		EncodingRule.encode(bb, (int) -2);
		bb.flip();
		bb.get();
		assertEquals(3, EncodingRule.decodeRawNumber(bb));

		bb.clear();
		EncodingRule.encode(bb, Integer.MAX_VALUE);
		bb.flip();
		bb.get();
		assertEquals(0xFFFFFFFFL - 1, EncodingRule.decodeRawNumber(bb));

		bb.clear();
		EncodingRule.encode(bb, (int) Integer.MIN_VALUE);
		bb.flip();
		bb.get();
		assertEquals(0xFFFFFFFFL, EncodingRule.decodeRawNumber(bb));
	}

	@Test
	public void encodeDecodeNumberBoundary() {
		ByteBuffer bb = ByteBuffer.allocate(100);

		bb.clear();
		EncodingRule.encode(bb, Short.MAX_VALUE);
		bb.flip();
		assertEquals(Short.MAX_VALUE, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, Integer.MAX_VALUE);
		bb.flip();
		assertEquals(Integer.MAX_VALUE, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, Long.MAX_VALUE);
		bb.flip();
		assertEquals(Long.MAX_VALUE, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, Short.MIN_VALUE);
		bb.flip();
		assertEquals(Short.MIN_VALUE, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, Integer.MIN_VALUE);
		bb.flip();
		assertEquals(Integer.MIN_VALUE, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, Long.MIN_VALUE);
		bb.flip();
		assertEquals(Long.MIN_VALUE, EncodingRule.decode(bb));

	}

	@Test
	public void encodeDecodeNumber() {
		ByteBuffer bb = ByteBuffer.allocate(100);
		EncodingRule.encode(bb, 0x0);
		bb.flip();
		assertEquals(2, bb.remaining());
		assertEquals(0x0, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, 0x2121212121212121L);
		bb.flip();
		assertEquals(10, bb.remaining());
		assertEquals(0x2121212121212121L, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, -0x2121212121212121L);
		bb.flip();
		assertEquals(10, bb.remaining());
		assertEquals(-0x2121212121212121L, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, -0x21212);
		bb.flip();
		assertEquals(4, bb.remaining());
		assertEquals(-0x21212, EncodingRule.decode(bb));

		bb.clear();
		EncodingRule.encode(bb, (short) -0x212);
		bb.flip();
		assertEquals(3, bb.remaining());
		assertEquals((short) -0x212, EncodingRule.decode(bb));
	}

	@Test
	public void encodeDecodeString() {
		// ordinary string test
		ByteBuffer bb = ByteBuffer.allocate(100);
		EncodingRule.encodeString(bb, "xeraph");
		bb.flip();
		assertEquals("xeraph", EncodingRule.decodeString(bb));

		// empty string test
		bb.clear();
		EncodingRule.encode(bb, "");
		bb.flip();
		assertEquals("", EncodingRule.decodeString(bb));
	}

	@Test
	public void encodeDecodeBoolean() {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.clear();
		EncodingRule.encodeBoolean(bb, true);
		bb.flip();

		assertEquals(true, EncodingRule.decode(bb));
	}

	@Test
	public void encodeDecodeStringInt() {
		// trailing other data
		ByteBuffer bb = ByteBuffer.allocate(100);
		bb.clear();
		EncodingRule.encodeString(bb, "xeraph");
		EncodingRule.encodeInt(bb, 100);
		bb.flip();

		assertEquals("xeraph", EncodingRule.decodeString(bb));
		assertEquals(100, EncodingRule.decodeInt(bb));

	}

	@Test
	public void encodeDecodeArray() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		Object[] array = new Object[] { 1234, "xeraph" };

		EncodingRule.encodeArray(bb, array);
		bb.flip();

		Object[] decodedArray = (Object[]) EncodingRule.decode(bb);
		assertEquals(2, decodedArray.length);
		assertEquals(1234, decodedArray[0]);
		assertEquals("xeraph", decodedArray[1]);
	}

	@Test
	public void encodeDecodeIp() throws UnknownHostException {
		// case ip4
		Inet4Address ip4 = (Inet4Address) Inet4Address.getByAddress(new byte[] { 10, 0, 0, 1 });
		ByteBuffer bb = ByteBuffer.allocate(128);
		EncodingRule.encodeIp4(bb, ip4);
		bb.flip();

		assertEquals(ip4, EncodingRule.decodeIp4(bb));

		// case ip6
		bb.clear();
		Inet6Address ip6 = (Inet6Address) Inet6Address.getByAddress(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
				15, 16 });
		EncodingRule.encodeIp6(bb, ip6);
		bb.flip();

		assertEquals(ip6, EncodingRule.decodeIp6(bb));
	}

	@Test
	public void encodeDecodeMapIp() throws UnknownHostException {
		Map<String, Object> m = new HashMap<String, Object>();
		InetAddress local = Inet4Address.getByAddress(new byte[] { 127, 0, 0, 1 });
		m.put("ip", local);

		int length = EncodingRule.lengthOf(m);
		ByteBuffer bb = ByteBuffer.allocate(length);

		EncodingRule.encode(bb, m);
		bb.flip();

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

		int length = EncodingRule.lengthOf(m);
		ByteBuffer bb = ByteBuffer.allocate(length);

		EncodingRule.encode(bb, m);
		bb.flip();

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
		ByteBuffer bb = ByteBuffer.allocate(100);
		EncodingRule.encode(bb, blob);
		bb.flip();

		byte[] decodedBlob = EncodingRule.decodeBlob(bb);

		assertEquals(blob.length, decodedBlob.length);
		for (int i = 0; i < blob.length; i++) {
			assertEquals(blob[i], decodedBlob[i]);
		}
	}

	@Test
	public void lengthTest() {

		ByteBuffer bb = ByteBuffer.allocate(100);
		bb.mark();
		// int
		int intSamples[] = { 1, 10, 200, 2000, 1000000, -1, -2000 };
		for (int value : intSamples) {
			EncodingRule.encodeInt(bb, value);
			assertEquals(EncodingRule.lengthOfInt(value), bb.position());
			bb.reset();
		}

		// null
		EncodingRule.encodeNull(bb);
		assertEquals(EncodingRule.lengthOfNull(), bb.position());
		bb.reset();

		// short
		short shortSamples[] = { 1, 10, 200, 2000, -1, -2000 };
		for (short value : shortSamples) {
			EncodingRule.encodeShort(bb, value);
			assertEquals(EncodingRule.lengthOfShort(value), bb.position());
			bb.reset();
		}

		// string
		String stringSamples[] = { "sample", "chia", "pixar up", "asdfjkl;awefjkl;",
				"This method transfers the bytes remaining in the given source" };
		for (String value : stringSamples) {
			EncodingRule.encodeString(bb, value);
			assertEquals(EncodingRule.lengthOfString(value), bb.position());
			bb.reset();
		}

		// date
		Date dateSample = new Date();
		EncodingRule.encodeDate(bb, dateSample);
		assertEquals(EncodingRule.lengthOfDate(), bb.position());
		bb.reset();

		// address
		Inet4Address in4 = null;
		try {
			in4 = (Inet4Address) InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EncodingRule.encodeIp4(bb, in4);
		assertEquals(EncodingRule.lengthOfIp4(in4), bb.position());
		bb.reset();

		// map
		Map<String, Object> mapSample = new HashMap<String, Object>();
		mapSample.put("address", in4);
		mapSample.put("blabla", "aerosmith");
		mapSample.put("date", new Date());
		EncodingRule.encodeMap(bb, mapSample);
		assertEquals(EncodingRule.lengthOfMap(mapSample), bb.position());
		bb.reset();

		// array
		Object[] arraySample = { "24", new Date(), in4, "bla" };
		EncodingRule.encodeArray(bb, arraySample);
		assertEquals(EncodingRule.lengthOfArray(arraySample), bb.position());
		bb.reset();

		// blob
		byte[] blobSample = { 1, 100, 110, -110, -5 };
		EncodingRule.encodeBlob(bb, blobSample);
		assertEquals(EncodingRule.lengthOfBlob(blobSample), bb.position());
		bb.reset();

	}
}
