package org.krakenapps.pcap.decoder.smb.structure;

public class SmbResumeKey {
	byte reserved;
	byte []serverState = new byte[16];
	byte []clientState = new byte[4];
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public byte[] getServerState() {
		return serverState;
	}
	public void setServerState(byte[] serverState) {
		this.serverState = serverState;
	}
	public byte[] getClientState() {
		return clientState;
	}
	public void setClientState(byte[] clientState) {
		this.clientState = clientState;
	}
	@Override
	public String toString(){
		return String.format("Structure : SmbResumeKey\n"+
				"reserved = 0x%s , serverState = %s , clientState = %s\n",
				Integer.toHexString(this.reserved) , this.serverState.toString() , this.clientState.toString());
	}
	
}
