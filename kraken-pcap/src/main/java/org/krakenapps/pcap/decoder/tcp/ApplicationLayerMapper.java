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