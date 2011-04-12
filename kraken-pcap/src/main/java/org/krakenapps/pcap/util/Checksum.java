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
package org.krakenapps.pcap.util;

/**
 * @author mindori
 */
public class Checksum {
	private Checksum() {
	}

	public static int sum(short[] words) {
		int[] complements = new int[words.length];
		for (int i = 0; i < complements.length; i++)
			complements[i] = ~words[i] & 0xffff;

		int sum = 0;
		for (int i = 0; i < complements.length; i++)
			sum = UnsignedCalc.unsignedAdd(sum, complements[i]);

		int msb = (sum >> 16) & 0xffff;
		return (sum & 0xffff) + msb;
	}
}
