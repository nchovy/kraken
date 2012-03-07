package org.krakenapps.pcap.decoder.smb;

import org.krakenapps.pcap.util.Buffer;


public interface SmbProcessor {
/*	public void process();
	public void 
*/
	public void processTcpRx(Buffer b);
	public void processTcpTx(Buffer b);
	public void processUdp(Buffer b);
	public void processMailslot(Buffer b);
}
