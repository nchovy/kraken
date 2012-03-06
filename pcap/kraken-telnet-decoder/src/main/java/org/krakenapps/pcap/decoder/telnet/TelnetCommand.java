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

public enum TelnetCommand {
	Unknown((byte) -1), Will((byte) 251), WONT((byte) 252), DO((byte) 253), DONT((byte) 254), SE((byte) 240), Nop(
			(byte) 241), DataMark((byte) 242), Break((byte) 243), InterruptProcess((byte) 244), AbortOutput((byte) 245), AreYouThere(
			(byte) 246), EraseCharacter((byte) 247), EraseLine((byte) 248), GoAhead((byte) 249), SB((byte) 250);

	private TelnetCommand(byte command) {
		this.command = command;
	}

	private byte command;

	public boolean hasOption() {
		if (command == (byte) 250 || command == (byte) 251 || command == (byte) 252 || command == (byte) 253
				|| command == (byte) 254)
			return true;
		return false;
	}

	public static TelnetCommand parse(byte command) {
		switch (command) {
		case (byte) 240:
			return SE;
		case (byte) 241:
			return Nop;
		case (byte) 242:
			return DataMark;
		case (byte) 243:
			return Break;
		case (byte) 244:
			return InterruptProcess;
		case (byte) 245:
			return AbortOutput;
		case (byte) 246:
			return AreYouThere;
		case (byte) 247:
			return EraseCharacter;
		case (byte) 248:
			return EraseLine;
		case (byte) 249:
			return GoAhead;
		case (byte) 250:
			return SB;
		case (byte) 251:
			return Will;
		case (byte) 252:
			return WONT;
		case (byte) 253:
			return DO;
		case (byte) 254:
			return DONT;
		default:
			return Unknown;
		}
	}
}
