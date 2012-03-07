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

public enum TelnetOption {
	Unknown, TransmitBinary, Echo, SuppressGoAhead, Status, TimingMark, Rcte, Logout, ByteMacro, DataEntryTerminal, Supdup, SupdupOutput, SendLocation, TerminalType, EndOfRecord, TacacsUserIdentification, OutputMarking, Ttyloc, Regime, X3Pad, NegotiateAboutWindowSize, TerminalSpeed, ToggleFlowControl, Linemode, XDisplayLocation, Authentication, NewEnviron, ExtendedOptionsList;

	public static TelnetOption parse(byte option) {
		switch (option) {
		case (byte) 0:
			return TransmitBinary;
		case (byte) 1:
			return Echo;
		case (byte) 3:
			return SuppressGoAhead;
		case (byte) 5:
			return Status;
		case (byte) 6:
			return TimingMark;
		case (byte) 7:
			return Rcte;
		case (byte) 18:
			return Logout;
		case (byte) 19:
			return ByteMacro;
		case (byte) 20:
			return DataEntryTerminal;
		case (byte) 21:
			return Supdup;
		case (byte) 22:
			return SupdupOutput;
		case (byte) 23:
			return SendLocation;
		case (byte) 24:
			return TerminalType;
		case (byte) 25:
			return EndOfRecord;
		case (byte) 26:
			return TacacsUserIdentification;
		case (byte) 27:
			return OutputMarking;
		case (byte) 28:
			return Ttyloc;
		case (byte) 29:
			return Regime;
		case (byte) 30:
			return X3Pad;
		case (byte) 31:
			return NegotiateAboutWindowSize;
		case (byte) 32:
			return TerminalSpeed;
		case (byte) 33:
			return ToggleFlowControl;
		case (byte) 34:
			return Linemode;
		case (byte) 35:
			return XDisplayLocation;
		case (byte) 37:
			return Authentication;
		case (byte) 39:
			return NewEnviron;
		case (byte) 255:
			return ExtendedOptionsList;
		default:
			return Unknown;
		}
	}
}
