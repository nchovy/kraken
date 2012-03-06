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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.api.TelnetCommand;

class TelnetEncoder implements ProtocolEncoder {
	private final byte INTERPRET_AS_CONTROL = (byte) 255;

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		if (message instanceof TelnetOptionControl) {
			encodeOptionControl(message, out);
		} else if (message instanceof TelnetCommand) {
			encodeCommand(message, out);
		} else if (message instanceof String) {
			encodeString(session, message, out);
		} else if (message instanceof AnsiEscapeCode) {
			encodeAnsiEscapeCode(message, out);
		} else if (message instanceof byte[]) {
			encodeBytes(message, out);
		}
	}

	private void encodeAnsiEscapeCode(Object message, ProtocolEncoderOutput out) {
		AnsiEscapeCode ansiEscapeCode = (AnsiEscapeCode) message;
		IoBuffer buffer = getEscapedData(ansiEscapeCode.toByteArray());
		buffer.flip();
		out.write(buffer);
	}

	private void encodeBytes(Object message, ProtocolEncoderOutput out) {
		IoBuffer buffer = getEscapedData((byte[]) message);
		buffer.flip();
		out.write(buffer);
	}

	private void encodeCommand(Object message, ProtocolEncoderOutput out) {
		TelnetCommand command = (TelnetCommand) message;
		byte[] commandBytes = command.toByteArray();

		IoBuffer buffer = IoBuffer.allocate(1 + commandBytes.length);
		buffer.put(INTERPRET_AS_CONTROL);
		buffer.put(commandBytes);
		buffer.flip();
		out.write(buffer);
	}

	private void encodeString(IoSession session, Object message, ProtocolEncoderOutput out)
			throws CharacterCodingException {
		String m = (String) message;
		if (m.length() == 0)
			return;

		byte[] string = m.getBytes(Charset.forName("utf-8"));
		IoBuffer buffer = getEscapedData(string);
		buffer.flip();
		out.write(buffer);
	}

	private IoBuffer getEscapedData(byte[] data) {
		IoBuffer buffer = IoBuffer.allocate(data.length * 2);
		for (int i = 0; i < data.length; i++) {
			// escape IAC in data stream
			if (data[i] == INTERPRET_AS_CONTROL) {
				buffer.put(data[i]);
				buffer.put(data[i]);
			} else {
				buffer.put(data[i]);
			}
		}
		return buffer;
	}

	private void encodeOptionControl(Object message, ProtocolEncoderOutput out) {
		final int AVERAGE_OPTION_COMMAND_LENGTH = 3;

		TelnetOptionControl optionControl = (TelnetOptionControl) message;
		IoBuffer buffer = IoBuffer.allocate(AVERAGE_OPTION_COMMAND_LENGTH).setAutoExpand(true);

		// e.g. IAC (255), WILL (251), ECHO (1)
		buffer.put(INTERPRET_AS_CONTROL);
		buffer.put(optionControl.getTypeCode());
		buffer.put(optionControl.getOptionCode());

		buffer.flip();
		out.write(buffer);
	}

	public void dispose(IoSession session) throws Exception {
	}
}