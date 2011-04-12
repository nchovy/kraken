package org.krakenapps.pcap;

import org.krakenapps.pcap.util.Buffer;

/**
 * All injectable packet components should implements this interface. It
 * provides serialized packet buffer.
 * 
 * @author xeraph
 * @since 1.3
 */
public interface Injectable {
	Buffer getBuffer();
}
