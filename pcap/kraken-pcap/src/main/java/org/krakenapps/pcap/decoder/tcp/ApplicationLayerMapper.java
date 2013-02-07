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

import java.util.Collection;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.util.Buffer;

public class ApplicationLayerMapper {
	private TcpProtocolMapper mapper;

	public ApplicationLayerMapper(TcpProtocolMapper mapper) {
		this.mapper = mapper;
	}

	public void sendToApplicationLayer(Protocol protocol, TcpSessionKey key, TcpDirection direction, Buffer data) {
		Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);

		if (processors == null)
			return;

		for (TcpProcessor p : processors) {
			handlingL7(key, p, direction, data);
		}
	}

	private void handlingL7(TcpSessionKey key, TcpProcessor processor, TcpDirection direction, Buffer data) {
		if (direction == TcpDirection.ToServer)
			processor.handleTx(key, data);
		else
			processor.handleRx(key, data);
	}
}