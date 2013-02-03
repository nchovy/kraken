/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers;

import java.util.ArrayList;

import org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos.ShareInfo502I;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ShareInfo502Container  implements ContainerInterface{

	int entriesRead;
	ArrayList<ShareInfo502I> buffer;
	@Override
	public void parse(Buffer b) {
		// TODO Auto-generated method stub
		
	}

}
