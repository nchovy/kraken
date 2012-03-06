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

import java.util.Arrays;
import java.util.Set;

import org.krakenapps.pcap.util.Buffer;

public class TelnetSession {
	final byte findCharNormal[] = { (byte) 0xff, 0x0d, 0x1b };
	final byte findCharOption[] = { (byte) 0xff };
	final int bufSize = 4096;
	final int argSize = 16;

	public TelnetSession() {
		setAnsiState(NORMAL);
		setState(NORMAL);
		buffer = new byte[bufSize];
		args = new int[argSize];
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
		switch (state) {
		case NORMAL:
			findChar = findCharNormal;
			if (ansiState != NORMAL) {
				findLen = 1;
				return;
			} else {
				findLen = bufSize;
			}
			break;
		case COMMAND:
			findChar = null;
			findLen = 1;
			break;
		case OPTION:
			findChar = null;
			findLen = 1;
			break;
		case SUBOPTION:
			findChar = findCharOption;
			findLen = bufSize;
			return;
		}
		bufferPos = 0;
	}

	public int getNextAnsiState() {
		byte ch = buffer[bufferPos - 1];

		switch (ansiState) {
		case ANSI:
			ansiMode = ANSI;
			if (ch == '[')
				return CSI;
			else if (ch == ']')
				return XTERM;
			else if (ch == '#')
				return SHARP;
			else if (ch == '!')
				return EXCL;
			dispatchAnsiControl(ch);
			break;
		case SHARP:
			ansiMode = SHARP;
			dispatchAnsiControl(ch);
			break;
		case EXCL:
			ansiMode = EXCL;
			dispatchAnsiControl(ch);
			break;
		case CSI:
			ansiMode = CSI;
			if (ch >= '0' && ch <= '9')
				return VALUE;
			else if (ch == '?' || ch == '>')
				return QUOT;
			dispatchAnsiControl(ch);
			break;
		case QUOT:
			ansiMode = QUOT;
			if (ch >= '0' && ch <= '9')
				return VALUE;
			dispatchAnsiControl(ch);
			break;
		case VALUE:
			if (ch >= '0' && ch <= '9')
				return VALUE;
			else if (ch == ';' || ch == '"') {
				if (bufferPos > 1)
					args[argPos++] = Integer.parseInt(new String(buffer, 0, bufferPos - 1));
				bufferPos = 0;
				return VALUE;
			} else {
				if (bufferPos > 1)
					args[argPos++] = Integer.parseInt(new String(buffer, 0, bufferPos - 1));
				dispatchAnsiControl(ch);
			}
			break;
		case XTERM:
			if (ch == ';') {
				bufferPos = 0;
				return XTERM;
			} else if (ch != 7)
				return XTERM;
			else if (bufferPos > 1) {
				bufferPos--;
				dispatchTitle();
			}
		}
		return NORMAL;
	}

	public void setAnsiState(int ansiState) {
		switch (ansiState) {
		case ANSI:
			argPos = 0;
			break;
		case XTERM:
		case VALUE:
			this.ansiState = ansiState;
			return;
		}
		bufferPos = 0;
		this.ansiState = ansiState;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getNextState(boolean hit, int index) {
		switch (state) {
		case NORMAL:
			if (hit) {
				if (index == 0)
					return COMMAND;
				else if (index == 2)
					setAnsiState(ANSI);
			} else if (ansiState != NORMAL)
				setAnsiState(getNextAnsiState());

			if (ansiState == NORMAL) {
				dispatchData();
			}
			break;
		case COMMAND:
			int next = NORMAL;
			command = buffer[0];
			switch (command) {
			case (byte) 250: // SB
				next = SUBOPTION;
				bufferPos = 0;
				break;
			case (byte) 251: // WILL
				next = OPTION;
				break;
			case (byte) 252: // WON'T
				next = OPTION;
				break;
			case (byte) 253: // DO
				next = OPTION;
				break;
			case (byte) 254: // DON'T
				next = OPTION;
				break;
			default:
				option = -1;
				bufferPos = 0;
				dispatchCommand();
			}
			return next;
		case OPTION:
			option = buffer[0];
			bufferPos = 0;
			dispatchCommand();
			break;
		case SUBOPTION:
			if (bufferPos == 1)
				option = buffer[0];

			if (hit && index == 0) {
				dispatchCommand();
				return COMMAND;
			}
			return SUBOPTION;
		}
		return NORMAL;
	}

	public void handlePacket(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			if (findChar != null) {
				int j;
				for (j = 0; j < findChar.length; j++) {
					if (data[i] == findChar[j])
						break;
				}
				if (j < findChar.length) {
					setState(getNextState(true, j));
					continue;
				}
			}
			buffer[bufferPos++] = data[i];
			if (bufferPos >= findLen)
				setState(getNextState(false, 0));
		}
		if (ansiState == NORMAL) {
			dispatchData();
			bufferPos = 0;
		}
	}

	public void handlePacket(Buffer data) {
		int readable = data.readableBytes();
		for (int i = 0; i < readable; i++) {
			byte b = data.get();
			if(findChar != null) { 
				int j;
				for (j = 0; j < findChar.length; j++) {
					if (b == findChar[j])
						break;
				}
				if (j < findChar.length) {
					setState(getNextState(true, j));
					continue;
				}
			}
			buffer[bufferPos++] = b;
			if (bufferPos >= findLen)
				setState(getNextState(false, 0));
		}
		if (ansiState == NORMAL) {
			dispatchData();
			bufferPos = 0;
		}
	}

	public void setCallbacks(Set<TelnetProcessor> callbacks) {
		this.callbacks = callbacks;
	}

	private void dispatchTitle() {
		String title = new String(buffer, 0, bufferPos);
		for (TelnetProcessor processor : callbacks) {
			if (isTx)
				processor.onClientTitle(title);
			else
				processor.onServerTitle(title);
		}
	}

	private void dispatchData() {
		String data = new String(buffer, 0, bufferPos);
		if (bufferPos == 0)
			return;

		for (TelnetProcessor processor : callbacks) {
			if (isTx)
				processor.onClientData(data);
			else
				processor.onServerData(data);
		}
	}

	private void dispatchCommand() {
		TelnetCommand telnetCommand = TelnetCommand.parse(command);
		TelnetOption telnetOption = TelnetOption.parse(option);
		byte[] data = Arrays.copyOfRange(buffer, 0, bufferPos);

		for (TelnetProcessor processor : callbacks) {
			if (isTx)
				processor.onClientCommand(telnetCommand, telnetOption, data);
			else
				processor.onServerCommand(telnetCommand, telnetOption, data);
		}
	}

	private void dispatchAnsiControl(byte command) {
		AnsiMode mode = AnsiMode.parse(ansiMode);
		TelnetCommand telnetCommand = TelnetCommand.parse(command);
		int[] arguments = Arrays.copyOf(args, argPos);

		for (TelnetProcessor processor : callbacks) {
			if (isTx)
				processor.onClientAnsiControl(mode, telnetCommand, arguments);
			else
				processor.onServerAnsiControl(mode, telnetCommand, arguments);
		}
	}

	public void setTx(boolean isTx) {
		this.isTx = isTx;
	}

	private int args[];
	private int state;
	private int ansiState;
	private int ansiMode;
	private int bufferPos = 0;
	private int argPos = 0;
	private byte command;
	private byte option;
	private byte buffer[];
	private byte findChar[];
	private int findLen;
	private Set<TelnetProcessor> callbacks;
	private boolean isTx;

	static final int NORMAL = 0;
	static final int COMMAND = 1;
	static final int OPTION = 2;
	static final int SUBOPTION = 3;

	static final int ANSI = 1;
	static final int CSI = 2;
	static final int VALUE = 3;
	static final int QUOT = 4;
	static final int XTERM = 5;
	static final int SHARP = 6;
	static final int EXCL = 7;

}
