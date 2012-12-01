/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers;

import java.util.ArrayList;

import org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos.FileInfo3;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author tgnice@nchovy.com
 *
 */
public class FileInfo3Container implements ContainerInterface{

	int entriesRead;
	ArrayList<FileInfo3> buffer;
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
	public ArrayList<FileInfo3> getBuffer() {
		return buffer;
	}
	public void setBuffer(ArrayList<FileInfo3> buffer) {
		this.buffer = buffer;
	}
	
}
