/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers;

/**
 * @author tgnice@nchovy.com
 *
 */
import java.util.ArrayList;

import org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos.ConnectionInfo0;
import org.krakenapps.pcap.util.Buffer;

public class ConnectInfo0Container implements ContainerInterface{

	int entriesRead;
	ArrayList<ConnectionInfo0> buffer;
	public int getEntriesRead() {
		return entriesRead;
	}
	public void setEntriesRead(int entriesRead) {
		this.entriesRead = entriesRead;
	}
	public ArrayList<ConnectionInfo0> getBuffer() {
		return buffer;
	}
	public void setBuffer(ArrayList<ConnectionInfo0> buffer) {
		this.buffer = buffer;
	}
	/* (non-Javadoc)
	 * @see org.krakenapps.pcap.decoder.srvsvc.structure.ContainerInterface#parse(org.krakenapps.pcap.util.Buffer)
	 */
	@Override
	public void parse(Buffer b) {
		// TODO Auto-generated method stub
		
	}
	
}
