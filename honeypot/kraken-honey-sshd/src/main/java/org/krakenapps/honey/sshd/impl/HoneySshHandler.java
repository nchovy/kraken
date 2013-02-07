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
package org.krakenapps.honey.sshd.impl;

import org.krakenapps.honey.sshd.HoneySshSession;
import org.krakenapps.honey.sshd.handler.Shell;
import org.krakenapps.termio.TerminalEventListener;
import org.krakenapps.termio.TerminalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneySshHandler implements TerminalEventListener {
	private final Logger logger = LoggerFactory.getLogger(HoneySshHandler.class.getName());
	private HoneySshSession session;
	private Shell shell;

	public HoneySshHandler(HoneySshSession session) {
		this.session = session;
	}

	@Override
	public void onConnected(TerminalSession session) {
		shell = new Shell();
		shell.setSession(this.session);
		shell.start();
	}

	@Override
	public void onDisconnected(TerminalSession session) {
		shell.kill();
	}

	@Override
	public void onCommand(TerminalSession session, int command, byte[] option) {
	}

	@Override
	public void onData(TerminalSession session, byte b) {
		try {
			session.getInputStream().push(b);
		} catch (InterruptedException e) {
		}
	}

}
