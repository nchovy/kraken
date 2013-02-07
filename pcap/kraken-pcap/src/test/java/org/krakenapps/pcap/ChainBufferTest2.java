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

import org.junit.Test;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class ChainBufferTest2 {
	@Test
	public void bufferAddTest() { 
		Buffer b = new ChainBuffer();
		Buffer b1 = new ChainBuffer();
		
		setData(b, b1);
		/* beforehand fetch to 2 elements */
		b1.get();
		b1.get();
		b1.discardReadBytes();
		b.addLast(b1);
		printResult(b);
	}

	@Test
	public void bufferAddTest2() { 
		Buffer b = new ChainBuffer();
		Buffer b1 = new ChainBuffer();
		
		setData(b, b1);
		b.addLast(b1);
		printResult(b);
	}
	
	@Test
	public void bufferAddTest3() { 
		Buffer b = new ChainBuffer();
		Buffer b1 = new ChainBuffer();
		
		setData(b, b1);
		b1.get();
		b1.get();
		b1.get();
		b1.get();
		b1.discardReadBytes();
		b.addLast(b1);
		printResult(b);
	}
	
	@Test
	public void bufferAddTest4() { 
		Buffer b = new ChainBuffer();
		Buffer b1 = new ChainBuffer();
		
		setData(b, b1);
		byte[] temp = new byte[12];
		b1.gets(temp);
		/* case: Out of range then don't discard. */
		b1.discardReadBytes();
		b.addLast(b1);
		printResult(b);
	}
	
	@Test
	public void bufferAddTest5() { 
		Buffer b = new ChainBuffer();
		Buffer b1 = new ChainBuffer();
		
		setData(b, b1);
		byte[] temp = new byte[11];
		b1.gets(temp);
		b1.discardReadBytes();
		b.addLast(b1);
		printResult(b);
	}
	
	private void setData(Buffer b1, Buffer b2) { 
		byte[] t = new byte[] { 101, 102, 103, 104 };
		byte[] t1 = new byte[] { 105, 106, 107, 108, 109 };
		byte[] t2 = new byte[] { 110, 111, 112 };
		
		byte[] r = new byte[] { 1, 2, 3, 4 };
		byte[] r1 = new byte[] { 5, 6, 7, 8, 9 };
		byte[] r2 = new byte[] { 10, 11, 12 };
		
		if(b1 != null) { 
			b1.addLast(t);
			b1.addLast(t1);
			b1.addLast(t2);
		}
		
		if(b2 != null) { 
			b2.addLast(r);
			b2.addLast(r1);
			b2.addLast(r2);
		}
	}
	
	private void printResult(Buffer b) {
		int capa = b.readableBytes();
		byte[] t = new byte[capa];
		b.gets(t);
		System.out.println();
		for(byte t1: t) { 
			System.out.printf("%d:", t1);
		}
	}
}