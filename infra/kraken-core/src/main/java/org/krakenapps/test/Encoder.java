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
package org.krakenapps.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.api.TelnetCommand;
import org.krakenapps.console.TelnetOptionControl;

public class Encoder {
	Charset charset = Charset.forName("utf-8");
	CharsetEncoder charsetEncoder = charset.newEncoder();
	
	private final byte INTERPRET_AS_CONTROL = (byte) 255;
	
	public void encode(Session session, Object message, Writer out) throws Exception {
		if (message instanceof TelnetCommand) {
			encodeCommand(session, message, out);
		} else if (message instanceof String) {
			encodeString(session, message, out);
		} else if (message instanceof AnsiEscapeCode){
			encodeAnsiEscapeCode(session, message, out);
		} else if( message instanceof byte[]){
			encodeBytes(session, message, out);
		} else if( message instanceof TelnetOptionControl){
			encodeOptionControl(session, message, out);
		}
	}

	private void encodeOptionControl(Session session, Object message, Writer out){
		final int AVERAGE_OPTION_COMMAND_LENGTH = 3;
		
		TelnetOptionControl optionControl = (TelnetOptionControl) message;
		ByteBuffer buffer = ByteBuffer.allocate(AVERAGE_OPTION_COMMAND_LENGTH);
		
		buffer.put(INTERPRET_AS_CONTROL);
		buffer.put(optionControl.getTypeCode());
		buffer.put(optionControl.getOptionCode());
		
		buffer.flip();
		try {
			out.send(new WriteRequest(session.getChannel(), buffer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void encodeBytes(Session session, Object message, Writer out) throws InterruptedException {
		ByteBuffer buffer = getEscapedData((byte[]) message);
		buffer.flip();
		
		try {
			out.send(new WriteRequest(session.getChannel(), buffer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void encodeAnsiEscapeCode(Session session, Object message, Writer out) throws InterruptedException {
		AnsiEscapeCode ansiEscapeCode = (AnsiEscapeCode) message;
		ByteBuffer buffer = getEscapedData(ansiEscapeCode.toByteArray());
		buffer.flip();
		
		try {
			out.send(new WriteRequest(session.getChannel(), buffer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void encodeCommand(Session session, Object message, Writer out) throws InterruptedException {
		TelnetCommand command = (TelnetCommand) message;
		byte[] commandBytes = command.toByteArray();
		
		ByteBuffer buffer = ByteBuffer.allocate(1 + commandBytes.length);
		buffer.put(INTERPRET_AS_CONTROL);
		buffer.put(commandBytes);
		buffer.flip();
		
		try {
			out.send(new WriteRequest(session.getChannel(), buffer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void encodeString(Session session, Object message, Writer out)
			throws CharacterCodingException, IOException, InterruptedException {

		String m = (String) message;
		if (m.length() == 0)
			return;

		ByteBuffer bb = charset.encode(m);
		byte[] string = new byte[bb.limit()];
		bb.get(string);
		bb = getEscapedData(string);
		
		out.send(new WriteRequest(session.getChannel(), bb));
	}
	
	private ByteBuffer getEscapedData(byte[] data){
		ByteBuffer buffer = ByteBuffer.allocate(data.length * 2);
		for( int i=0; i< data.length; i++){
			if( data[i] == INTERPRET_AS_CONTROL){
				buffer.put(data[i]);
				buffer.put(data[i]);
			} else{
				buffer.put(data[i]);
			}
		}
		return buffer;
	}
}
