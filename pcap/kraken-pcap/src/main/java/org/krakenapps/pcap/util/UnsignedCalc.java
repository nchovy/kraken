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
public class UnsignedCalc {
	private UnsignedCalc() {
	}

	public static int unsignedConvert(byte byteVal) {
		return (int) byteVal & 0x0000ffff;
	}

	public static int unsignedConvert(short shortVal) {
		return (int) shortVal & 0x0000ffff;
	}

	public static long unsignedConvert(int intVal) {
		return (long) intVal & 0x00000000ffffffff;
	}

	public static int unsignedAdd(short s1, short s2) {
		int intVal1 = (int) s1 & 0x0000ffff;
		int intVal2 = (int) s2 & 0x0000ffff;

		return (intVal1 + intVal2);
	}

	public static int unsignedAdd(int intVal1, short shortVal) {
		int intVal2 = (int) shortVal & 0x0000ffff;

		return (intVal1 + intVal2);
	}

	public static int unsignedAdd(int intVal1, int intVal2) {
		return (intVal1 + intVal2);
	}
}
