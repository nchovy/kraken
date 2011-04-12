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
