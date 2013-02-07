/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.termio;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalDecoder {
	private final Logger logger = LoggerFactory.getLogger(TerminalDecoder.class.getName());
	private byte commandCode;
	private State state;

	private TerminalSession session;
	private ByteBuffer negoBuffer;
	private State negoState;

	private enum State {
		Command, Option, Data, AnsiEscape, SubNegotiation
	}

	public TerminalDecoder(TerminalSession session) {
		this.session = session;
		this.state = State.Data;
		this.negoBuffer = ByteBuffer.allocate(16);
	}

	public void feed(byte b) {
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
		if (negoState == State.Command && b == TerminalCommand.SE) {
			negoBuffer.flip();

			// window size
			byte c = negoBuffer.get();
			if (c == 0x1f) {
				short width = negoBuffer.getShort();
				short height = negoBuffer.getShort();

				logger.trace("kraken-core: changed negotiate about window size: [{}, {}]", width, height);
			}

			negoBuffer.clear();
			state = State.Data;
			return;
		}

		negoBuffer.put(b);
	}

	private void handleOption(byte b) {
		notifyCommand(commandCode, new byte[] { b });
		state = State.Data;
	}

	private void handleCommand(byte b) {
		if (isEscapeCode(b)) {
			notifyData(b);
			state = State.Data;
		} else {
			commandCode = b;
			if (isOptionCommand(commandCode)) {
				if (commandCode == TerminalCommand.SB) {
					state = State.SubNegotiation;
					negoState = State.Option;
				} else
					state = State.Option;
			} else {
				notifyCommand(commandCode, new byte[] {});
				state = State.Data;
			}
		}
	}

	private void handleData(byte b) {
		if (isInterpreatAsControl(b))
			state = State.Command;
		else if (isEscape(b))
			state = State.AnsiEscape;
		else
			notifyData(b);
	}

	private boolean isEscape(byte b) {
		return b == (byte) 0x1b;
	}

	private boolean isInterpreatAsControl(byte commandCode) {
		return commandCode == TerminalCommand.InterpretAsControl;
	}

	private boolean isEscapeCode(byte commandCode) {
		return isInterpreatAsControl(commandCode);
	}

	private boolean isOptionCommand(byte commandCode) {
		return commandCode == TerminalCommand.SB || commandCode == TerminalCommand.Will
				|| commandCode == TerminalCommand.Wont || commandCode == TerminalCommand.Do
				|| commandCode == TerminalCommand.Dont;
	}

	private void notifyCommand(int commandCode, byte[] b) {
		for (TerminalEventListener listener : session.getListeners()) {
			try {
				listener.onCommand(session, commandCode, b);
			} catch (Throwable t) {
				logger.warn("kraken termio: terminal event listener should not throw any exception", t);
			}
		}
	}

	private void notifyData(byte b) {
		for (TerminalEventListener listener : session.getListeners()) {
			try {
				listener.onData(session, b);
			} catch (Throwable t) {
				logger.warn("kraken termio: terminal event listener should not throw any exception", t);
			}
		}
	}

}
