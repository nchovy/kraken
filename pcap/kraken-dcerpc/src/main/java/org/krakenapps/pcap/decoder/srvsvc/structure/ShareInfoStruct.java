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
public class ShareInfoStruct implements StructInterface{

	int level;
	ContainerInterface shareInfo;
	ContainerTypeMapper mapper;
	@Override
	public void parse(Buffer b) {
		// TODO Auto-generated method stub
		
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public ContainerInterface getShareInfo() {
		return shareInfo;
	}
	public void setShareInfo(ContainerInterface shareInfo) {
		this.shareInfo = shareInfo;
	}
}
