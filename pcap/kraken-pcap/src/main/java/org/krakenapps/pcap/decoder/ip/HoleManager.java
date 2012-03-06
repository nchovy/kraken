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
package org.krakenapps.pcap.decoder.ip;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.util.Buffer;

/**
 * @author mindori
 */
public class HoleManager {
	private int first = 0;
	private int goal = -1;
	
	private Map<Integer, Byte> list;
	private ByteBuffer reassembled;
	
	public HoleManager() {
		list = new HashMap<Integer, Byte>();
		reassembled = ByteBuffer.allocate(65515);
	}
	
	public int getFirst() {
		return first;
	}
	
	public int getGoal() {
		return goal;
	}
	
	public void setGoal(int goal) {
		/* fragment's MF == 0 */
		this.goal = goal;
	}

	public ByteBuffer getReassembled() {
		return reassembled;
	}
	
	public void put(Buffer data, int offset, int length) {
		byte[] b = new byte[length];
		data.gets(b, 0, length);
		
		for(int i = 0; i < length; i++) { 
			list.put(i + offset, b[i]);
		}
	}	
	
	public boolean isFlush(int offset, int length) { 
		if(offset == first) {
			return true;
		}
		else if(offset < first && (offset + length) > first) { 
			return true;
		}
		return false;
	}
	
	public void flush(Buffer data) {
		/* start: hole.first, end: until find empty hole */
		Byte b;
		int length = 0;
		int offset = first;
		
		while((b = list.get(offset)) != null) { 
			reassembled.put(b);
			offset++;
			length++;
		}
		first += length;
	}
	
	public boolean isReassemble() { 
		if(first == goal && goal != -1)
			return true;
		return false;
	}
}