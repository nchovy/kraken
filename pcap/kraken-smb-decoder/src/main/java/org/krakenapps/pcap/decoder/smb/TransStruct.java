package org.krakenapps.pcap.decoder.smb;

import org.krakenapps.pcap.util.Buffer;

public interface TransStruct {

	public TransStruct parse(Buffer b , SmbSession session);
	public String toString();
}
