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
public class ConnectInfoStruct implements StructInterface {

	int level;
	ContainerInterface connectionInfo;
	ContainerTypeMapper mapper;
	@Override
	public void parse(Buffer b) {
		// TODO Auto-generated method stub
		
	}
	

}
