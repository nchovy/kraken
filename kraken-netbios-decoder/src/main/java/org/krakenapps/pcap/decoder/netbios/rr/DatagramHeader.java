package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;

import org.krakenapps.pcap.decoder.netbios.NetBiosDatagramType;

abstract public class DatagramHeader {
	public final static byte SNT = (byte) 0x06;
	public final static byte F = (byte) 0x02;
	public final static byte M = (byte) 0x01;
	protected NetBiosDatagramType msgType;
	protected byte flags;
	protected short dgmID;
	protected InetAddress addresses;
	protected short port;

	public NetBiosDatagramType getMsgType() {
		return msgType;
	}

	public void setMsgType(NetBiosDatagramType msgType) {
		this.msgType = msgType;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public short getDgmID() {
		return dgmID;
	}

	public void setDgmID(short dgmID) {
		this.dgmID = dgmID;
	}

	public InetAddress getAddresses() {
		return addresses;
	}

	public void setAddresses(InetAddress addresses) {
		this.addresses = addresses;
	}

	public short getPort() {
		return port;
	}

	public void setPort(short port) {
		this.port = port;
	}

	public int isM() {
		return (int) (this.flags & M);
	}

	public int isF() {
		return (int) ((this.flags & F) >> 1);
	}

	public int isSNT() {
		return (int) ((this.flags & SNT) >> 2);
	}
	@Override
	public String toString(){
		return String.format("DatagramHeader\n"+
				"msgType = %s , flags = 0x%s , dgmID = 0x%s\n"+
				"address = %s , port = 0x%s\n",
				this,msgType , Integer.toHexString(this.flags) , Integer.toHexString(this.dgmID) , 
				this.addresses , Integer.toHexString(this.port));
		
	}
}
