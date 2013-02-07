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
package org.krakenapps.pcap;

import static org.junit.Assert.*;

import java.nio.BufferUnderflowException;
import java.nio.InvalidMarkException;

import org.junit.Test;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class ChainBufferTest {
	private Buffer initBuffer() {
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		return buffer;
	}

	private Buffer initBuffer2() {
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2, 3, 4 };
		byte[] testArray2 = new byte[] { 5, 6, 7, 8, 9 };
		byte[] testArray3 = new byte[] { 10, 11, 12 };

		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		return buffer;
	}

	private Buffer initBuffer3() {
		Buffer buffer = initBuffer();

		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);

		Buffer buffer3 = new ChainBuffer();
		byte[] testArray9 = new byte[] { 21, 22, 23 };
		byte[] testArray10 = new byte[] { 24, 25 };
		byte[] testArray11 = new byte[] { 26, 27, 28, 29 };
		byte[] testArray12 = new byte[] { 30, 31, 32, 33, 34, 35 };
		buffer3.addLast(testArray9);
		buffer3.addLast(testArray10);
		buffer3.addLast(testArray11);
		buffer3.addLast(testArray12);

		buffer.addLast(buffer2);
		buffer.addLast(buffer3);
		return buffer;
	}

	private Buffer initBuffer4() {
		Buffer buffer = initBuffer();

		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);

		Buffer buffer3 = new ChainBuffer();
		byte[] testArray9 = new byte[] { 21, 22, 23 };
		byte[] testArray10 = new byte[] { 24, 25 };
		byte[] testArray11 = new byte[] { 26, 27, 28, 29 };
		byte[] testArray12 = new byte[] { 30, 31, 32, 33, 34, 35 };
		buffer3.addLast(testArray9);
		buffer3.addLast(testArray10);
		buffer3.addLast(testArray11);
		buffer3.addLast(testArray12);

		buffer2.get();
		buffer2.get();
		buffer2.get();
		buffer.addLast(buffer2);

		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer.addLast(buffer3);
		return buffer;
	}

	@Test
	public void positionTest() {
		Buffer buffer = initBuffer4();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		buffer.mark();

		byte[] temp = new byte[20];
		buffer.gets(temp);
		assertEquals(22, buffer.position());
		buffer.reset();
		assertEquals(2, buffer.position());
	}

	@Test
	public void positionTest2() {
		Buffer buffer = initBuffer4();

		assertEquals(0, buffer.position());
		assertEquals(1, buffer.get());
		assertEquals(1, buffer.position());
		assertEquals(2, buffer.get());
		assertEquals(2, buffer.position());
		buffer.mark();
		buffer.reset();
		assertEquals(2, buffer.position());
	}

	@Test
	public void positionTest3() {
		Buffer buffer = initBuffer4();

		byte[] temp = new byte[13];
		buffer.gets(temp);
		assertEquals(13, buffer.position());
	}

	@Test
	public void positionIntTest() {
		Buffer buffer = initBuffer3();

		byte[] temp = new byte[13];
		buffer.gets(temp);
		buffer.position(19);
		assertEquals(106, buffer.get());
	}

	@Test
	public void positionIntTest2() {
		Buffer buffer = initBuffer4();

		byte[] temp = new byte[15];
		buffer.gets(temp);
		buffer.position(21);
		assertEquals(108, buffer.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void positionIntTest3() {
		Buffer buffer = initBuffer4();

		byte[] temp = new byte[15];
		buffer.gets(temp);
		buffer.position(-1);
		assertEquals(111, buffer.get());
	}

	@Test
	public void getTest() {
		/* Test: buffer.get(), buffer.reset(), buffer.discardReadBytes() */
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		buffer.rewind();
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		buffer.discardReadBytes();
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		assertEquals(7, buffer.get());
		buffer.rewind();
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		assertEquals(7, buffer.get());
	}

	@Test(expected = BufferUnderflowException.class)
	public void getBufferUnderflowTest() {
		/* Test: buffer.get(), catch BufferUnderflowException */
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		assertEquals(7, buffer.get());
		assertEquals(8, buffer.get());
		assertEquals(9, buffer.get());
		assertEquals(10, buffer.get());
		assertEquals(11, buffer.get());
		assertEquals(12, buffer.get());
		buffer.get();
	}

	@Test
	public void getShortIntTest() {
		/* Test: buffer.getShort(), buffer.getInt() */
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14, 15 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);

		assertEquals(0x01, buffer.get());
		assertEquals(0x0203, buffer.getShort());
		assertEquals(0x0405, buffer.getShort());
		assertEquals(0x06, buffer.get());
		assertEquals(0x0708090a, buffer.getInt());
	}

	@Test
	public void getStringTest() {
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 76, 79, 86, 69 };
		byte[] testArray2 = new byte[] { 83, 69, 82 };
		byte[] testArray3 = new byte[] { 89, 85, 78, 71 };

		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);

		String s1 = buffer.getString(4);
		String s2 = buffer.getString(2);
		String s3 = buffer.getString(5);

		assertEquals(new String("LOVE"), s1);
		assertEquals(new String("SE"), s2);
		assertEquals(new String("RYUNG"), s3);
	}

	@Test
	public void getsTest() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());

		byte[] testb = new byte[7];
		buffer.gets(testb, 0, 7);
		int expected = 4;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}
	}

	@Test(expected = BufferUnderflowException.class)
	public void getsTest2() {
		/* Test: buffer.gets(), catch BufferUnderflowException */
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());

		byte[] testb = new byte[10];
		buffer.gets(testb, 0, 10);
		int expected = 6;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}
	}

	@Test
	public void getsTest3() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		buffer.mark();
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());

		byte[] testb = new byte[5];
		buffer.gets(testb, 0, 5);
		int expected = 6;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}
		buffer.reset();
		testb = null;
		testb = new byte[8];
		buffer.gets(testb, 0, 8);
		expected = 3;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}
	}

	@Test
	public void getsTest4() {
		Buffer buffer = initBuffer2();

		buffer.get();
		buffer.get();
		buffer.get();
		byte[] testb = new byte[6];
		buffer.gets(testb, 0, 6);
		int expected = 4;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}
	}

	@Test
	public void getsTest5() {
		/* Test: buffer.gets(), buffer.addLast(byte[]) */
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2, 3, 4 };
		byte[] testArray2 = new byte[] { 5, 6, 7, 8, 9 };

		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.get();
		buffer.get();
		buffer.get();

		byte[] testb = new byte[6];
		buffer.gets(testb, 0, 6);
		int expected = 4;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}

		byte[] testArray3 = new byte[] { 10, 11, 12 };
		buffer.addLast(testArray3);
		assertEquals(10, buffer.get());
		assertEquals(11, buffer.get());
		assertEquals(12, buffer.get());
	}

	@Test
	public void getsTest6() {
		Buffer buffer = initBuffer2();

		buffer.get();
		buffer.get();
		buffer.get();

		byte[] testb = new byte[4];
		buffer.gets(testb, 0, 4);
		int expected = 4;
		for (byte b : testb) {
			assertEquals(expected, b);
			expected++;
		}

		assertEquals(8, buffer.get());
		assertEquals(9, buffer.get());
		assertEquals(10, buffer.get());
	}

	@Test
	public void markRewindTest1() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		buffer.mark();
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());
		buffer.reset();
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
	}

	@Test
	public void markRewindTest2() {
		/* Test: buffer.mark(), buffer.rewind() */
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		buffer.mark();
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		buffer.reset();
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
	}

	@Test
	public void markNewTest() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());
		buffer.mark();
		buffer.position(0);
		assertEquals(1, buffer.get());
		buffer.reset();
		assertEquals(6, buffer.get());
	}

	@Test
	public void markNewTest2() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		buffer.mark();
		buffer.position(0);
		assertEquals(1, buffer.get());
		buffer.reset();
		assertEquals(5, buffer.get());
	}

	@Test
	public void markNewTest3() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		buffer.mark();
		buffer.reset();
		assertEquals(5, buffer.get());
	}

	@Test
	public void markNewTest4() {
		Buffer buffer = initBuffer2();

		buffer.mark();
		buffer.reset();
		assertEquals(1, buffer.get());
	}

	@Test(expected = BufferUnderflowException.class)
	public void markNewTest5() {
		Buffer buffer = initBuffer2();

		byte[] temp = new byte[12];
		buffer.gets(temp);
		buffer.mark();
		buffer.reset();
		assertEquals(12, buffer.get());
	}

	@Test
	public void markNewTest6() {
		Buffer buffer = initBuffer4();

		byte[] temp = new byte[12];
		buffer.gets(temp);
		buffer.mark();
		buffer.reset();
		assertEquals(13, buffer.get());
	}

	@Test
	public void markNewTest7() {
		Buffer buffer = initBuffer4();

		byte[] temp = new byte[18];
		buffer.gets(temp);
		buffer.mark();
		buffer.position(0);
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		buffer.reset();
		assertEquals(105, buffer.get());
	}

	@Test(expected = InvalidMarkException.class)
	public void markNewTest8() {
		Buffer buffer = initBuffer2();

		byte[] temp = new byte[4];
		buffer.gets(temp);
		buffer.position(0);
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		buffer.reset();
	}

	@Test
	public void markNewTest9() {
		Buffer buffer = initBuffer2();

		byte[] temp = new byte[4];
		buffer.gets(temp);
		buffer.position(0);
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		buffer.mark();
		assertEquals(3, buffer.get());
		buffer.reset();
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());
		buffer.mark();
		assertEquals(6, buffer.get());
		buffer.reset();
		assertEquals(6, buffer.get());
	}

	@Test
	public void clearTest() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());

		buffer.mark();
		assertEquals(4, buffer.get());
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		buffer.clear();

		assertEquals(1, buffer.get());
	}

	@Test
	public void clearTest2() {
		Buffer buffer = initBuffer2();

		byte[] temp = new byte[12];
		buffer.gets(temp);
		buffer.clear();

		assertEquals(1, buffer.get());
	}

	@Test
	public void discardTest() {
		Buffer buffer = initBuffer2();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		buffer.rewind();
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
		buffer.discardReadBytes();
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		assertEquals(7, buffer.get());
		buffer.rewind();
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		assertEquals(7, buffer.get());
	}

	@Test
	public void bytesBeforeTest() {
		Buffer buffer = initBuffer();

		byte[] target = new byte[] { 8, 9, 10, 11, 12 };
		buffer.get();
		buffer.get();

		int length = buffer.bytesBefore(target);
		byte[] test = new byte[length];
		if (length > 0) {
			buffer.gets(test, 0, length);
			int expected = 3;
			for (byte b : test) {
				assertEquals(expected, b);
				expected++;
			}
		}
	}

	@Test
	public void addLastTest() {
		Buffer buffer = initBuffer3();
		byte[] test = new byte[14];
		buffer.gets(test, 0, 14);

		assertEquals(101, buffer.get());
		assertEquals(102, buffer.get());
		assertEquals(103, buffer.get());
		assertEquals(104, buffer.get());
		assertEquals(105, buffer.get());
		assertEquals(106, buffer.get());
		assertEquals(107, buffer.get());
		assertEquals(108, buffer.get());
		assertEquals(109, buffer.get());
		assertEquals(110, buffer.get());
		assertEquals(111, buffer.get());
		assertEquals(112, buffer.get());
		assertEquals(21, buffer.get());
		assertEquals(22, buffer.get());
		assertEquals(23, buffer.get());
		assertEquals(24, buffer.get());
		assertEquals(25, buffer.get());
		assertEquals(26, buffer.get());
		assertEquals(27, buffer.get());
		assertEquals(28, buffer.get());
		assertEquals(29, buffer.get());
		assertEquals(30, buffer.get());
		assertEquals(31, buffer.get());
		assertEquals(32, buffer.get());
		assertEquals(33, buffer.get());
		assertEquals(34, buffer.get());
		assertEquals(35, buffer.get());
	}

	@Test
	public void addLastTest2() {
		Buffer buffer = initBuffer3();

		byte[] test = new byte[9];
		buffer.gets(test, 0, 9);

		int expected = 1;
		for (byte b : test) {
			assertEquals(expected, b);
			expected++;
		}
	}

	@Test
	public void addLastTest3() {
		Buffer buffer = initBuffer3();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());

		buffer.mark();
		byte[] test = new byte[25];
		buffer.gets(test, 0, 25);

		assertEquals(5, test[0]);
		assertEquals(6, test[1]);
		assertEquals(7, test[2]);
		assertEquals(8, test[3]);
		assertEquals(9, test[4]);
		assertEquals(10, test[5]);
		assertEquals(11, test[6]);
		assertEquals(12, test[7]);
		assertEquals(13, test[8]);
		assertEquals(14, test[9]);
		assertEquals(101, test[10]);
		assertEquals(102, test[11]);
		assertEquals(103, test[12]);
		assertEquals(104, test[13]);
		assertEquals(105, test[14]);
		assertEquals(106, test[15]);
		assertEquals(107, test[16]);
		assertEquals(108, test[17]);
		assertEquals(109, test[18]);
		assertEquals(110, test[19]);
		assertEquals(111, test[20]);
		assertEquals(112, test[21]);
		assertEquals(21, test[22]);
		assertEquals(22, test[23]);
		assertEquals(23, test[24]);

		buffer.reset();
		assertEquals(5, buffer.get());
	}

	@Test
	public void addLastTest4() {
		Buffer buffer = initBuffer();

		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);

		Buffer buffer3 = new ChainBuffer();
		byte[] testArray9 = new byte[] { 21, 22, 23 };
		byte[] testArray10 = new byte[] { 24, 25 };
		byte[] testArray11 = new byte[] { 26, 27, 28, 29 };
		byte[] testArray12 = new byte[] { 30, 31, 32, 33, 34, 35 };
		buffer3.addLast(testArray9);
		buffer3.addLast(testArray10);
		buffer3.addLast(testArray11);
		buffer3.addLast(testArray12);

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);

		/* put buffer2 { 104, 105, ..., 112 } */
		buffer2.get();
		buffer2.get();
		buffer2.get();
		buffer.addLast(buffer2);

		/* put buffer3 { 24, 25, ..., 112 } */
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer.addLast(buffer3);

		buffer.addLast(buffer4);

		byte[] t = new byte[13];
		buffer.gets(t);

		assertEquals(14, buffer.get());
		assertEquals(101, buffer.get());
		assertEquals(102, buffer.get());
		assertEquals(103, buffer.get());
		assertEquals(104, buffer.get());
		assertEquals(105, buffer.get());
		assertEquals(106, buffer.get());
		assertEquals(107, buffer.get());
		assertEquals(108, buffer.get());
		assertEquals(109, buffer.get());
		assertEquals(110, buffer.get());
		assertEquals(111, buffer.get());
		assertEquals(112, buffer.get());
		assertEquals(21, buffer.get());
		assertEquals(22, buffer.get());
		assertEquals(23, buffer.get());
		assertEquals(24, buffer.get());
		assertEquals(25, buffer.get());
		assertEquals(26, buffer.get());
		assertEquals(27, buffer.get());
		assertEquals(28, buffer.get());
		assertEquals(29, buffer.get());
		assertEquals(30, buffer.get());
		assertEquals(31, buffer.get());
		assertEquals(32, buffer.get());
		assertEquals(33, buffer.get());
		assertEquals(34, buffer.get());
		assertEquals(35, buffer.get());
	}

	@Test
	public void addLastTest5() {
		Buffer buffer = initBuffer();

		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);

		Buffer buffer3 = new ChainBuffer();
		byte[] testArray9 = new byte[] { 21, 22, 23 };
		byte[] testArray10 = new byte[] { 24, 25 };
		byte[] testArray11 = new byte[] { 26, 27, 28, 29 };
		byte[] testArray12 = new byte[] { 30, 31, 32, 33, 34, 35 };
		buffer3.addLast(testArray9);
		buffer3.addLast(testArray10);
		buffer3.addLast(testArray11);
		buffer3.addLast(testArray12);

		ChainBuffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);

		buffer2.get();
		buffer2.get();
		buffer2.get();
		buffer.addLast(buffer2);

		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer3.get();
		buffer.addLast(buffer3);

		buffer.addLast(buffer4);

		byte[] b = new byte[13];
		buffer.gets(b);

		assertEquals(14, buffer.get());
		assertEquals(101, buffer.get());
		assertEquals(102, buffer.get());
		assertEquals(103, buffer.get());
		assertEquals(104, buffer.get());
		assertEquals(105, buffer.get());
		assertEquals(106, buffer.get());
		assertEquals(107, buffer.get());
		assertEquals(108, buffer.get());
		assertEquals(109, buffer.get());
		assertEquals(110, buffer.get());
		assertEquals(111, buffer.get());
		assertEquals(112, buffer.get());
		assertEquals(21, buffer.get());
		assertEquals(22, buffer.get());
		assertEquals(23, buffer.get());
		assertEquals(24, buffer.get());
	}

	/*
	 * addLastLenTest() ~ addLastLenTest15() refer to
	 * http://mindori.egloos.com/2637204
	 */
	@Test
	public void addLastLenTest() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 5);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 71;
		int i = 0;
		while (i < 5) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest2() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 6);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 71;
		int i = 0;
		while (i < 6) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest3() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 8);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 71;
		int i = 0;
		while (i < 8) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest4() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 9);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 71;
		int i = 0;
		while (i < 9) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest5() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 11);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 71;
		int i = 0;
		while (i < 11) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest6() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 19);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 71;
		int i = 0;
		while (i < 19) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest7() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer.addLast(buffer4, 20);

		byte[] skip = new byte[35];
		buffer.gets(skip);
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest8() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer4.get();
		buffer4.get();
		buffer.addLast(buffer4, 3);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 73;
		int i = 0;
		while (i < 3) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest9() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer4.get();
		buffer4.get();
		buffer.addLast(buffer4, 4);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 73;
		int i = 0;
		while (i < 4) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest10() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer4.get();
		buffer4.get();
		buffer.addLast(buffer4, 6);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 73;
		int i = 0;
		while (i < 6) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest11() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer4.get();
		buffer4.get();
		buffer.addLast(buffer4, 7);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 73;
		int i = 0;
		while (i < 7) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest12() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer4.get();
		buffer4.get();
		buffer.addLast(buffer4, 17);

		byte[] skip = new byte[41];
		buffer.gets(skip);

		int expected = 73;
		int i = 0;
		while (i < 17) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest13() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		buffer4.get();
		buffer4.get();
		buffer.addLast(buffer4, 18);

		byte[] skip = new byte[35];
		buffer.gets(skip);
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest14() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		byte[] skip = new byte[35];
		buffer.gets(skip);

		byte[] skip2 = new byte[13];
		buffer4.gets(skip2);
		buffer.addLast(buffer4, 3);

		int expected = 30;
		int i = 0;
		while (i < 3) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void addLastLenTest15() {
		Buffer buffer = initBuffer4();

		Buffer buffer4 = new ChainBuffer();
		byte[] testArray13 = new byte[] { 71, 72, 73, 74, 75, 76 };
		byte[] testArray14 = new byte[] { 77, 78, 79 };
		byte[] testArray15 = new byte[] { 80, 81 };
		byte[] testArray16 = new byte[] { 82, 83, 84, 85, 86, 87, 88, 89 };
		buffer4.addLast(testArray13);
		buffer4.addLast(testArray14);
		buffer4.addLast(testArray15);
		buffer4.addLast(testArray16);

		byte[] skip = new byte[35];
		buffer.gets(skip);

		byte[] skip2 = new byte[13];
		buffer4.gets(skip2);
		buffer.addLast(buffer4, 6);

		int expected = 30;
		int i = 0;
		while (i < 6) {
			assertEquals(expected, buffer.get());
			expected++;
			i++;
		}
		/* call buffer.get() throws BufferUnderflowException: OK */
	}

	@Test
	public void moveTest() {
		Buffer buffer = initBuffer();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		buffer.skip(8);
		assertEquals(11, buffer.get());
		assertEquals(12, buffer.get());
	}

	@Test
	public void moveTest2() {
		Buffer buffer = initBuffer();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		buffer.skip(8);
		assertEquals(12, buffer.get());
		assertEquals(13, buffer.get());
	}

	@Test
	public void moveTest3() {
		Buffer buffer = initBuffer();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(null, buffer.skip(13));
		assertEquals(3, buffer.get());
		assertEquals(4, buffer.get());
	}

	@Test
	public void moveTest4() {
		Buffer buffer = initBuffer();

		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
		buffer.skip(1);
		assertEquals(5, buffer.get());
		assertEquals(6, buffer.get());
		assertEquals(7, buffer.get());
		buffer.rewind();
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
	}

	@Test
	public void unsignedShortTest() {
		Buffer buffer = new ChainBuffer();
		buffer.addLast(new byte[] { (byte) 0xfb, 0x50 });

		int value = buffer.getUnsignedShort();
		assertEquals(64336, value);
	}
	
	@Test
	public void flipTest() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		buffer.get();
		buffer.get();
		buffer.get();
		buffer.flip();
		
		assertEquals(3, buffer.readableBytes());
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		assertEquals(3, buffer.get());
	}
	
	@Test
	public void flipTest2() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		buffer.get();
		buffer.get();
		buffer.flip();
		
		assertEquals(2, buffer.readableBytes());
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
	}
	
	@Test
	public void flipTest3() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		buffer.get();
		buffer.flip();
		
		assertEquals(1, buffer.readableBytes());
		assertEquals(1, buffer.get());
	}
	
	@Test
	public void flipTest4() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		buffer.flip();
		
		assertEquals(0, buffer.readableBytes());
	}
	
	@Test
	public void flipTest5() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		byte[] b = new byte[14];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(14, buffer.readableBytes());
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
	}
	
	@Test
	public void flipTest6() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		byte[] b = new byte[13];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(13, buffer.readableBytes());
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
	}
	
	@Test
	public void flipTest7() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);
		
		buffer.addLast(buffer2);
		byte[] b = new byte[17];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(17, buffer.readableBytes());
		assertEquals(1, buffer.get());
		assertEquals(2, buffer.get());
		buffer.skip(10);
		assertEquals(13, buffer.get());
		assertEquals(14, buffer.get());
		assertEquals(101, buffer.get());
	}
	
	@Test
	public void flipTest8() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);
		
		buffer.addLast(buffer2);
		byte[] b = new byte[26];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(26, buffer.readableBytes());
	}
	
	@Test
	public void flipTest9() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);
		
		buffer.addLast(buffer2);
		byte[] b = new byte[14];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(14, buffer.readableBytes());
		assertEquals(1, buffer.get());
	}
	
	@Test
	public void flipTest10() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);
		
		buffer.addLast(buffer2);
		byte[] b = new byte[23];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(23, buffer.readableBytes());
		assertEquals(1, buffer.get());
	}
	
	@Test
	public void flipTest11() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);
		
		buffer.addLast(buffer2);
		byte[] b = new byte[22];
		buffer.gets(b);
		buffer.flip();
		
		assertEquals(22, buffer.readableBytes());
		assertEquals(1, buffer.get());
	}
	
	@Test
	public void flipTest12() { 
		Buffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2 };
		byte[] testArray2 = new byte[] { 3, 4, 5 };
		byte[] testArray3 = new byte[] { 6, 7, 8, 9 };
		byte[] testArray4 = new byte[] { 10, 11 };
		byte[] testArray5 = new byte[] { 12, 13, 14 };
		buffer.addLast(testArray);
		buffer.addLast(testArray2);
		buffer.addLast(testArray3);
		buffer.addLast(testArray4);
		buffer.addLast(testArray5);
		
		Buffer buffer2 = new ChainBuffer();
		byte[] testArray6 = new byte[] { 101, 102, 103, 104 };
		byte[] testArray7 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] testArray8 = new byte[] { 110, 111, 112 };
		buffer2.addLast(testArray6);
		buffer2.addLast(testArray7);
		buffer2.addLast(testArray8);

		buffer.addLast(buffer2);
		
		buffer.get();
		buffer.get();
		buffer.get();
		buffer.get();
		
		buffer.discardReadBytes();
		buffer.skip(22);
		buffer.flip();
		
		assertEquals(22, buffer.readableBytes());
		assertEquals(5, buffer.get());
	}
}