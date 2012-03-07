package org.krakenapps.pcap.decoder.tftp;

import java.io.InputStream;

public interface TftpProcessor {
	void onCommand(String command);

	void onExtractFile(InputStream is, String fileName);
}
