package org.krakenapps.ice;

import java.text.MessageFormat;

public class IceEndpointSpec {
	public enum Protocol {
		tcp, udp
	};

	private Protocol proto;
	private String hostname;
	private int port;

	public IceEndpointSpec(Protocol proto, String hostname, int port) {
		this.proto = proto;
		this.hostname = hostname;
		this.port = port;
	}

	public IceEndpointSpec(Protocol proto, int port) {
		this.proto = proto;
		this.hostname = null;
		this.port = port;
	}

	@Override
	public String toString() {
		if (hostname == null)
			return String.format("%s -p %d", proto.toString(), port);
		else
			return String.format("%s -h %s -p %d", proto.toString(), hostname, port);
	}

	public static void main(String[] args) {
		System.out.println(new IceEndpointSpec(Protocol.tcp, 40001).toString());
		System.out.println(new IceEndpointSpec(Protocol.tcp, "127.0.0.1", 40001).toString());
	}
}
