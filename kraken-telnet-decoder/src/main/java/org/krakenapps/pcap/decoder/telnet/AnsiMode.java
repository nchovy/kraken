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
package org.krakenapps.pcap.decoder.telnet;

public enum AnsiMode {
	Unknown(-1), Normal(0), Ansi(1), Csi(2), Value(3), Quot(4), Xterm(5), Sharp(6), Excl(7);

	private AnsiMode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static AnsiMode parse(int mode) {
		switch (mode) {
		case 1:
			return Ansi;
		case 2:
			return Csi;
		case 3:
			return Value;
		case 4:
			return Quot;
		case 5:
			return Xterm;
		case 6:
			return Sharp;
		case 7:
			return Excl;
		default:
			return Unknown;
		}
	}

	private int code;
}
