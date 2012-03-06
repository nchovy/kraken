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
package org.krakenapps.pcap.decoder.tcp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mindori
 */
enum Action {
	SEND_SYN, RECV_SYN, RECV_SYNACKED, RECV_RST, SEND_FIN, RECV_FIN, RECV_FINACKED, RECV_ACKED
}

public class TcpTransitionMapper {
	private final Map<TcpState, Map<Action, TcpState>> fsm;

	public TcpTransitionMapper() {
		fsm = new HashMap<TcpState, Map<Action, TcpState>>();
		buildFsm(fsm);
	}

	private void buildFsm(Map<TcpState, Map<Action, TcpState>> fsm) {
		connect(fsm, TcpState.LISTEN, Action.SEND_SYN, TcpState.SYN_SENT);
		connect(fsm, TcpState.LISTEN, Action.RECV_SYN, TcpState.SYN_RCVD);
		connect(fsm, TcpState.SYN_SENT, Action.RECV_SYNACKED, TcpState.ESTABLISHED);
		connect(fsm, TcpState.SYN_RCVD, Action.RECV_SYNACKED, TcpState.ESTABLISHED);
		connect(fsm, TcpState.ESTABLISHED, Action.SEND_FIN, TcpState.FIN_WAIT_1);
		connect(fsm, TcpState.ESTABLISHED, Action.RECV_FIN, TcpState.CLOSE_WAIT);
		// Normal close.
		connect(fsm, TcpState.FIN_WAIT_1, Action.RECV_ACKED, TcpState.FIN_WAIT_2);
		connect(fsm, TcpState.FIN_WAIT_1, Action.RECV_FINACKED, TcpState.CLOSED);
		// Simultaneous close.
		connect(fsm, TcpState.FIN_WAIT_1, Action.RECV_FIN, TcpState.CLOSING);
		// TIME_WAIT -> CLOSED.
		connect(fsm, TcpState.FIN_WAIT_2, Action.RECV_FIN, TcpState.CLOSED);
		// TIME_WAIT -> CLOSED.
		connect(fsm, TcpState.CLOSING, Action.RECV_FINACKED, TcpState.CLOSED);
		connect(fsm, TcpState.CLOSE_WAIT, Action.SEND_FIN, TcpState.LAST_ACK);
		connect(fsm, TcpState.LAST_ACK, Action.RECV_FINACKED, TcpState.CLOSED);
	}

	private void connect(Map<TcpState, Map<Action, TcpState>> fsm, TcpState state1, Action action,
			TcpState state2) {
		Map<Action, TcpState> m = fsm.get(state1);
		if (m == null) {
			m = new HashMap<Action, TcpState>();
			fsm.put(state1, m);
		}
		m.put(action, state2);
	}

	public TcpState map(TcpPacket segment, TcpState state, Action action) {
		try {
			if (fsm == null || fsm.get(state).get(action) == null)
				return TcpState.CLOSED;
			return fsm.get(state).get(action);	
		} catch(NullPointerException e) { 
			return TcpState.CLOSED;
		}
	}
}
