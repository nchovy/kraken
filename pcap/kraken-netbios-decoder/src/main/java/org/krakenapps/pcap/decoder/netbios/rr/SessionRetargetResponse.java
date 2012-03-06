package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionRetargetResponse implements NetBiosSessionData {
	private InetAddress retargetIPAdress;
	private short port;

	private SessionRetargetResponse(InetAddress retargetAddress, short port) {
		this.retargetIPAdress = retargetAddress;
		this.port = port;
	}

	// this use retarget response packet only
	// this use request packet only
	public InetAddress getRetargetIPAdress() {
		return retargetIPAdress;
	}

	public short getPort() {
		return port;
	}

	public static NetBiosSessionData parse(Buffer b) {
		InetAddress retargetAddress = readIpAddress(b);
		short port = b.getShort();
		return new SessionRetargetResponse(retargetAddress, port);
	}

	private static InetAddress readIpAddress(Buffer b) {
		byte[] addr = new byte[4];
		b.gets(addr);
		try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String toString(){
		return String.format("retargetIPAddress = %s , port = 0x%s\n",
				this.retargetIPAdress , Integer.toHexString(this.port));
	}

}
