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

import java.io.IOException;
import java.nio.BufferUnderflowException;

import org.junit.Test;
import org.krakenapps.pcap.util.BufferInputStream;
import org.krakenapps.pcap.util.ChainBuffer;

public class BufferInputStreamTest {
	@Test
	public void test1() { 
		ChainBuffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2, 3, 4 };
		byte[] testArray2 = new byte[] { 5, 6, 7, 8, 9 };
		byte[] testArray3 = new byte[] { 10, 11, 12 };
		
 		buffer.addLast(testArray);
 		buffer.addLast(testArray2);
 		buffer.addLast(testArray3);
 		
 		BufferInputStream bis = new BufferInputStream(buffer);
 		try {
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			
 			byte[] b = new byte[5];
 			System.out.println("r: " + bis.read(b));
 			for(byte bs: b)
 				System.out.println(bs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test2() { 
		ChainBuffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2, 3, 4 };
		byte[] testArray2 = new byte[] { 5, 6, 7, 8, 9 };
		byte[] testArray3 = new byte[] { 10, 11, 12 };
		
 		buffer.addLast(testArray);
 		buffer.addLast(testArray2);
 		buffer.addLast(testArray3);
 		
 		BufferInputStream bis = new BufferInputStream(buffer);
 		try {
 			System.out.println("----------------------------------------------");
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			bis.skip(3);
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
// 			System.out.println(bis.read());
// 			System.out.println(bis.read());
// 			System.out.println(bis.read());
// 			System.out.println(bis.read());
 			byte[] b = new byte[2];
 			System.out.println("r: " + bis.read(b));
 		} catch (IOException e) {
 			e.printStackTrace();
		} catch (BufferUnderflowException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void rewindTest() { 
		ChainBuffer buffer = new ChainBuffer();
		byte[] testArray = new byte[] { 1, 2, 3, 4 };
		byte[] testArray2 = new byte[] { 5, 6, 7, 8, 9 };
		byte[] testArray3 = new byte[] { 10, 11, 12 };
		
 		buffer.addLast(testArray);
 		buffer.addLast(testArray2);
 		buffer.addLast(testArray3);
 		
 		BufferInputStream bis = new BufferInputStream(buffer);
 		try {
 			System.out.println("----------------------------------------------");
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			bis.mark();
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			System.out.println(bis.read());
 			bis.reset();
 			System.out.println(bis.read());
 			System.out.println(bis.read());
// 			System.out.println(bis.read());
// 			System.out.println(bis.read());
// 			System.out.println(bis.read());
// 			System.out.println(bis.read());
 			byte[] b = new byte[2];
 			System.out.println("r: " + bis.read(b));
 		} catch (IOException e) {
 			e.printStackTrace();
		} catch (BufferUnderflowException e) {
			e.printStackTrace();
		}
	}
}
