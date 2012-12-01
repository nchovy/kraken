/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers;

import org.krakenapps.pcap.util.Buffer;

/**
 * @author tgnice@nchovy.com
 *
 */
public interface ContainerInterface {

	int EntriesRead = 0;
	public void parse(Buffer b );
}
