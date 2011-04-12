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
public class ByteOrderConverter {
	private ByteOrderConverter() {
	}

	public static int swap(int value) {
		int a = value;
		int b = (a >> 24) & 0xFF;
		int c = (a >> 8) & 0xFF00;
		int d = (a << 8) & 0xFF0000;
		int e = (a << 24) & 0xFF000000;
		return (b | c | d | e);
	}

	public static short swap(short value) {
		short a = value;
		short b = (short) ((a >> 8) & 0xFF);
		short c = (short) ((a << 8) & 0xFF00);
		return (short)(b | c);
	}
	
	public static long swap(long value){
		long v = 0;
		for (int i = 7; i >= 0; i--) {
			v |= ((value >> ((7 - i) * 8)) & 0xff);
			if(i ==0){
				break;
			}
			v <<= 8;
		}
		return v;
/*
		long convert=0;
		convert = ((value & 0xff00000000000000l) >> 56) |
  				  ((value & 0x00ff000000000000l) >> 40) |
  				  ((value & 0x0000ff0000000000l) >> 24) |
  				  ((value & 0x000000ff00000000l) >>  8) |
				  ((value & 0x00000000ff000000l) <<  8) |
				  ((value & 0x0000000000ff0000l) << 24) |
				  ((value & 0x000000000000ff00l) << 40) |
				  ((value & 0x00000000000000ffl) << 56);
		return convert;*/
	}
}
