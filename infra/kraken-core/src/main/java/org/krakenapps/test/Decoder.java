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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import org.krakenapps.console.ShellSession;

public class Decoder {
	
	public Object doDecode(Session session, Object in, DecoderOutput out) throws CharacterCodingException {
		byte b = 0;
		ByteBuffer temp = (ByteBuffer) in;

		ShellSession shell = (ShellSession) session.getAttribute("session");
		session.setAttribute("FSM", new TelnetStateMachine(out, shell.getScriptContext()));
		
		TelnetStateMachine state = (TelnetStateMachine) session.getAttribute("FSM");
		
		while (temp.hasRemaining()) {
			b = temp.get();
			state.feed(b);
		}
		return null;
	}
}
