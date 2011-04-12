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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.decoder.telnet.TelnetProcessor;
import org.krakenapps.pcap.decoder.telnet.TelnetSession;
import org.krakenapps.pcap.util.Buffer;

public class TelnetDecoder implements TcpProcessor {
	private Set<TelnetProcessor> callbacks;
	private Map<TcpSessionKey, TelnetSession> sessionMapTx;
	private Map<TcpSessionKey, TelnetSession> sessionMapRx;

	public TelnetDecoder() {
		callbacks = new HashSet<TelnetProcessor>();
		sessionMapTx = new HashMap<TcpSessionKey, TelnetSession>();
		sessionMapRx = new HashMap<TcpSessionKey, TelnetSession>();
	}

	public void register(TelnetProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(TelnetProcessor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void handleRx(TcpSessionKey sessionKey, Buffer data) {
		TelnetSession session = sessionMapRx.get(sessionKey);
		session.handlePacket(data);
		// handleRxBuffer(session, data);
	}

	@Override
	public void handleTx(TcpSessionKey sessionKey, Buffer data) {
		TelnetSession session = sessionMapTx.get(sessionKey);
		session.handlePacket(data);
		// handleTxBuffer(session, data);
	}

	@Override
	public void onEstablish(TcpSessionKey sessionKey) {
		TelnetSession session;
		session = new TelnetSession();
		session.setCallbacks(callbacks);
		session.setTx(true);
		sessionMapTx.put(sessionKey, session);
		session = new TelnetSession();
		session.setCallbacks(callbacks);
		session.setTx(false);
		sessionMapRx.put(sessionKey, session);
	}

	@Override
	public void onFinish(TcpSessionKey session) {
		sessionMapTx.remove(session);
		sessionMapRx.remove(session);
	}

	@Override
	public void onReset(TcpSessionKey session) {
		sessionMapTx.remove(session);
		sessionMapRx.remove(session);
	}

}
