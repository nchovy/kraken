/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure;

import org.krakenapps.pcap.decoder.srvsvc.ContainerTypeMapper;
import org.krakenapps.pcap.decoder.srvsvc.structure.containers.ContainerInterface;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author tgnice@nchovy.com
 *
 */
public class SessionInfoStruct implements StructInterface{

	int level;
	ContainerInterface sessionInfo;
	ContainerTypeMapper mapper;
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public ContainerInterface getSessionInfo() {
		return sessionInfo;
	}
	public void setSessionInfo(ContainerInterface sessionInfo) {
		this.sessionInfo = sessionInfo;
	}
	@Override
	public void parse(Buffer b) {
		// TODO Auto-generated method stub
		
	}
	
}
