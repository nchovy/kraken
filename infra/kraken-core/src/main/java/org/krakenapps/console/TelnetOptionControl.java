/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.console;

public class TelnetOptionControl {

	private TelnetOptionMessageType messageType;
	private byte optionCode;

	public TelnetOptionControl(TelnetOptionMessageType messageType,
			byte optionCode) {
		this.messageType = messageType;
		this.optionCode = optionCode;
	}

	public TelnetOptionMessageType getMessageType() {
		return messageType;
	}

	public byte getTypeCode() {
		switch (this.messageType) {
		case WILL:
			return (byte) 251;
		case WONT:
			return (byte) 252;
		case DO:
			return (byte) 253;
		case DONT:
			return (byte) 254;
		}

		// not reachable.
		throw new RuntimeException("Invalid telnet option message type.");
	}

	public byte getOptionCode() {
		return optionCode;
	}
}
