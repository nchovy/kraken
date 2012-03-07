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
package org.krakenapps.test;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.TelnetCommand;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetStateMachine {
	private final Logger logger = LoggerFactory.getLogger(TelnetStateMachine.class.getName());
	private CharsetDecoder charsetDecoder;
	private IoBuffer temp;
	private int trailingByteCount = 0;
	private byte commandCode;
	private State state;
	//private ProtocolDecoderOutput out;
	private DecoderOutput out;
	private byte lastByte;
	private byte ansiPriorChar;
	private ScriptContext context;

	private ByteBuffer negoBuffer;
	private State negoState;

	private enum State {
		Command, Option, Data, AnsiEscape, SubNegotiation
	}

	public TelnetStateMachine(DecoderOutput out, ScriptContext context) {
		this.out = out;
		this.context = context;

		state = State.Data;
		charsetDecoder = Charset.forName("utf-8").newDecoder();
		temp = IoBuffer.allocate(8);
		negoBuffer = ByteBuffer.allocate(16);
	}

	public void feed(byte b) throws CharacterCodingException {
		switch (state) {
		case Data:
			handleData(b);
			break;
		case Command:
			handleCommand(b);
			break;
		case Option:
			handleOption(b);
			break;
		case AnsiEscape:
			handleAnsiEscape(b);
			break;
		case SubNegotiation:
			handleSubNegotiation(b);
			break;
		}
	}

	private void handleSubNegotiation(byte b) {
		if (negoState == State.Option && isInterpreatAsControl(b)) {
			negoState = State.Command;
			return;
		}

		// subnegotiation end (SE)
		if (negoState == State.Command && b == TelnetCommand.SE) {
			negoBuffer.flip();

			// window size
			byte c = negoBuffer.get();
			if (c == 0x1f) {
				short width = negoBuffer.getShort();
				short height = negoBuffer.getShort();

				context.setWindowSize(width, height);
				logger.trace("kraken-core: changed negotiate about window size: [{}, {}]", width, height);
			}

			negoBuffer.clear();
			state = State.Data;
			return;
		}

		negoBuffer.put(b);
	}

	private void handleAnsiEscape(byte b) {
		switch (b) {
		case 0x5b:
			state = State.AnsiEscape;
			break;
		case 'A':
			out.write(new FunctionKeyEvent(KeyCode.UP));
			state = State.Data;
			break;
		case 'B':
			out.write(new FunctionKeyEvent(KeyCode.DOWN));
			state = State.Data;
			break;
		case 'C':
			out.write(new FunctionKeyEvent(KeyCode.RIGHT));
			state = State.Data;
			break;
		case 'D':
			out.write(new FunctionKeyEvent(KeyCode.LEFT));
			state = State.Data;
			break;
		case '3':
		case '1':
		case '4':
			ansiPriorChar = b;
			state = State.AnsiEscape;
			break;
		case '~':
			switch (ansiPriorChar) {
			case '3':
				out.write(new FunctionKeyEvent(KeyCode.DELETE));
				break;
			case '1':
				out.write(new FunctionKeyEvent(KeyCode.HOME));
				break;
			case '4':
				out.write(new FunctionKeyEvent(KeyCode.END));
				break;
			}
			ansiPriorChar = '\0';
			state = State.Data;
			break;
		}
	}

	private void handleOption(byte b) {
		TelnetCommandHandler handler = TelnetCommandHandlerFactory.create(commandCode);
		handler.execute(new byte[] { b });

		if (b == 0x1f) {
			// send "do negotiate about window size"
			ScriptOutputStream os = context.getOutputStream();
			os.print(new TelnetCommand() {
				@Override
				public byte[] toByteArray() {
					return new byte[] { (byte) 0xfd, 0x1f };
				}
			});
		}

		state = State.Data;
	}

	private void handleCommand(byte b) throws CharacterCodingException {
		if (isEscapeCode(b)) {
			putData(b);
			state = State.Data;
		} else {
			commandCode = b;
			if (isOptionCommand(commandCode)) {
				if (commandCode == TelnetCommand.SB) {
					state = State.SubNegotiation;
					negoState = State.Option;
				} else
					state = State.Option;
			} else {
				TelnetCommandHandler handler = TelnetCommandHandlerFactory.create(commandCode);
				handler.execute(new byte[] {});
				state = State.Data;
			}
		}
	}

	private void handleData(byte b) throws CharacterCodingException {
		if (isInterpreatAsControl(b))
			state = State.Command;
		else if (isEscape(b))
			state = State.AnsiEscape;
		else
			putData(b);
	}

	private boolean isEscape(byte b) {
		return b == (byte) 0x1b;
	}

	private void putData(byte b) throws CharacterCodingException {
		// exceptional case: CR NUL to CR LF
		if (lastByte == 13 && b == 0)
			b = 10;

		lastByte = b;

		temp.put(b);
		if (trailingByteCount == 0) {
			trailingByteCount = trailingBytes(b);
		} else {
			trailingByteCount--;
		}

		if (trailingByteCount == 0) {
			consume();
		}
	}

	private void consume() throws CharacterCodingException {
		temp.flip();
		String character = temp.getString(charsetDecoder);
		char ch = character.charAt(0);

		if (ch == (char) 1)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_A));
		else if (ch == (char) 2)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_B));
		else if (ch == (char) 3)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_C));
		else if (ch == (char) 4)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_D));
		else if (ch == (char) 5)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_E));
		else if (ch == (char) 6)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_F));
		else if (ch == (char) 14)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_N));
		else if (ch == (char) 16)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_P));
		else if (ch == (char) 18)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_R));
		else if (ch == (char) 21)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_U));
		else if (ch == (char) 22)
			out.write(new FunctionKeyEvent(KeyCode.CTRL_V));
		else if (ch == (char) 127 || ch == (char) 8)
			out.write(new FunctionKeyEvent(KeyCode.BACKSPACE));
		else
			out.write(character);

		temp.clear();
	}

	private int trailingBytes(byte leadingByte) {
		int decimal = (int) leadingByte;
		if ((decimal & 0xF0) == 0xF0) {
			return 3;
		} else if ((decimal & 0xE0) == 0xE0) {
			return 2;
		} else if ((decimal & 0xD0) == 0xC0) {
			return 1;
		} else {
			return 0; // ASCII
		}
	}

	private boolean isInterpreatAsControl(byte commandCode) {
		return commandCode == TelnetCommand.InterpretAsControl;
	}

	private boolean isEscapeCode(byte commandCode) {
		return isInterpreatAsControl(commandCode);
	}

	private boolean isOptionCommand(byte commandCode) {
		return commandCode == TelnetCommand.SB || commandCode == TelnetCommand.Will
				|| commandCode == TelnetCommand.Wont || commandCode == TelnetCommand.Do
				|| commandCode == TelnetCommand.Dont;
	}
}
