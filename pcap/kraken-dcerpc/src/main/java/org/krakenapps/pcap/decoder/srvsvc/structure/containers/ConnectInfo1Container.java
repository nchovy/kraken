/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers;

import java.util.ArrayList;

import org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos.ConnectionInfo1;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ConnectInfo1Container implements ContainerInterface{

	int entriesRead;
	ArrayList<ConnectionInfo1> buffer;
	
	@Override
	public void parse(Buffer b) {
		// TODO Auto-generated method stub
		
	}
	public int getEntriesRead() {
		return entriesRead;
	}
	public void setEntriesRead(int entriesRead) {
		this.entriesRead = entriesRead;
	}
	public ArrayList<ConnectionInfo1> getBuffer() {
		return buffer;
	}
	public void setBuffer(ArrayList<ConnectionInfo1> buffer) {
		this.buffer = buffer;
	}


}
