package org.krakenapps.pcap.decoder.tftp;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class TftpSession {
	private InetSocketAddress srcAddr;
	private InetAddress destIp;

	private TftpMethod method;
	private String fileName;
	private TftpMode mode;

	private int sendNum = 0;
	private int ackNum = 0;

	private Buffer data;

	public TftpSession(int opCode, InetSocketAddress srcAddr, InetAddress destIp, byte[] fileNameBytes, byte[] modeBytes) {
		if (opCode == 1)
			method = TftpMethod.GET;
		else
			method = TftpMethod.PUT;

		this.srcAddr = srcAddr;
		this.destIp = destIp;

		fileName = new String(fileNameBytes);
		setMode(modeBytes);

		data = new ChainBuffer();
	}

	public InetSocketAddress getSrcAddress() {
		return srcAddr;
	}

	public InetAddress getDestIp() {
		return destIp;
	}

	public TftpMethod getMethod() {
		return method;
	}

	public String getFileName() {
		return fileName;
	}

	public TftpMode getMode() {
		return mode;
	}

	private void setMode(byte[] modeBytes) {
		String s = new String(modeBytes);
		if (s.equalsIgnoreCase("netascii")) {
			mode = TftpMode.NETASCII;
		} else if (s.equalsIgnoreCase("octet")) {
			mode = TftpMode.OCTET;
		} else if (s.equalsIgnoreCase("mail")) {
			mode = TftpMode.MAIL;
		}
	}

	public int getSendNum() {
		return sendNum;
	}

	public void incSendNum() {
		sendNum++;
	}

	public int getAckNum() {
		return ackNum;
	}

	public void incAckNum() {
		ackNum++;
	}

	public Buffer getData() {
		return data;
	}

	public void putData(byte[] b) {
		data.addLast(b);
	}

	public boolean equals(InetSocketAddress sockAddr, InetAddress addr) {
		if (srcAddr.equals(sockAddr) && destIp.equals(addr))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "tftp " + destIp.toString() + " " + method.toString() + " " + fileName + "(" + mode + ")";
	}
}
