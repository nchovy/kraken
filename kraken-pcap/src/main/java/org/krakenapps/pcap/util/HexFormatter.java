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
 * @author xeraph
 */
public class HexFormatter {
	private HexFormatter() {
	}

	public static String format(byte[] bytes, int offset, int length) {
		int col = 0;
		StringBuilder sb = new StringBuilder(length * 3);

		for (int i = offset; i < length; i++) {
			byte b = bytes[i];
			if (col == 8) {
				sb.append("\n");
				col = 0;
			}

			sb.append(String.format("%02X ", b));
			col++;
		}

		return sb.toString();
	}

	public static String format(byte[] bytes) {
		return format(bytes, 0, bytes.length);
	}
}
